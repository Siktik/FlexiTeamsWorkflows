package org.workflow.Simulation;

import org.workflow.OntologyClasses.Event;
import org.workflow.OntologyClasses.Task;

public class TaskRunConcept {

	/**
	 * this class is used to link events to tasks
	 * these are created to plan executions are then used to be executed
	 * the thread safed here is supposed to run the task
	 * there are no multiple concurrent executions of tasks possible at the moment as this is not coded
	 */

	public Event event;
	public Thread t;
	public String taskName;

	public TaskRunConcept(Event e, String task) {
		this.event = e;
		this.taskName = task;
	}

	public void start() {
		Task ref = EntityManager.allTasks.get(taskName);
		ref.currentEvent = event;
		t = new Thread(ref::runSelf);
		t.start();
	}

	public Task getTask() {
		return EntityManager.allTasks.get(taskName);
	}
}
