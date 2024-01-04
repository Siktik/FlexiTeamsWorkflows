package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;
import org.workflow.TimeManager;

import java.sql.Time;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
public class Task {

    /**
     * this represents a state in a workflow where some work is done using resources, person with qualifications
     * or none of those
     */

    private static int idCounter=0;
    private int id;
    /**
     * the name of the task
     */
    private String name;
    /**
     * the time that the task needs to get finished in seconds from its start
     */
    private int timeNeeded;
    /**
     * may be useful in the future to decide which task should be executed if two tasks are on the same level and no choice
     * for one of the tasks could have been made
     */
    private int priority;
    /**
     * qualifications needed for this task
     * if qualifications are missing the idea may be to wait until qualification are ready again
     */
    private List<String> qualificationsNeeded;
    /**
     * whether this task is an startTask, meaning that this task does not follow on any other task
     */
    private boolean isStartTask;
    /**
     * whether this task is an endTask, meaning that no other taks will follow on this task
     */
    private boolean isEndTask;
    /**
     * this contains the names of the tasks that follow on this task, the linkage to the tasks is made when importing the Ontology
     * yet it is not clear how it will be implemented which tasks will be chosen if multiple tasks follow on this task
     */
    private List<String> followingTaskPlaceholder;
    /**
     * here the tasks corresponding to the placeholders are linked in
     */
    private List<Task> followingTasks; // can only be set when all Tasks have been instantiated
    /**
     * this contains the names of the resources that are needed to execute this task, the linkage to the tasks is made when importing the Ontology
     */
    private List<String> resourcesPlaceholder;


    public Task(String name, int timeNeeded, boolean isEndTask, boolean isStartTask,
                List<String> qualificationsNeeded, int priority, List<String> followingTaskPlaceholder,
                List<String> resourcesPlaceholder){
        this.id= idCounter++;
        this.qualificationsNeeded= qualificationsNeeded;
        this.followingTaskPlaceholder= followingTaskPlaceholder;
        this.followingTasks= new LinkedList<>();
        this.name=name;
        this.timeNeeded= timeNeeded;
        this.isEndTask= isEndTask;
        this.isStartTask=isStartTask;
        this.priority=priority;
        this.resourcesPlaceholder= resourcesPlaceholder;
        System.out.println(this);
    }
    public int start;

    /**
     * in this method a task executes itself, currently the execution starts when the workflow class
     * to which this task belongs calls this method
     * it runs for the time in seconds that was given by the ontology for this task
     * currently if the time is over and the task therefore ends, the thread is interrupted that runs this method
     * by this the workflow class gets knowledge that this task is done and will move over to the next task
     *
     * if there will be work done during this task it may be done via this method
     */
    public synchronized void runSelf(){
        start= TimeManager.getSimTime();
        int lastOutput = start;
        System.out.println("starting Task "+ name);
        System.out.println(name+" needs "+ timeNeeded +" seconds starting at simTime "+ start);

        while(TimeManager.getSimTime()- start < timeNeeded){

            /*if(TimeManager.getSimTime()-lastOutput >2){
                System.out.println("Update On Task "+ name+ " still running");
                lastOutput= TimeManager.getSimTime();
            }*/

        }
        Thread.currentThread().interrupt();


    }



    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timeNeeded=" + timeNeeded +
                ", priority=" + priority +
                ", qualificationsNeeded=" + qualificationsNeeded +
                ", isStartTask=" + isStartTask +
                ", isEndTask=" + isEndTask +
                ", followingTaskPlaceholder=" + followingTaskPlaceholder +
                ", followingTasks=" + followingTasks +
                ", resourcesPlaceholder=" + resourcesPlaceholder +
                '}';
    }
}
