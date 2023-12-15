package org.workflow;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;

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
    private static String ontologyPrefix; //Todo set dynamically

    private static OntModel ontModel;
    private static Model baseModel;

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
        importQualifications();
        importPersons();
        importResources();
        importEvents();
        importTasks();
        EntityManager.setUpTasksFollowedBy();


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
        System.out.println("Used prefix is \n"+  ontologyPrefix);


        baseModel= ontModel.getBaseModel();
        qualificationName = baseModel.getProperty(ontologyPrefix+ PropertyTypes.qualificationName);
        eventName = baseModel.getProperty(ontologyPrefix+ PropertyTypes.eventName);
        taskName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.taskName);
        personName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.personName);
        resourceName= baseModel.getProperty(ontologyPrefix+ PropertyTypes.resourceName);


    }

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

            if(!endTask){

                startTask =  resource.getProperty(taskIsStartTask).getObject().asLiteral().getBoolean();
                StmtIterator iter = resource.listProperties(taskIsFollowedBy);

                while(iter.hasNext()){

                    Statement st= iter.nextStatement();
                    taskIsFollowedByPlaceholder.add(st.getObject().asResource().getProperty(taskName).getObject().asLiteral().getString());
                    //System.out.println(name+" is followed by "+st.getObject().asResource().getProperty(taskHasName).getObject().asLiteral().getString());
                }
            }

            List<String> qualificationsNeeded= new LinkedList<>();
            StmtIterator iter= resource.listProperties(taskNeedsQualification);

            while(iter.hasNext()){

                Statement st= iter.nextStatement();
                qualificationsNeeded.add(st.getObject().asResource().getProperty(qualificationName).getObject().asLiteral().getString());

            }

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

    private static void importResources(){

        ResIterator iterator= retrieveIterator(resourceName);
        while(iterator.hasNext()){
            Resource resource= iterator.nextResource();
            String name= resource.getProperty(resourceName).getObject().asLiteral().getString();
            EntityManager.addResource(name);

        }

    }

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
