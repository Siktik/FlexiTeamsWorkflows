package org.workflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyTypes {

    /**
     *
     * if you do not know what you are doing here, don't change anything
     * this is a mapping to the properties in the Ontology which uses .owl exported in RDF/XML Syntax
     * with the help of this the importer works
     *
     * if you rename any of these String the Importer may fail!!
     * if you rename Strings here for any reason you need to make sure that the property within the Ontology
     * is named the exact same way!!
     */

    static String personName= "personHasName";
    static String eventName= "eventHasName";
    static String resourceName= "resourceHasName";
    static String qualificationName= "qualificationHasName";
    static String taskName= "taskHasName";
    static String resourceTypeName = "resourceTypeHasName";
    static String eventPriority= "eventPriority";
    static String taskPriority= "taskPriority";
    static String eventHasStartTime= "eventHasStartTime";
    static String taskHasTimeNeeded= "taskHasTimeNeeded";
    static String taskIsEndTask= "taskIsEndTask";
    static String taskIsStartTask= "taskIsStartTask";
    static String personHasQualification = "personHasQualification";
    static String taskNeedsQualification= "taskNeedsQualification";
    static String taskNeedsResource = "taskNeedsResource";
    static String taskIsFollowedBy= "taskIsFollowedBy";
    static String isOfResourceType= "isOfResourceType";
    static String unlimitedResource= "unlimited";



    public static Map<ClassTypes, List<String>> propertyRulesMap= new HashMap<>(){{
        put(ClassTypes.EVENT, Arrays.asList(eventName, eventPriority, eventHasStartTime));
        put(ClassTypes.TASK, Arrays.asList(taskName, taskIsEndTask, taskIsStartTask, taskPriority, taskNeedsQualification, taskHasTimeNeeded));
        put(ClassTypes.PERSON, Arrays.asList(personName));
        put(ClassTypes.RESOURCE, Arrays.asList(resourceName, isOfResourceType));
        put(ClassTypes.RESOURCETYPE, Arrays.asList(resourceTypeName));
        put(ClassTypes.QUALIFICATION, Arrays.asList(qualificationName));
        }
    };



}
