package org.workflow.Printer;

import java.util.Arrays;
import jdk.jfr.Description;
import org.workflow.Simulation.EntityManager;
import org.workflow.ImporterPackage.PropertyTypes;
import org.workflow.SimulationHelpers.TimeManager;

public class Printer {

	@Description(
		"print Output, first Param is Source (f.e. Name), second Param is Message"
	)
	public static void print(String source, String message) {
		System.out.println(
			source + ":" + TimeManager.getSimTime() + ":" + message
		);
	}

	public static void errorPrint(String source, String message) {
		System.err.println(source + ":" + TimeManager.getSimTime() + ":" + message);
	}

	public static String printPropertyRules(ClassTypes type) {
		System.out.println();
		switch (type) {
			case TASK -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " + ClassTypes.TASK + "\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.TASK)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");
				return builder.toString();
			}
			case EVENT -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " + ClassTypes.EVENT + "\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.EVENT)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");

				return builder.toString();
			}
			case PERSON -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " + ClassTypes.PERSON + "\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.PERSON)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");

				return builder.toString();
			}
			case RESOURCE -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " +
					ClassTypes.RESOURCE +
					"\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.RESOURCE)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");

				return builder.toString();
			}
			case RESOURCETYPE -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " +
					ClassTypes.RESOURCETYPE +
					"\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.TASK)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");

				return builder.toString();
			}
			case QUALIFICATION -> {
				StringBuilder builder = new StringBuilder(
					"Property Rules For " +
					ClassTypes.QUALIFICATION +
					"\nMustHaves: "
				);

				PropertyTypes.propertyRulesMap
					.get(ClassTypes.QUALIFICATION)
					.forEach(e -> builder.append("|| " + e));
				builder.append("\n");

				return builder.toString();
			}
		}

		return "";
	}

	/**
	 * printing Details
	 * @param all true if all should printed, second param is ignored then, if false, second param is used
	 * @param types
	 */
	public static void printEntityDetails(boolean all, ClassTypes types) {
		if (all) {
			System.out.println("Printing Details for all entities");
			Arrays.stream(ClassTypes.values())
				.sequential()
				.forEach(Printer::printEntityDetail);
		} else {
			if (types == null) {
				throw new IllegalStateException(
					"Wrong usage of printEntityDetails, passed false for printing all but didnt specify ClassType to print"
				);
			}
			System.out.println(
				"Printing Details for entities of type " + types
			);

			printEntityDetail(types);
		}
	}

	private static void printEntityDetail(ClassTypes type) {
		switch (type) {
			case TASK -> {
				System.out.println("\nAll Tasks:\n");
				EntityManager.allTasks.values().forEach(System.out::println);
			}
			case EVENT -> {
				System.out.println("\nAll Events:\n");
				EntityManager.allEvents.values().forEach(System.out::println);
			}
			case RESOURCETYPE -> {
				System.out.println("\nAll ResourceTypes:\n");
				EntityManager.allResourceTypes
					.values()
					.forEach(System.out::println);
			}
			case QUALIFICATION -> {
				System.out.println("\nAll Qualification:\n");
				EntityManager.allQualifications
					.values()
					.forEach(System.out::println);
			}
			case PERSON -> {
				System.out.println("\nAll Person:\n");
				EntityManager.allPersons.values().forEach(System.out::println);
			}
			case WORKFLOW -> {
				System.out.println("\nAll Workflows:\n");
				EntityManager.allWorkflows
					.values()
					.forEach(System.out::println);
			}
		}
	}
}