package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;
import org.workflow.TimeManager;

import java.util.*;

@Getter

public class Workflow {

    // at this time workflows can have only one start Task and one endTask
    // moreover the simulator only works with linear workflows
    @Setter
    private Task startTask;
    @Setter
    private Task endTask;
    @Setter
    private List<Task> innerTasks;
    private int id;
    private Map<String, Task> quickAccess;

    private static int idCounter = 0 ;

    public Workflow(Task startTask){
        this.id= idCounter++;
        queuedEvents = new PriorityQueue<>(comparingEvents);
        this.startTask= startTask;
        System.out.println(this);
    }

    public Workflow(Task startTask, Task endTask, List<Task> innerTasks){
        this.id= idCounter++;
        queuedEvents = new PriorityQueue<>(comparingEvents);
        needsFurtherExecution= new PriorityQueue<>(comparingEvents);
        quickAccess = new HashMap<>();
        quickAccess.put(startTask.getName(), startTask);
        quickAccess.put(endTask.getName(), endTask);
        innerTasks.forEach(e-> quickAccess.put(e.getName(), e));
        this.startTask= startTask;
        this.endTask= endTask;
        this.innerTasks= innerTasks;
        System.out.println(this);
        tasksThatAreCurrentlyBeingWorkedOn= new Hashtable<>();
        Thread eventListener= new Thread(this::runWorkflow);
        eventListener.start();
    }

    private Map<String, TaskRunConcept> tasksThatAreCurrentlyBeingWorkedOn;

    public void addInnerTask(Task t){
        innerTasks.add(t);
    }

    @Getter
    public boolean working;

    public void addEventToQueue(Event e){
        queuedEvents.add(e);
    }


    private PriorityQueue<Event> needsFurtherExecution;

    private void runWorkflow(){
        working = true;
        int outPutLimiterTaskWaiting= TimeManager.getSimTime();
        int outPutLimiterEventWaiting= TimeManager.getSimTime();

        while(working) {


                if(!tasksThatAreCurrentlyBeingWorkedOn.isEmpty()) {
                    List<String> toRemove= new LinkedList<>();
                    List<TaskRunConcept> toAdd= new LinkedList<>();
                    Iterator<String> iterator = tasksThatAreCurrentlyBeingWorkedOn.keySet().iterator();

                    while (iterator.hasNext()) {

                            String key = iterator.next();
                            TaskRunConcept taskRunConcept = tasksThatAreCurrentlyBeingWorkedOn.get(key);
                            if (taskRunConcept.t.isInterrupted()) {
                                toRemove.add(key);
                                Task task = taskRunConcept.task;
                                if (task.isEndTask()) {
                                    System.out.println("Finished a workflow for Event " + taskRunConcept.e.getName());
                                } else {

                                    Event e= taskRunConcept.e;
                                    e.nextTask= task.getFollowingTasks().get(0).getName();
                                    // conditional task
                                    needsFurtherExecution.add(taskRunConcept.e);
                                    System.out.println(taskRunConcept.e.getName()+"finished task " + task.getName());
                                    //

                                    //
                                }
                            }

                    }
                    if(!toRemove.isEmpty()){
                        toRemove.forEach(e-> tasksThatAreCurrentlyBeingWorkedOn.remove(e));
                    }
                    if(!toAdd.isEmpty()){
                        toAdd.forEach(e-> tasksThatAreCurrentlyBeingWorkedOn.put(e.task.getName(), e));
                    }

                }
                if(!needsFurtherExecution.isEmpty()){
                    List<Event> toRemove= new LinkedList<>();
                    for (Event e: needsFurtherExecution){
                        if(!tasksThatAreCurrentlyBeingWorkedOn.containsKey(e.nextTask)){

                            toRemove.add(e);
                            Task nextTask= quickAccess.get(e.nextTask);
                            System.out.println(e.getName()+" continuing work with task "+ nextTask.getName());
                            TaskRunConcept nextTaskRunConcept = new TaskRunConcept(e, nextTask);
                            tasksThatAreCurrentlyBeingWorkedOn.put(nextTask.getName(), nextTaskRunConcept);
                            nextTaskRunConcept.start();
                        }
                    }
                    needsFurtherExecution.removeAll(toRemove);
                }

                if (!queuedEvents.isEmpty()) {

                    if (!tasksThatAreCurrentlyBeingWorkedOn.containsKey(startTask.getName())) {
                        Event e = queuedEvents.poll();
                        System.out.println(e.getName()+ "Found event and start Task free" );

                        TaskRunConcept starterTaskRunConcept = new TaskRunConcept(e, startTask);
                        tasksThatAreCurrentlyBeingWorkedOn.put(startTask.getName(), starterTaskRunConcept);
                        starterTaskRunConcept.start();

                    } else {
                        if (TimeManager.getSimTime() - outPutLimiterTaskWaiting > 4) {
                            outPutLimiterTaskWaiting = TimeManager.getSimTime();
                            System.out.println("Start task is not free for new Event");
                            continue;
                        }
                    }

                } else {
                    if (TimeManager.getSimTime() - outPutLimiterEventWaiting > 5) {
                        System.out.println("waiting on Events to come");
                        outPutLimiterEventWaiting = TimeManager.getSimTime();
                    }
                }
                if (TimeManager.getSimTime() > 90)
                    Thread.currentThread().interrupt();

            }


    }

    private PriorityQueue<Event> queuedEvents;

    private Comparator<Event> comparingEvents= new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getPriority()> o2.getPriority())
                return -1;
            if(o2.getPriority()> o1.getPriority())
                return 1;
            return 0;

        }
    };


    @Override
    public String toString() {
        return "Workflow{" +
                "startTask=" + startTask +
                ", endTask=" + endTask +
                ", innerTasks=" + innerTasks +
                ", id=" + id +
                ", working=" + working +
                ", queuedEvents=" + queuedEvents +
                ", comparingEvents=" + comparingEvents +
                '}';
    }

    /**
     * a helper class to map currently running tasks, the thread that is executing this task and the event that is currently
     * at this task in one object
     */
    private static class TaskRunConcept{

        public Event e;
        public Thread t;
        public Task task;

        TaskRunConcept(Event e, Task task){
            this.e=e;
            this.task=task;
        }

        public void start(){
            t= new Thread(task::runSelf);
            t.start();
        }


    }

}
