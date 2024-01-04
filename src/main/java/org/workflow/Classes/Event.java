package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
    /**
     * this describes an occasion by which a workflow is started on its start task and then is worked through
     */


    private static int idCounter=0;
    private int id;
    /**
     * the name of the event
     */
    private String name;
    /**
     * the priority of the event, if multiple events wait to begin with the start task, the one with the highest priority is chosen
     */
    private int priority;
    /**
     * the time in seconds after sim start where this event will occur
     */
    private int startTime;
    /**
     * a helper set by the task the event is currently in as the task knows best which task may follow
     * this is currently easy as only linear workflows are represented
     */
    public String nextTask;

    public Event(String name, int priority, int startTime){
        this.id= idCounter++;
        this.name=name;
        this.priority= priority;
        this.startTime=startTime;
        nextTask= "StartTask";
        System.out.println(this);
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
