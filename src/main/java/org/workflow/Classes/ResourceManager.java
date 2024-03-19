package org.workflow.Classes;

import com.google.common.collect.ArrayListMultimap;

import com.google.common.collect.Multimap;
import org.workflow.EntityManager;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import java.util.*;

public class ResourceManager {

    private static Hashtable<String, ResourceType> allTypes;
    public static Hashtable<String, List<Resource>> unlimitedResources;
    public static Hashtable<String, List<Resource>> availableLimitedResources;


    public static boolean checkResourceAssertionPossible(TaskRunConcept trc){
        Task task= trc.getTask();
        Multimap<String, Resource> multiMap= ArrayListMultimap.create();
        List<String> resourceNamesUnlimited= new LinkedList<>();
        List<String> resourceNamesLimited= new LinkedList<>();
        Printer.print(trc.event.getName(),"checking Resource Assertion");

        /**
         * if following stream would be done when importing, computing power is saved easily
         */
        task.getResourcesPlaceholder().forEach(type->{
            if(EntityManager.allResourceTypes.get(type).isUnlimited())
                resourceNamesUnlimited.add(type);
            else
                resourceNamesLimited.add(type);
        });

        for(String type: resourceNamesLimited){
            if(!availableLimitedResources.get(type).isEmpty()) {
                multiMap.put(type, availableLimitedResources.get(type).remove(0));
            }
            else {
                System.out.println("Missing a"+ type+" cant assert Resources for Task Execution");
                return false;
            }
        }
        System.out.println("all limited resources can be asserted now:");
        for(String type: multiMap.keySet()){
            System.out.println(type+","+multiMap.get(type).size());
        }
        System.out.println("Creating unlimited Resource::");
        for(String type: resourceNamesUnlimited){
            multiMap.put(type, new Resource(allTypes.get(type)));
        }

        System.out.println("all limited + unlimited resources can be asserted now:");
        for(String type: multiMap.keySet()){
            System.out.println(type+","+multiMap.get(type).size());
        }
        task.setAssertedResources(multiMap);
        return true;

    }

    public static boolean freeResources(Task task){
        Printer.print(task.getName(), "freeing resources");
        Multimap<String,Resource> multimap= task.getAssertedResources();
        for(String key: multimap.keySet()){
            if(!allTypes.get(key).isUnlimited()){
                availableLimitedResources.get(key).addAll(multimap.get(key));
            }
        }
        return true;
    }



    public static void initResourceManager(){
        allTypes= EntityManager.allResourceTypes;
        unlimitedResources= new Hashtable<>();
        availableLimitedResources= new Hashtable<>();
        for(ResourceType type: allTypes.values()){
            if(!type.isUnlimited()){
                availableLimitedResources.put(type.getName(),createMultipleResources(type));
            }
        }

    }


    public static List<Resource> createMultipleResources(ResourceType type){
        System.out.println("Creating limited Resource::");
        List<Resource> list= new LinkedList<>();
        for(int i=0; i<type.getNumberOfOccasions() ; i++)
            list.add(new Resource(type));

        return list;
    }






}
