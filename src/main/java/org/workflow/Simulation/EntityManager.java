package org.workflow.Simulation;

import java.util.*;

import org.workflow.OntologyClasses.*;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

public class EntityManager {

	/**
	 * every instance that was imported from the ontology is safed here
	 */

	/**
	 * all events imported from ontology
	 */
	public static Hashtable<String, Event> allEvents;
	/**
	 * all Persons imported from ontology
	 */
	public static Hashtable<String, Person> allPersons;

	/**
	 * all Tasks imported from ontology
	 */
	public static Hashtable<String, Task> allTasks;
	/**
	 * all Qualifications imported from ontology
	 */
	public static Hashtable<String, Qualifikation> allQualifications;

	/**
	 * sorted tasks according to start-, end-, inner Task
	 */


	public static Hashtable<String, ResourceType> allResourceTypes;

	/**
	 * only the use of one workflow is currently safe and implemented
	 */
	public static Hashtable<String, Workflow> allWorkflows;

	/**
	 * while there is currently only one workflow possible for easy ref as its starttask is set for every event as every event takes place in this worrkflow
	 * as only one workflow is implemented
	 */
	public static Workflow workflow;

	public static void init() {
		allEvents = new Hashtable<>();
		allPersons = new Hashtable<>();
		allResourceTypes = new Hashtable<>();
		allTasks = new Hashtable<>();
		allQualifications = new Hashtable<>();
		allWorkflows = new Hashtable<>();
	}

	public static void addWorkflow(
		String name,
		String startTask,
		String endTask
	) {

		if (allWorkflows.isEmpty()) {
			Workflow workflow = new Workflow(
				name,
				allTasks.get(startTask),
				allTasks.get(endTask)
			);
			allWorkflows.put(name, workflow);
			EntityManager.workflow = workflow;
			SimulationManager.workflow = workflow;
		} else {
			Printer.errorPrint(
				Sources.EntityManager.name(),
				"Only one worflow is tested and implemented for use, this ontology contains more than one Workflow"
			);
		}
	}

	public static void addEvent(String name, int priority, int startTime) {
		if (!allEvents.containsKey(name)) {
			allEvents.put(
				name,
				new Event(
					name,
					priority,
					startTime,
					workflow.getStartTask().getName()
				)
			);
		} else {
			System.err.println(
				"Did not instantiate a second Event with the name :" +
				name +
				" \n" +
				"names have to be unique!!!!\n##################\n#################\n#################\n#################"
			);
		}
	}

	public static void addPerson(String name, List<String> qualifications) {
		if (!allPersons.containsKey(name)) {
			allPersons.put(name, new Person(name, qualifications));
		} else {
			System.err.println(
				"Did not instantiate a second Person with the name :" +
				name +
				" \n" +
				"names have to be unique!!!!\n##################\n#################\n#################\n#################"
			);
		}
	}

	public static void addQualification(String name) {
		if (!allQualifications.containsKey(name)) {
			allQualifications.put(name, new Qualifikation(name));
		} else {
			System.err.println(
				"Did not instantiate a second Qualification with the name :" +
				name +
				" \n" +
				"names have to be unique!!!!\n##################\n#################\n#################\n#################"
			);
		}
	}

	public static void addResourceType(String name, boolean unlimitedResource, int limitedNumber)
		throws IllegalStateException {
		if(allResourceTypes.containsKey(name)){
			Printer.errorPrint(Sources.EntityManager.name(), "there is already a ResourceType with name "+ name +" a second one will not be imported");
		}else{
			allResourceTypes.put(name, new ResourceType(name, unlimitedResource, limitedNumber));
		}

	}

	public static void addTask(
		String name,
		int timeNeeded,
		boolean isEndTask,
		boolean isStartTask,
		List<String> neededQualifications,
		int priority,
		List<String> taskHasPredecessors,
		List<String> followedByPlaceholder,
		List<String> resourcesPlaceholder
	) {
		if (!allTasks.containsKey(name)) {
			Task t = new Task(
				name,
				timeNeeded,
				isEndTask,
				isStartTask,
				neededQualifications,
				priority,
				taskHasPredecessors,
				followedByPlaceholder,
				resourcesPlaceholder
			);
			allTasks.put(name, t);
		}
	}

	/**
	 * to specify the tasks that follow on a specific task it is, when modelling OWL, only possible to link the names of the tasks
	 * this method finds the instances of the tasks according to the names in the TasksFollowedByPlaceholder List
	 * therefore this method must be called after all tasks have been imported
	 * this is done to allow for easy reference, else we would have to search for certain tasks everytime
	 */
	public static void findEntitiesForPlaceholders() {
		for (Task task : allTasks.values()) {
			if (!task.isEndTask()) {
				task.setFollowingTasks(
					setUpFollowerOrPredecessorTask(
						task.getFollowingTaskPlaceholder()
					)
				);
			}
			if (!task.isStartTask()) {
				task.setPredecessorTasks(
					setUpFollowerOrPredecessorTask(
						task.getPredecessorTaskPlaceHolder()
					)
				);
			}

			if (!task.getQualificationsNeededPlaceHolder().isEmpty()) {
				setUpQualifications(task);
			}
		}
	}

	private static List<Task> setUpFollowerOrPredecessorTask(
		List<String> placeHolders
	) {
		List<Task> followingOrPredecessorTasks = new LinkedList<>();
		for (String taskName : placeHolders) {
			Task existent = allTasks.get(taskName);
			if (existent == null) {
				Printer.errorPrint(
					Sources.EntityManager.name(),
					"Well that's weird, as following property directly links to a task, this error can only occur if the task" +
					"that is linked was not imported correctly, the name may be " +
					taskName
				);
				System.exit(-1);
			}
			followingOrPredecessorTasks.add(existent);
		}
		return followingOrPredecessorTasks;
	}

	private static void setUpQualifications(Task task) {
		List<String> strings = task.getQualificationsNeededPlaceHolder();
		Qualifikation[] qualificationNeeded = new Qualifikation[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			for (Qualifikation q : allQualifications.values()) {
				if (q.getName().equals(strings.get(i))) {
					qualificationNeeded[i] = q;
					break;
				}
			}
		}
		task.setQualificationsNeeded(List.of(qualificationNeeded));
	}
}
