package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;
import org.workflow.EntityManager;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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


    public String startTask;







    public Event(String name, int priority, int startTime, String startTask){
        this.id= idCounter++;
        this.name=name;
        this.priority= priority;
        this.startTime=startTime;
        this.startTask= startTask;
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
