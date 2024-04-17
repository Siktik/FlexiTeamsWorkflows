package org.workflow.OntologyClasses;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import lombok.Getter;
import lombok.Setter;
import org.workflow.Simulation.EntityManager;
import org.workflow.Simulation.ResourceManager;
import org.workflow.Simulation.TaskRunConcept;
import org.workflow.SimulationHelpers.TimeManager;
import org.workflow.Printer.Printer;

@Getter
public class Workflow {

	/**
	 * a workflow supports parallelism by a token principle
	 * think of the workflow as if it was a directed acyclic Graph with a single entry point, a rootNode (startTask)
	 * the rootNode cannot have parents, but k childs,
	 * every node that is not the rootNode can have k parents and m childs
	 * nodes can only be connected to nodes with a greater height
	 * know if every node(task) saves information that it has processed a certain event, starting at the rootNode, we see
	 * that if rootnode x has processed event z and we want to process x on childnode y we have to check x that has processed z and
	 * we are good to go
	 * by induction it can be shown that this works for any complex workflow as long as we have a directed acyclic graph with directed edges only
	 * from node x with height t to any node having a height of t+n while n is strict positive
	 *
	 * a workflow therefore can have multiple endTasks, but only one startTask
	 * this also means that there are no conditional ways supported, for this other approaches need to be implemented
	 * also there are no loops possible
	 * didn't check which algorithms and datastructures would allow this but im pretty sure there are ways to achieve this
	 *
	 * but as its been said with this implementation any workflow with a single startpoint and any form of parallelism can be simulated
	 */

	// at this time workflows can have only one start Task
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
	 * this saves every TRC that is currently beeing executed
	 * by checking if the thread of a TRC is interrupted we find tasks that finished their execution
	 * currently only one instance of a task can be executed at a time with this approach
	 */
	private Map<String, TaskRunConcept> runningTasks;
	/**
	 * here we save the names of tasks that are currently free and where a planned TRC could execute
	 */
	private HashSet<String> freeTasks;
	/**
	 * here we save names of tasks where a TRC couldnt execute as there were resources missing
	 * this list of tasks is evaluated when TRC ended their execution as resources are freed then
	 * by saving names of the task in this list we ensure that we dont always have to check if for a waiting TRC all resources are available
	 * if the taskName where the TRC should execute is in this list while method startExecution is called, there are resources missing
	 */
	private HashSet<String> waitingForResources;

	/**
	 * saves names of tasks where TRCs are waiting, quickRef
	 */
	private HashSet<String> tasksHaveWaitingTRC;
	/**
	 * key: taskName, value: TRCs sorted by priority that are planned to execute the task with name as key
	 * when method planExecution() is called TRC are planned for every event that has just arrived or that finished on a task and has following tasks, while
	 * checking that this event has been processed by all predecessors (parallelism)
	 */
	private Map<String, PriorityQueue<TaskRunConcept>> waitingTasks;

	/**
	 * some unique identifier for every instantiated workflow
	 */
	private static int idCounter = 0;

	/**
	 * the thread that is running the main method of this workflow
	 * this is needed as the simulations main thread is running in the simulation manager distributing events to currently only one workflow but maybe
	 * more workflows in the future
	 */
	private Thread workflowMainThread;

	/**
	 * here the simulation manager adds events that are meant for this workflow
	 * as the workflow also works on this queue, by removing events where he adds a planned TRC in planExecution, we need to prevent
	 * ConcurrentModificationExceptions, as two thread are working on this queue
	 * this is achieved by the PriorityBlockingQueue
	 * this should not cause any remarkable performance bottleneck unless there are hundreds or thousands of events beeing scheduled within seconds
	 */
	private PriorityBlockingQueue<Event> queuedEvents;

	public Workflow(String name, Task startTask) {
		this.id = idCounter++;
		queuedEvents = new PriorityBlockingQueue<Event>(1, comparingEvents);
		this.name = name;
		this.startTask = startTask;

		workflowMainThread = new Thread(this::runWorkflow);
	}

	/**
	 * Initializer
	 */
	public void initTasksMap() {
		Set<String> taskNames = EntityManager.allTasks.keySet();
		runningTasks = new Hashtable<>();
		waitingTasks = new Hashtable<>();
		freeTasks = new HashSet<>();
		waitingForResources = new HashSet<>();
		tasksHaveWaitingTRC= new HashSet<>();

		/**
		 * pre initializing with all taskNames as keys and empty priorityQueues for future waiting TRC
		 */
		for (String key : taskNames) {
			waitingTasks.put(
					key,
					new PriorityQueue<TaskRunConcept>(comparingTRC)
			);
			freeTasks.add(key);
		}
	}


	public void startWorkflow() {
		workflowMainThread.start();
	}

	public void addEventToQueue(Event e) {
		queuedEvents.add(e);
	}


	/**
	 * the main of a Workflow, don't change the order of the method be
	 */
	private void runWorkflow() {
		/**
		 * set working to false on some condition to break from the currently infinite while loop
		 */
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
		int stop = 0;
		while (working) {
			planExecution();
			startExecution();
			if(!checkingStopCriteria) {
				if (runningTasks.isEmpty()) {
					checkingStopCriteria = true;
					stop = TimeManager.getSimTime();
				}
			}else
				if(TimeManager.getSimTime() - stop > 30) {
					System.out.println("\n##################################################\n" +
							"##################################################\n" +
							"############## Finishing SIMULATION ###############\n" +
							"############## AT SIM_TIME: " +
							TimeManager.getSimTime() +
							"      ###############\n" +
							"##################################################\n" +
							"##################################################\n");
					break;
				}
		}
	}
	boolean checkingStopCriteria= false;
	/**
	 * Details in README
	 */

	private void planExecution() {


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
			TaskRunConcept trc = runningTasks.remove(taskName);
			Event e = trc.event;
			Task task = trc.getTask();
			task.currentEvent = null;
			task.addProcessedEvent(trc.event);
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
				List<Task> predecessors = task.getPredecessorTasks();
				if (
						predecessors
								.stream()
								.allMatch(t -> t.hasProcessedEvent(e.getName()))
				) {
					Printer.print(
							e.getName(),
							"No followers for " + taskName + " execution ends here"
					);
					continue;
				}
			}
			for (Task follower : task.getFollowingTasks()) {
				List<Task> predecessors = follower.getPredecessorTasks();
				String followerTaskName= follower.getName();
				if (
					predecessors
						.stream()
						.allMatch(t -> t.hasProcessedEvent(e.getName()))
				) {
					TaskRunConcept nextTRC = new TaskRunConcept(
						e,
						followerTaskName
					);
					waitingTasks.get(followerTaskName).add(nextTRC);
					tasksHaveWaitingTRC.add(followerTaskName);
					printed = false;
					Printer.print(
						e.getName(),
						"Planned TRC on " +
						followerTaskName +
						" event was processed by all predecessors"
					);
				} else {
					/**
					 * this is in the case that the event has not been processed by all predecessor tasks
					 * this does nothing than printing the reason at the moment
					 */
					List<String> causes = predecessors
						.stream()
						.filter(t -> !t.hasProcessedEvent(e.getName()))
						.map(Task::getName)
						.toList();
					Printer.print(
						e.getName(),
						"Couldn't plan " +
						followerTaskName +
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
			tasksHaveWaitingTRC.add(e.startTask);
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




	/**
	 * Details in README
	 */
	private void startExecution() {
		/**
		 * printing Statement
		 */
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

		HashSet<String> toDelete= new HashSet<>();
		for (String taskName : tasksHaveWaitingTRC) {
			if (
				freeTasks.contains(taskName) &&
				!waitingTasks.get(taskName).isEmpty()&&
						!waitingForResources.contains(taskName)
			) {
				TaskRunConcept trc = waitingTasks.get(taskName).peek();

				assert trc != null;
				if (ResourceManager.checkResourceAssertionPossible(trc, true, null)) {
					waitingTasks.get(taskName).poll();
					if(waitingTasks.get(taskName).isEmpty())
						toDelete.add(taskName);
					freeTasks.remove(taskName);
					checkingStopCriteria = false;
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
		tasksHaveWaitingTRC.removeAll(toDelete);
	}





	/**
	 * the comparator used in the queuedEvents queue
	 */

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

	/**
	 * the comparator used in the waitingTasks Map
	 */

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

	@Override
	public String toString() {
		return "Workflow{" +
				"startTask=" + startTask +
				", id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
