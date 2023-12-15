package org.workflow.Classes;

import org.workflow.TimeManager;

import java.sql.Time;
import java.util.LinkedList;
import java.util.List;

public class Task {

    private static int idCounter=0;
    private int id;
    private String name;
    private int timeNeeded;
    private int priority;
    private List<String> qualificationsNeeded;

    private boolean isStartTask;
    private boolean isEndTask;
    private List<String> followingTaskPlaceholder; // placeholder to link the follower task when it has been instantiated
    private List<Task> followingTasks; // can only be set when all Tasks have been instantiated
    private List<String> resourcesPlaceholder;
    public Task(String name, int timeNeeded, boolean isEndTask, boolean isStartTask,
                List<String> qualificationsNeeded, int priority, List<String> followingTaskPlaceholder, List<String> resourcesPlaceholder){
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



    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        Task.idCounter = idCounter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimeNeeded() {
        return timeNeeded;
    }

    public void setTimeNeeded(int timeNeeded) {
        this.timeNeeded = timeNeeded;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<String> getQualificationsNeeded() {
        return qualificationsNeeded;
    }

    public void setQualificationsNeeded(List<String> qualificationsNeeded) {
        this.qualificationsNeeded = qualificationsNeeded;
    }

    public boolean isStartTask() {
        return isStartTask;
    }

    public void setStartTask(boolean startTask) {
        isStartTask = startTask;
    }

    public boolean isEndTask() {
        return isEndTask;
    }

    public void setEndTask(boolean endTask) {
        isEndTask = endTask;
    }

    public List<String> getFollowingTaskPlaceholder() {
        return followingTaskPlaceholder;
    }

    public void setFollowingTaskPlaceholder(List<String> followingTaskPlaceholder) {
        this.followingTaskPlaceholder = followingTaskPlaceholder;
    }

    public List<Task> getFollowingTasks() {
        return followingTasks;
    }

    public void setFollowingTasks(List<Task> followingTasks) {
        this.followingTasks = followingTasks;
    }
}
