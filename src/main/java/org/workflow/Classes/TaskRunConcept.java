package org.workflow.Classes;

import org.workflow.EntityManager;

public class TaskRunConcept {

	public Event event;
	public Thread t;
	public String taskName;

	TaskRunConcept(Event e, String task) {
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
