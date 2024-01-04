package org.workflow;

import org.apache.jena.reasoner.rulesys.builtins.Print;
import org.workflow.Classes.*;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityManager {

    /**
     * every instance that was imported from the ontology is safed here
     */

    /**
     * all events imported from ontology
     */
    public static List<Event> allEvents;
    /**
     * all Persons imported from ontology
     */
    public static List<Person> allPersons;
    /**
     * all Resources imported from ontology
     */
    public static List<Resource> allResources;
    /**
     * all Tasks imported from ontology
     */
    public static List<Task> allTasks;
    /**
     * all Qualifications imported from ontology
     */
    public static List<Qualifikation> allQualifications;

    /**
     * following the idea that at some point there may be multiple workflows using similar persons and resources
     * not yet implemented, there is only on workflow and currently that one is linear with one start and one end task
     */
    public static List<Workflow> allWorkflows;

    /**
     * sorted tasks according to start-, end-, inner Task
     */
    public static Map<WorkflowTaskType, List<Task>> sortedTasks;

    public enum WorkflowTaskType{
        STARTTASK,
        INNERTASK,
        ENDTASK
    }

    public static void init(){
        allEvents= new LinkedList<>();
        allPersons= new LinkedList<>();
        allResources= new LinkedList<>();
        allTasks= new LinkedList<>();
        allQualifications= new LinkedList<>();
        allWorkflows= new LinkedList<>();
        sortedTasks= new HashMap<>(){
            {
             put(WorkflowTaskType.STARTTASK, new LinkedList<>());
             put(WorkflowTaskType.INNERTASK, new LinkedList<>());
             put(WorkflowTaskType.ENDTASK, new LinkedList<>());
            }
        };

    }

    public static void addEvent(String name, int priority, int startTime){
        allEvents.add(new Event(name, priority, startTime));
    }

    public static void addPerson(String name, List<String> qualifications){
        allPersons.add(new Person(name, qualifications));
    }
    public static void addResource(String name){
        allResources.add(new Resource(name));
    }

    public static void addQualification(String name){
        allQualifications.add(new Qualifikation(name));
    }

    public static void addTask(String name, int timeNeeded, boolean isEndTask, boolean isStartTask
            , List<String> neededQualifications, int priority, List<String> followedByPlaceholder, List<String> resourcesPlaceholder){


        Task t= new Task(name,
                timeNeeded,
                isEndTask,
                isStartTask,
                neededQualifications,
                priority,
                followedByPlaceholder,
                resourcesPlaceholder
        );
        allTasks.add(t);
    }

    /**
     * to specify the tasks that follow on a specific task it is, when modelling OWL, only possible to link the names of the tasks
     * this method finds the instances of the tasks according to the names in the TasksFollowedByPlaceholder List
     * therefore this method must be called after all tasks have been imported
     * this is done to allow for easy reference, else we would have to search for certain tasks everytime
     */
    public static void setUpTasksFollowedBy(){
        System.out.println("\n------------SET UP TASKS FOLLOWED BY------------\n");

        for(Task t1: allTasks){

            if(!t1.isEndTask()){
                Printer.print(Sources.EntityManager.name(), "setting up following task Links for Task with name "+ t1.getName());

                List<Task> followingTasks= new LinkedList<>();
                for(String taskName: t1.getFollowingTaskPlaceholder()){
                    for(Task t2: allTasks){
                        if(t2.getName().equals(taskName)){
                            followingTasks.add(t2);
                        }
                    }
                }
                t1.setFollowingTasks(followingTasks);
                StringBuilder builder= new StringBuilder();

                t1.getFollowingTaskPlaceholder().forEach(builder::append);
                Printer.print(Sources.EntityManager.name(),"Have set following tasks to a number of "+ t1.getFollowingTasks().size() +" ||| String names were: "+ builder);
                System.out.println();
                builder.delete(0, builder.length());
                t1.getFollowingTasks().forEach(e-> builder.append(e.getName()).append("||"));
                Printer.print(Sources.EntityManager.name(), "Found tasks with names: "+ builder);

                System.out.println();
            }

        }
        sortTasksAndFindWorkflows();

    }


    /**
     * sorting the tasks may help building the workflows
     * this is yet not implemented
     */
    private static void sortTasksAndFindWorkflows(){
        allTasks.forEach(task->{
            if(task.isStartTask())
                addToSortedTasks(WorkflowTaskType.STARTTASK, task);
            else if(task.isEndTask())
                addToSortedTasks(WorkflowTaskType.ENDTASK, task);
            else
                addToSortedTasks(WorkflowTaskType.INNERTASK, task);
        });

        /*
        List<Task> startTasks= sortedTasks.get(WorkflowTaskType.STARTTASK);
        if (startTasks.isEmpty()){
            System.out.println("No start task found, terminating...");
            System.exit(0);
        }
        if(startTasks.size()>1)
            System.out.println("found more than one startTask, this is currently not possible, only the first starttask will be considered, its workflow built and used in Sim");

        Task startTask= startTasks.get(0);
        Workflow workflow = new Workflow(startTasks.get(0));
        if(startTask.getFollowingTasks().isEmpty()){
            System.out.println("No following task for startTask with name "+ startTask.getName());
            System.out.println("Terminating.....");
            System.exit(0);
        }
        List<>
        for(Task t: startTask.getFollowingTasks()){

            if(t.isEndTask()){
                if(workflow.getEndTask() == null)
                    workflow.setEndTask(t);
                else {
                    System.out.println("Workflow with id " + workflow.getId() + " already has an end Task\n" +
                            "currently not able to handle this, therefore terminating....");
                    System.exit(0);
                }
            }else{
                workflow.addInnerTask(t);
            }

        }
        */



    }


    private static void addToSortedTasks(WorkflowTaskType workflowTaskType, Task t){
        List<Task> requestedList= sortedTasks.get(workflowTaskType);
        requestedList.add(t);
        sortedTasks.put(workflowTaskType, requestedList);
    }






}
