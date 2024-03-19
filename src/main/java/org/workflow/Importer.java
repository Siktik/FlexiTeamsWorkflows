package org.workflow;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.workflow.Classes.Event;
import org.workflow.Classes.ResourceType;
import org.workflow.Classes.Task;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Importer {

    private static String xsdPrefix;
    private static String rdfsPrefix;
    private static String owlPrefix;
    private static String rdfPrefix;
    /**
     * the prefix of every property in the ontology and therefore key for accessing any instance
     */
    private static String ontologyPrefix; //Todo set dynamically

    /**
     * the ontology as model with every property and detail
     */
    private static OntModel ontModel;
    /**
     * the ontology consisting of only the modelled classes, instances, properties
     */
    private static Model baseModel;

    /**
     * via the property name, which has to be assigned to every instance we can find every instance
     * instances that do not have a name property according to their type/class wont be imported, another reason everything may fail
     */
    private static Property eventName;
    private static Property qualificationName;
    private static Property taskName;
    private static Property personName;
    private static Property resourceTypeName;
    private static Property workflowName;


    public static void importOWL() {
        try {
            setUpModel();
        }catch(Exception e){
            System.err.println("Problems importing -> terminating");
            System.exit(0);
        }
        System.out.println("\n------------Importing Now------------\n");

        //do not change the order unless you really need to, may cause problems
        importQualifications();
        importPersons();
        importResourceTypes();
        importTasks();
        importWorkflow();
        importEvents();

        EntityManager.findEntitiesForPlaceholders();

        Printer.print(Sources.Importer.name(), "Done Importing, numbers are:" +
                "\nEvents: "+ EntityManager.allEvents.size()+
                "\nTasks: "+ EntityManager.allTasks.size()+
                "\nPersons: "+ EntityManager.allPersons.size()+
                "\nResourcesTypes: "+ EntityManager.allResources.keySet().size()+
                "\nResources: "+ EntityManager.allResources.values().stream().mapToLong(List::size).sum()+
                "\nQualifications: "+ EntityManager.allQualifications.size()
        );

        Scanner scanner = new Scanner(System.in);

        System.out.println("print Details for Entities? (Enter 'y' for Yes, 'n' for No)");

        while (true) {
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("y")) {
                Printer.printEntityDetails(true, null);
                /*System.out.println("print Details for all Entities type \"all\"? Else type \"E\"(Event) or \"T\"(Task), or\"R\"(Resource)" +
                        " or \"Q\"(Qualification) or \"RT\"(ResourceType) or \"Person\" (Enter 'y' for Yes, 'n' for No)");
                while(true){

                    input= scanner.nextLine().toLowerCase();
                    if(input.equals("all")){
                        Printer.printEntityDetails(true, null);
                        break;
                    }
                }*/
                break;
            } else if (input.equals("n")) {
                break;
            } else {
                // Invalid input, prompt the user again
                System.out.println("Invalid input. Please enter 'y' for Yes or 'n' for No.");
            }
        }
    }

    private static void setUpModel() throws Exception{

        String owlFilePath = "src/main/resources/experimental.rdf";
        ontModel= ModelFactory.createOntologyModel();
        InputStream inputStream = new FileInputStream(owlFilePath);
        ontModel.read(inputStream, null, "RDF/XML");
        Map<String,String> prefixes = ontModel.getNsPrefixMap();
        xsdPrefix= prefixes.get("xsd");
        rdfsPrefix= prefixes.get("rdfs");
        rdfPrefix= prefixes.get("rdf");
        owlPrefix= prefixes.get("owl");
        ontologyPrefix= prefixes.get("untitled-ontology-10");
        Printer.print(Sources.Importer.name(), "Used prefix is \n"+  ontologyPrefix);


        baseModel= ontModel.getBaseModel();
        qualificationName = baseModel.getProperty(ontologyPrefix+ PropertyTypes.qualificationName);
        eventName = baseModel.getProperty(ontologyPrefix+ PropertyTypes.eventName);
        taskName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.taskName);
        personName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.personName);
        resourceTypeName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.resourceTypeName);
        workflowName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.workflowHasName);

    }


    private static void importWorkflow(){
        ResIterator iterator= retrieveIterator(workflowName);
        Property endTaskIsProperty= ontModel.getProperty(ontologyPrefix+ PropertyTypes.isEndOfWorkflow);
        Property startTaskIsProperty= ontModel.getProperty(ontologyPrefix+ PropertyTypes.isStartOfWorkflow);


        while(iterator.hasNext()){
            Resource resource= iterator.nextResource();
            String name = resource.getProperty(workflowName).getObject().asLiteral().getString();
            Statement endTaskIsStatement= resource.getProperty(endTaskIsProperty);
            Statement startTaskIsStatement= resource.getProperty(startTaskIsProperty);
            String startTaskName="";
            String endTaskName="";

            if(endTaskIsStatement!= null){
                endTaskName= endTaskIsStatement.getResource().getProperty(taskName).getObject().asLiteral().getString();
            }else{
                Printer.errorPrint(Sources.Importer.name(), "No endTask specified for workflow "+ name+" terminating");
                System.exit(-1);
            }
            if(startTaskIsStatement!= null){
                startTaskName= startTaskIsStatement.getResource().getProperty(taskName).getObject().asLiteral().getString();
            }else{
                Printer.errorPrint(Sources.Importer.name(), "No startTask specified for workflow "+ name+" terminating");
                System.exit(-1);
            }


            EntityManager.addWorkflow(name, startTaskName, endTaskName);

        }
    }
    private static void importResourceTypes(){
        ResIterator iterator= retrieveIterator(resourceTypeName);
        Property unlimitedResources = baseModel.getProperty(ontologyPrefix+PropertyTypes.unlimitedResource);
        while(iterator.hasNext()){
            Resource resource= iterator.nextResource();
            Statement statement= resource.getProperty(resourceTypeName);

            String name = statement.getObject().asLiteral().getString();
            boolean unlimitedResource = false;
            Statement unlimitedResourceStatement= resource.getProperty(unlimitedResources);

            if(unlimitedResourceStatement!=null){
                unlimitedResource = unlimitedResourceStatement.getObject().asLiteral().getBoolean();
            }else{
                System.err.println("unlimited property not there");
            }

            EntityManager.addResourceType(name, unlimitedResource);

        }
    }


    /**
     * via the property qualificationName we find every instance that has this property
     * we iterate through them, pick the current instance from the iterator as Resource and would request other properties
     * if there were more
     * currently a qualification has only a name
     */
    private static void importQualifications(){

        ResIterator iterator= retrieveIterator(qualificationName);
        while(iterator.hasNext()){
            Resource resource= iterator.nextResource();
            Statement statement= resource.getProperty(qualificationName);

            if(statement != null){

                String name = statement.getObject().asLiteral().getString();
                if(name!=null){
                    EntityManager.addQualification(name);
                }

            }
        }

    }

    /**
     * via the property eventName we find every instance that has this property
     * we iterate through them, pick the current instance from the iterator as Resource and request other properties
     * an event also has a priority and a startTime, therefore we specify these properties using the baseModel
     * and ask for the values using the resource the iterator is currently pointing to
     * you have to be aware of the datatypes that are mapped along these properties, if you are not sure which it is, have a look in the ontology
     */
    private static void importEvents(){

        Property eventHasPriority= baseModel.getProperty(ontologyPrefix+PropertyTypes.eventPriority);
        Property eventHasStartTime= baseModel.getProperty(ontologyPrefix+PropertyTypes.eventHasStartTime);
        ResIterator iterator= retrieveIterator(eventName);

        while(iterator.hasNext()){

            Resource resource= iterator.nextResource();
            String name = resource.getProperty(eventName).getObject().asLiteral().getString();

            int priority;
            int startTime;
            try {
                priority = resource.getProperty(eventHasPriority).getObject().asLiteral().getInt();
                startTime = resource.getProperty(eventHasStartTime).getObject().asLiteral().getInt();
            }catch(NullPointerException e){
                Printer.errorPrint(Sources.Importer.name(), "problems importing the event "+ name+"\n" +
                        "make sure this event has the must have properties\n" + Printer.printPropertyRules(ClassTypes.EVENT));
                continue;
            }


            EntityManager.addEvent(name, priority, startTime);
        }

    }

    /**
     * via the property taskName we find every instance that has this property
     * we iterate through them, pick the current instance from the iterator as Resource and request other properties
     * an task has:
     * - priority
     * - taskTimeNeeded
     * - taskNeedsQualification
     * - taskIsEndTask
     * - taskIsStartTask
     * - taskIsFollowedBy
     * - taskNeedsResource
     * the properties taskNeedQualification, taskIsFollowedBy and taskNeedsResource may be used multiple times
     * therefore we have to look for all statements using these properties, this is done in the while loops
     * the other properties should be single values if modelled correctly
     * if modelled wrong and there were multiple values for the priority f.e. only one of them will be used by implementation
     */
    private static void importTasks(){

        Property taskHasPriority= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskPriority);
        Property taskTimeNeeded= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskHasTimeNeeded);
        Property taskNeedsQualification= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskNeedsQualification);
        Property taskIsEndTask= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskIsEndTask);
        Property taskIsStartTask= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskIsStartTask);
        Property taskIsFollowedBy= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskIsFollowedBy);
        Property taskHasPredecessor= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskHasPredecessor);
        Property taskNeedsResource= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskNeedsResource);
        ResIterator iterator= retrieveIterator(taskName);

        while(iterator.hasNext()){

            Resource resource= iterator.nextResource();

            String name = resource.getProperty(taskName).getObject().asLiteral().getString();
            int priority;
            int timeNeeded;
            boolean startTask;
            boolean endTask;

            List<String> taskIsFollowedByPlaceholder= new LinkedList<>();
            List<String> taskHasPredecessors= new LinkedList<>();

            try {
                priority = resource.getProperty(taskHasPriority).getObject().asLiteral().getInt();
                timeNeeded = resource.getProperty(taskTimeNeeded).getObject().asLiteral().getInt();
            }catch(NullPointerException e){
                Printer.errorPrint(Sources.Importer.name(), "problems importing the Task "+ name+"\n" +
                        "make sure this Task has the must have properties\n" + Printer.printPropertyRules(ClassTypes.TASK));
                continue;
            }
            try {
                endTask = resource.getProperty(taskIsEndTask).getObject().asLiteral().getBoolean();
            }catch(NullPointerException e){
                endTask = false;
            }
            try {
                startTask = resource.getProperty(taskIsStartTask).getObject().asLiteral().getBoolean();
            }catch(NullPointerException e){
                startTask = false;
            }

            // if no end Task, there are one or more tasks to follow
            if(!endTask){

                try {
                    StmtIterator iter = resource.listProperties(taskIsFollowedBy);

                    while (iter.hasNext()) {

                        Statement st = iter.nextStatement();
                        taskIsFollowedByPlaceholder.add(st.getObject().asResource().getProperty(taskName).getObject().asLiteral().getString());
                        //System.out.println(name+" is followed by "+st.getObject().asResource().getProperty(taskHasName).getObject().asLiteral().getString());
                    }
                }catch(NullPointerException e){
                    Printer.errorPrint(Sources.Importer.name(), "problems importing the Task "+ name+"\n" +
                            "there are no predecessor tasks linked fo a task that is not a start task");
                    System.exit(-1);
                }
            }
            if(!startTask){
                try {
                    StmtIterator iter = resource.listProperties(taskHasPredecessor);

                    while (iter.hasNext()) {

                        Statement st = iter.nextStatement();
                        taskHasPredecessors.add(st.getObject().asResource().getProperty(taskName).getObject().asLiteral().getString());
                        //System.out.println(name+" is followed by "+st.getObject().asResource().getProperty(taskHasName).getObject().asLiteral().getString());
                    }
                }catch(NullPointerException e){
                    Printer.errorPrint(Sources.Importer.name(), "problems importing the Task "+ name+"\n" +
                            "there are no predecessor tasks linked fo a task that is not a start task");
                    System.exit(-1);
                }
            }

            //importing all qualifications needed for the task
            List<String> qualificationsNeeded= new LinkedList<>();
            StmtIterator iter= resource.listProperties(taskNeedsQualification);
            while(iter.hasNext()){

                Statement st= iter.nextStatement();
                qualificationsNeeded.add(st.getObject().asResource().getProperty(qualificationName).getObject().asLiteral().getString());

            }

            //importing all resources needed for the task
            List<String> resourcesPlaceholder= new LinkedList<>();
            iter= resource.listProperties(taskNeedsResource);
            while(iter.hasNext()){

                Statement st= iter.nextStatement();
                resourcesPlaceholder
                        .add(
                                st.getObject().asResource()// this is the Resource, now get name
                                        .getProperty(resourceTypeName)
                                        .getObject()
                                        .asLiteral()
                                        .getString());

            }
            EntityManager.addTask(name, timeNeeded, endTask, startTask, qualificationsNeeded, priority,taskHasPredecessors, taskIsFollowedByPlaceholder, resourcesPlaceholder);
        }
    }



    /**
     * this follows the concept of the other import methods, look them up for explanation
     */
    private static void importPersons(){
        Property hasQualifications= baseModel.getProperty(ontologyPrefix+ PropertyTypes.personHasQualification);

        ResIterator iterator= retrieveIterator(personName);
        while(iterator.hasNext()){
            Resource resource = iterator.nextResource();
            String name = resource.getProperty(personName).getObject().asLiteral().getString();
            List<String> qualificationsNeeded= new LinkedList<>();
            StmtIterator iter= resource.listProperties(hasQualifications);
            while(iter.hasNext()){
                Statement st= iter.nextStatement();
                qualificationsNeeded
                        .add(
                                st.getObject().asResource() // this is the qualification, now get name
                                        .getProperty(qualificationName)
                                        .getObject()
                                        .asLiteral()
                                        .getString()
                        );
            }
            EntityManager.addPerson(name, qualificationsNeeded);
        }

    }

    private static ResIterator retrieveIterator(Property property){
        return baseModel.listResourcesWithProperty(property);
    }

}
