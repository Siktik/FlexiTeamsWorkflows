package org.workflow.Classes;


public class Event {

    private static int idCounter=0;
    private int id;
    private String name;
    private int priority;
    private int startTime;

    public String nextTask;
    public  Event(String name, int priority, int startTime){
        this.id= idCounter++;
        this.name=name;
        this.priority= priority;
        this.startTime=startTime;
        nextTask= "StartTask";
        System.out.println(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", startTime=" + startTime +
                '}';
    }
}
