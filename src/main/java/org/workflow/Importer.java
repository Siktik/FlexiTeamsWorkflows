package org.workflow;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
     */
    private static Property eventName;
    private static Property qualificationName;
    private static Property taskName;
    private static Property resourceName;
    private static Property personName;


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
        importResources();
        importEvents();
        importTasks();
        EntityManager.setUpTasksFollowedBy();

        Printer.print(Sources.Importer.name(), "Done Importing, numbers are:" +
                "\nEvents: "+ EntityManager.allEvents.size()+
                "\nTasks: "+ EntityManager.allTasks.size()+
                "\nPersons: "+ EntityManager.allPersons.size()+
                "\nResources: "+ EntityManager.allResources.size()+
                "\nQualifications: "+ EntityManager.allQualifications.size());

    }

    private static void setUpModel() throws Exception{

        String owlFilePath = "C:/Users/maxsp/IdeaProjects/Workflows/src/main/resources/Workflows.rdf";
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
        resourceName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.resourceName);

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
            int priority = resource.getProperty(eventHasPriority).getObject().asLiteral().getInt();
            int startTime = resource.getProperty(eventHasStartTime).getObject().asLiteral().getInt();

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
        Property taskNeedsResource= baseModel.getProperty(ontologyPrefix+PropertyTypes.taskNeedsResource);
        ResIterator iterator= retrieveIterator(taskName);

        while(iterator.hasNext()){

            Resource resource= iterator.nextResource();

            String name = resource.getProperty(taskName).getObject().asLiteral().getString();
            int priority = resource.getProperty(taskHasPriority).getObject().asLiteral().getInt();
            int timeNeeded = resource.getProperty(taskTimeNeeded).getObject().asLiteral().getInt();
            boolean startTask = false;
            boolean endTask = resource.getProperty(taskIsEndTask).getObject().asLiteral().getBoolean();
            List<String> taskIsFollowedByPlaceholder= new LinkedList<>();

            // if no end Task, there are one or more tasks to follow
            if(!endTask){

                startTask =  resource.getProperty(taskIsStartTask).getObject().asLiteral().getBoolean();
                StmtIterator iter = resource.listProperties(taskIsFollowedBy);

                while(iter.hasNext()){

                    Statement st= iter.nextStatement();
                    taskIsFollowedByPlaceholder.add(st.getObject().asResource().getProperty(taskName).getObject().asLiteral().getString());
                    //System.out.println(name+" is followed by "+st.getObject().asResource().getProperty(taskHasName).getObject().asLiteral().getString());
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
                                        .getProperty(resourceName)
                                        .getObject()
                                        .asLiteral()
                                        .getString());

            }
            EntityManager.addTask(name, timeNeeded, endTask, startTask, qualificationsNeeded, priority, taskIsFollowedByPlaceholder, resourcesPlaceholder);
        }
    }

    /**
     * this follows the concept of the other import methods, look them up for explanation
     */
    private static void importResources(){

        ResIterator iterator= retrieveIterator(resourceName);
        while(iterator.hasNext()){
            Resource resource= iterator.nextResource();
            String name= resource.getProperty(resourceName).getObject().asLiteral().getString();
            EntityManager.addResource(name);

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
