package org.workflow.Classes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.*;
import org.workflow.EntityManager;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

import javax.xml.transform.Source;

public class ResourceManager {


	/**
	 * this is currently used as static class but implemented to be acting for only one workflow and its classes, resources, tasks types
	 * if there is ever multiple workflows the static implementation needs to be adjusted to work for multiple workflows
	 * or this is instantiated for every workflow
	 * this may be the chase for various classes
	 */


	private static Hashtable<String, ResourceType> allTypes;
	public static Hashtable<String, List<Resource>> availableLimitedResources;

	public static HashSet<String> revaluateTasks(HashSet<String> taskNames){
		HashSet<String> newTaskNames= new HashSet<>();
		for(String string: taskNames){
			if(!checkResourceAssertionPossible(null, false, string)){
				newTaskNames.add(string);
			}
		}
		return  newTaskNames;
	}

	public static boolean checkResourceAssertionPossible(TaskRunConcept trc, boolean bind, String taskName) {
		Task task;
		if(bind){
			task= trc.getTask();
		}else{
			task= EntityManager.allTasks.get(taskName);
		}
		Multimap<String, Resource> multiMap = ArrayListMultimap.create();

		for (String type : task.getResourceNamesLimited()) {
			if (!availableLimitedResources.get(type).isEmpty()) {
				multiMap.put(
					type,
					availableLimitedResources.get(type).remove(0)
				);
			} else {
				if(bind) {
					Printer.print(trc.event.getName(), task.getName() + " missing a" +
							type +
							" cant assert Resources for Task Execution");
				}else {
					Printer.print(Sources.ResourceManager.name(), task.getName() + " missing a" +
							type +
							" cant assert Resources for Task Execution");

				}
				return false;
			}
		}

		if(bind) {
			if(!task.getResourceNamesUnlimited().isEmpty()) {
				Printer.print(trc.event.getName(), task.getName() + " creating Resources for unlimited Types:");
				for (String type : task.getResourceNamesUnlimited()) {
					Resource resource = new Resource(allTypes.get(type));
					System.out.println("\t" + resource);
					multiMap.put(type, resource);

				}
			}
			if(!multiMap.isEmpty()) {
				Printer.print(trc.event.getName(), task.getName() + " BINDING RESOURCES:");

				for (String type : multiMap.keySet()) {
					System.out.println("\t" + type + "," + multiMap.get(type).size() + (allTypes.get(type).isUnlimited() ? " (UL)" : " (L)"));
				}
				task.setAssertedResources(multiMap);
			}
		}else{
			Printer.print(taskName, "Assertion now theoretically possible");
			freeResources(multiMap);
		}
		return true;
	}



	public static boolean freeResources(Multimap<String, Resource> multimap) {

		for (String key : multimap.keySet()) {
			if (!allTypes.get(key).isUnlimited()) {
				availableLimitedResources.get(key).addAll(multimap.get(key));
			}
		}
		return true;
	}

	public static void initResourceManager() {
		allTypes = EntityManager.allResourceTypes;
		availableLimitedResources = new Hashtable<>();

		for (ResourceType type : allTypes.values()) {
			if (!type.isUnlimited()) {
				availableLimitedResources.put(
					type.getName(),
					createMultipleResources(type)
				);
			}
		}
	}

	public static List<Resource> createMultipleResources(ResourceType type) {
		System.out.println("Creating limited Resource::");
		List<Resource> list = new LinkedList<>();
		for (int i = 0; i < type.getNumberOfOccasions(); i++) {
			Resource resource= new Resource(type);
			System.out.println(resource);
			list.add(resource);
		}

		return list;
	}
}
