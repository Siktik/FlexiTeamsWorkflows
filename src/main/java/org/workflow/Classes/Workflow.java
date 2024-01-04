package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;
import org.workflow.TimeManager;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

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
        Printer.print(Sources.Workflow.name(), toString());
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
    int outPutLimiterTaskWaiting= TimeManager.getSimTime();
    int outPutLimiterEventWaiting= TimeManager.getSimTime();

    /**
     * the main of a Workflow, don't change the order of the method calls unless you know what the effect would be
     */
    private void runWorkflow(){
        working = true;

        while(working) {

               // don't change order
                checkTaskProgression();
                checkTasksWaitingForFurtherExecution();
                checkIncomingEvents();


                if (TimeManager.getSimTime() > 180)
                    working = false;
        }
        Thread.currentThread().interrupt();


    }

    /**
     * every task that is currently running is saved with the thread who is running the task and the event that lead to execution of the task
     * within the Map tasksThatAreCurrentlyBeingWorkedOn
     * if a task end it terminates its thread and this method will find the task then as it checks for threads that have been interrupted
     * we save the TaskRunConcept to a list which will remove this and other taskRunConcepts that may be finished from the map after we've done iterating over it
     * we check for the finished task if it is a endTask, then we just print it, else we add it to a List that will
     * checked after this method to calculate the event that will take place in the next task
     * by this it is possible to choose between two or more events that are waiting for the same task to be executed with regards to priority or time in Sim
     */
    private void checkTaskProgression(){
        if(!tasksThatAreCurrentlyBeingWorkedOn.isEmpty()) {
            List<String> toRemove= new LinkedList<>();
            Iterator<String> iterator = tasksThatAreCurrentlyBeingWorkedOn.keySet().iterator();

            while (iterator.hasNext()) {

                String key = iterator.next();
                TaskRunConcept taskRunConcept = tasksThatAreCurrentlyBeingWorkedOn.get(key);
                if (taskRunConcept.t.isInterrupted()) {
                    toRemove.add(key);
                    Task task = taskRunConcept.task;
                    if (task.isEndTask()) {
                        Printer.print(Sources.Workflow.name(), "Finished a workflow for Event " + taskRunConcept.e.getName() );
                    } else {

                        Event e= taskRunConcept.e;
                        e.nextTask= task.getFollowingTasks().get(0).getName();
                        // conditional task
                        needsFurtherExecution.add(taskRunConcept.e);
                        Printer.print(Sources.Workflow.name(), taskRunConcept.e.getName()+"finished task " + task.getName() );

                    }
                }

            }
            if(!toRemove.isEmpty()){
                toRemove.forEach(e-> tasksThatAreCurrentlyBeingWorkedOn.remove(e));
            }

        }
    }

    /**
     * this checks the list of Events that are waiting for further execution
     * if an event gets scheduled to this workflow and the start task is already running with another event, the event gets added to this list
     * also events that just finished their execution on a task get added to this list
     * currently the list is sorted by a comparator such that events with a high priority will be looked at first
     * if the task that this event should be executed on is free, a TaskRunConcept is built, added to the Map and the Thread running the task is started
     *
     * by now this means that if there are two events waiting for the same task and one Event is in there way longer, cause it may have failed future executions on the task
     * the event with the higher priority will be chosen first
     */
    private void checkTasksWaitingForFurtherExecution(){
        if(!needsFurtherExecution.isEmpty()){
            List<Event> toRemove= new LinkedList<>();
            for (Event e: needsFurtherExecution){
                if(!tasksThatAreCurrentlyBeingWorkedOn.containsKey(e.nextTask)){

                    toRemove.add(e);
                    Task nextTask= quickAccess.get(e.nextTask);
                    Printer.print(Sources.Workflow.name(), e.getName()+" continuing work with task "+ nextTask.getName());
                    TaskRunConcept nextTaskRunConcept = new TaskRunConcept(e, nextTask);
                    tasksThatAreCurrentlyBeingWorkedOn.put(nextTask.getName(), nextTaskRunConcept);
                    nextTaskRunConcept.start();
                }
            }
            needsFurtherExecution.removeAll(toRemove);
        }
    }

    /**
     * new Events are scheduled by the SImulation Manager towars this Workflow
     * currently there is only one Workflow, in future the Sim manager may schedule Events according to the workflows they should be worked on
     *
     * if an event has eben scheduled, the method checks if the startTask for this event is free
     * if free, the taskRunConcept is instantiated and runned
     * if not free, the event is added to the
     *
     */
    private void checkIncomingEvents(){
        if (!queuedEvents.isEmpty()) {
            Event e = queuedEvents.poll();
            if (!tasksThatAreCurrentlyBeingWorkedOn.containsKey(startTask.getName())) {
                Printer.print(Sources.Workflow.name(), e.getName()+ "Found event and start Task free");

                TaskRunConcept starterTaskRunConcept = new TaskRunConcept(e, startTask);
                tasksThatAreCurrentlyBeingWorkedOn.put(startTask.getName(), starterTaskRunConcept);
                starterTaskRunConcept.start();

            } else {
                Printer.print(Sources.Workflow.name(), "Start task is not free for new Event, adding to needsFurtherExecutionList");
                needsFurtherExecution.add(e);
            }

        } else {
            if (TimeManager.getSimTime() - outPutLimiterEventWaiting > 5) {
                System.out.println("waiting on Events to come");
                outPutLimiterEventWaiting = TimeManager.getSimTime();
            }
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
