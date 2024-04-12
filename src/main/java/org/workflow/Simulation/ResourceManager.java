package org.workflow.Simulation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.*;

import org.workflow.OntologyClasses.Resource;
import org.workflow.OntologyClasses.ResourceType;
import org.workflow.OntologyClasses.Task;
import org.workflow.printer.Printer;
import org.workflow.printer.Sources;

public class ResourceManager {


	/**
	 * this is currently used as static class but implemented to be acting for only one workflow and its classes, resources, tasks types
	 * if there is ever multiple workflows the static implementation needs to be adjusted to work for multiple workflows
	 * or this is instantiated for every workflow
	 * this may be the chase for various classes
	 */


	/**
	 * same as in Entity manager, just for quickReference
	 */
	private static Hashtable<String, ResourceType> allTypes;
	/**
	 * the available Limited Resources, key is ResourceType Name, value is a List of all current available Resources of this Type
	 * if the List is empty there is currently no Resource of this type available
	 */
	public static Hashtable<String, List<Resource>> availableLimitedResources;

	/**
	 * in workflow planExecution for every finished task all Resourcews are set free, at the end of the method we call this with the tasks that had
	 * been waiting for resources before there had been resources set free, therefore w e reevaluate these here to update
	 * the waitingForResources List in Workflow
	 * @param taskNames
	 * @return
	 */
	public static HashSet<String> revaluateTasks(HashSet<String> taskNames){
		HashSet<String> newTaskNames= new HashSet<>();
		for(String string: taskNames){
			if(!checkResourceAssertionPossible(null, false, string)){
				newTaskNames.add(string);
			}
		}
		return  newTaskNames;
	}


	/**
	 * this is used two way
	 * 1. just check if a Assertion would be possible , by passing trc = null, bind = false, taskName=taskName
	 * 2. check and if feasible assert all resources to the task, this is called right before a task would be executed
	 * 	  pass trc= trc, bind = true, taskName = anything | null (ignored)
	 * @param trc the TaskRunConcept
	 * @param bind	if resources should be asserted, set only true if trc is passed
	 * @param taskName if bind false and trc null, pass taskName to check if resources for this task are available
	 * @return for case 1. true if all needed Resources available else false
	 * 		   for case 2. true if all needed Resources available and have been bound to task in trc else false
	 */
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


	/**
	 * sets free al used resources for this task
	 * we only care for limited as we have to put them back into our map
	 * unlimited are thrown away here as they will be instantiated on demand
	 * @param multimap where all used resources on this tasks are safed
	 * @return currently always returns true
	 */
	public static boolean freeResources(Multimap<String, Resource> multimap) {

		for (String key : multimap.keySet()) {
			if (!allTypes.get(key).isUnlimited()) {
				availableLimitedResources.get(key).addAll(multimap.get(key));
			}
		}
		return true;
	}

	/**
	 * initializer
	 */
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

	/**
	 * helper
	 * @param type type of which resources are to be instantiated
	 * @return the List of limited Resources that have eben instantiated by the information contained in the resourceType
	 */
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
