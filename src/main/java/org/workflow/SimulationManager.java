package org.workflow;

import org.workflow.Classes.Event;
import org.workflow.Classes.Workflow;

import java.util.Comparator;
import java.util.PriorityQueue;

public class SimulationManager {
    
    static PriorityQueue<Event> eventQueue;
    //currently only one Workflow
    public static Workflow workflow;
    
    public static void init(){
        eventQueue= new PriorityQueue<>(compareEventsByTime);
        eventQueue.addAll(EntityManager.allEvents.values());
        workflow.initTasksMap();

    }

    public static void runSimulation(){

        TimeManager.startTimeManager();
        workflow.startWorkflow();

        while(!eventQueue.isEmpty()){
            Event e = null;

            if(eventQueue.peek().getStartTime() < TimeManager.getSimTime()) {
                e = eventQueue.poll();
                workflow.addEventToQueue(e);
            }


        }
        System.out.println("Worked all Events, waiting for tasks to terminate then its all done");


    }


    
    private static Comparator<Event> compareEventsByTime = new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getStartTime()<o2.getStartTime())
                return -1;
            else if(o2.getStartTime()<o1.getStartTime())
                return 1;
            return 0;
        }
    };
    
    
}
