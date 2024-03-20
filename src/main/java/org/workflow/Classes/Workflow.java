package org.workflow.Classes;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import javax.xml.transform.Source;
import lombok.Getter;
import lombok.Setter;
import org.workflow.EntityManager;
import org.workflow.TimeManager;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

@Getter
public class Workflow {

	// at this time workflows can have only one start Task and one endTask
	// moreover the simulator only works with linear workflows
	@Setter
	private Task startTask;

	@Setter
	private Task endTask;

	@Setter
	private int id;

	@Getter
	public boolean working;

	private String name;

	/**
	 * this saves every task that is currently beeing executed
	 * by checking if the thread of a runTaskConcept is interrupted we find tasks that finished their execution
	 * currently only one instance of a task can be executed at a time
	 */
	private Map<String, TaskRunConcept> runningTasks;
	private HashSet<String> freeTasks;
	private HashSet<String> waitingForResources;

	private Map<String, PriorityQueue<TaskRunConcept>> waitingTasks;

	private static int idCounter = 0;
	private Thread workflowMainThread;

	public Workflow(String name, Task startTask, Task endTask) {
		this.id = idCounter++;
		queuedEvents = new PriorityBlockingQueue<Event>(1, comparingEvents);
		this.name = name;
		this.startTask = startTask;
		this.endTask = endTask;

		workflowMainThread = new Thread(this::runWorkflow);
	}

	public void startWorkflow() {
		workflowMainThread.start();
	}

	@Override
	public String toString() {
		return (
			"Workflow{" +
			"startTask=" +
			startTask.getName() +
			", endTask=" +
			endTask.getName() +
			'}'
		);
	}

	public void addEventToQueue(Event e) {
		queuedEvents.add(e);
	}

	int outPutLimiterTaskWaiting = TimeManager.getSimTime();
	int outPutLimiterEventWaiting = TimeManager.getSimTime();

	/**
	 * the main of a Workflow, don't change the order of the method calls unless you know what the effect would be
	 */
	private void runWorkflow() {
		working = true;
		initTasksMap();
		System.out.println(
			"\n##################################################\n" +
			"##################################################\n" +
			"############## STARTING SIMULATION ###############\n" +
			"############## AT SIM_TIME: " +
			TimeManager.getSimTime() +
			"      ###############\n" +
			"##################################################\n" +
			"##################################################\n"
		);

		while (working) {
			planExecution();
			startExecution();
		}
		Thread.currentThread().interrupt();
	}

	private void planExecution() {
		/**
		 * update Priorities of waiting events possibly here
		 */

		/**
		 * check finished events
		 */

		List<String> endedTasks = runningTasks
			.entrySet()
			.stream()
			.filter(stringTaskRunConceptEntry ->
					stringTaskRunConceptEntry
							.getValue()
							.t
							.isInterrupted())
			.map(Map.Entry::getKey)
			.toList();
		if (!endedTasks.isEmpty()){ System.out.println(
			"\n##################################################\n" +
			"\tChecking and Planning FINISHED TASKS\n" +
			"\ttime: " +
			TimeManager.getSimTime()
		);
		}

		for (String taskName : endedTasks) {
			/**
			 *  -   all tasks considered here have been finished
			 *  -   therefore add the information that the event has eben processed on this task
			 *  -   make task free in runningTasksMap by replacing finished TaskRunConcept with null
			 *  **less computing needed if names of free tasks here are saved in another hashmap**
			 *
			 */
			TaskRunConcept trc = runningTasks.get(taskName);
			Event e = trc.event;
			Task task = trc.getTask();
			task.currentEvent = null;
			task.addProcessedEvent(trc.event);
			runningTasks.remove(taskName);
			freeTasks.add(taskName);
			Printer.print(e.getName(), "Ended " + taskName+" -> freeing resources");
			ResourceManager.freeResources(
					task.getAssertedResources()
			);
			/**
			 * -    check all following tasks, initiate a TaskRunConcept for every task whose predecessors all match
			 *      the condition that they have finished processing the event
			 * -    add TaskRunConcepts to waiting Tasks Map
			 */
			if (task.getFollowingTasks().isEmpty()) {
				Printer.print(
					e.getName(),
					"No followers for " + taskName + " execution ends here"
				);
				continue;
			}
			for (Task follower : task.getFollowingTasks()) {
				List<Task> predecessors = follower.getPredecessorTasks();
				if (
					predecessors
						.stream()
						.allMatch(t -> t.hasProcessedEvent(e.getName()))
				) {
					TaskRunConcept nextTRC = new TaskRunConcept(
						e,
						follower.getName()
					);
					waitingTasks.get(follower.getName()).add(nextTRC);
					printed = false;
					Printer.print(
						e.getName(),
						"Planned TRC on " +
						follower.getName() +
						" event was processed by all predecessors"
					);
				} else {
					List<String> causes = predecessors
						.stream()
						.filter(t -> !t.hasProcessedEvent(e.getName()))
						.map(Task::getName)
						.toList();
					Printer.print(
						e.getName(),
						"Couldn't plan " +
						follower.getName() +
						" as predecessors\n" +
						Arrays.toString(causes.toArray()) +
						" haven't processed the event"
					);
				}
			}
		}

		/**
		 * -    init TaskRunConcept for every Event that is new to the workflow
		 */
		if (!queuedEvents.isEmpty()) System.out.println(
			"\n##################################################\n" +
			"\tChecking and Planning NEW EVENTS\n" +
			"\ttime: " +
			TimeManager.getSimTime()
		);
		while (!queuedEvents.isEmpty()) {
			Event e = queuedEvents.poll();
			TaskRunConcept t = new TaskRunConcept(e, e.startTask);
			waitingTasks.get(e.startTask).add(t);
			printed = false;
			Printer.print(e.getName(), "Planned start on " + e.startTask);
		}

		/**
		 * could be done at start of this method as well were is another claussse checking for this condition
		 * but the condition there is intended to give the print banner if the forloop following that statement will
		 * have execution due to !list.empty
		 * reevaluating resources suits better after all new tasks have been planned as its easier to
		 * read on console
		 * syntactically it wouldn't make a difference if you do it at first if clause or here as
		 * the checking for resources does not have an impact on the rest of the planning method
		 */
		if(!endedTasks.isEmpty()){
			System.out.println(
					"\n##################################################\n" +
							"\tRECHECKING TASKS WAITING FOR RESOURCES\n" +
							"\ttime: " +
							TimeManager.getSimTime()
			);
			waitingForResources= ResourceManager.revaluateTasks(waitingForResources);
		}


	}

	private boolean printed = false;



	private void startExecution() {
		if (
			waitingTasks
				.values()
				.stream()
				.anyMatch(queue -> !queue.isEmpty()) &&
			!printed
		) {
			System.out.println(
				"\n##################################################\n" +
				"\tSTARTING EXECUTIONS\n" +
				"\ttime: " +
				TimeManager.getSimTime()
			);
			printed = true;
		}


		Set<String> taskNames = waitingTasks.keySet();
		for (String taskName : taskNames) {
			if (
				freeTasks.contains(taskName) &&
				!waitingTasks.get(taskName).isEmpty()&&
						!waitingForResources.contains(taskName)
			) {
				TaskRunConcept trc = waitingTasks.get(taskName).peek();

				assert trc != null;
				if (ResourceManager.checkResourceAssertionPossible(trc, true, null)) {
					waitingTasks.get(taskName).poll();
					freeTasks.remove(taskName);
					runningTasks.put(taskName, trc);
					Printer.print(
							trc.event.getName(),
							"starting " + taskName + " needs: " + trc.getTask().getTimeNeeded() + " seconds"
					);
					trc.start();
				} else {
					waitingForResources.add(taskName);
					Printer.errorPrint(
						trc.event.getName(),
						" couldn't start "+ trc.getTask().getName()+" due to missing Resources"
					);
				}

			}
		}
	}

	public void initTasksMap() {
		Set<String> taskNames = EntityManager.allTasks.keySet();
		runningTasks = new Hashtable<>();
		waitingTasks = new Hashtable<>();
		freeTasks = new HashSet<>();
		waitingForResources = new HashSet<>();
		for (String key : taskNames) {
			waitingTasks.put(
				key,
				new PriorityQueue<TaskRunConcept>(comparingTRC)
			);
			freeTasks.add(key);
		}
	}

	private PriorityBlockingQueue<Event> queuedEvents;

	private Comparator<Event> comparingEvents = new Comparator<Event>() {
		@Override
		public int compare(Event o1, Event o2) {
			if (o1.getPriority() > o2.getPriority()) return -1;
			if (o2.getPriority() > o1.getPriority()) return 1;
			if (o1.getStartTime()< o2.getStartTime()) return -1;
			if (o2.getStartTime()< o1.getStartTime()) return 1;
			return 0;
		}
	};
	private Comparator<TaskRunConcept> comparingTRC = new Comparator<
		TaskRunConcept
	>() {
		@Override
		public int compare(TaskRunConcept o1, TaskRunConcept o2) {
			if (o1.event.getPriority() > o2.event.getPriority()) return -1;
			if (o2.event.getPriority() > o1.event.getPriority()) return 1;
			if (o1.event.getStartTime() < o2.event.getStartTime()) return -1;
			if (o2.event.getStartTime()< o1.event.getStartTime()) return 1;
			return 0;
		}
	};
	/**
	 * a helper class to map currently running tasks, the thread that is executing this task and the event that is currently
	 * at this task in one object
	 */

}
