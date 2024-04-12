package org.workflow;

import org.workflow.ImporterPackage.Importer;
import org.workflow.Simulation.EntityManager;
import org.workflow.Simulation.ResourceManager;
import org.workflow.Simulation.SimulationManager;

public class Main {

	/**
	 * currently everything is started from here
	 * 1. init Entity Manager
	 * 2. import Ontology
	 * 3. init ResourceManager
	 * 4. init Simulation
	 * 5. run Simulation
	 * @param args
	 */

	public static void main(String[] args) {
		EntityManager.init();
		Importer.importOWL();
		ResourceManager.initResourceManager();
		SimulationManager.init();
		SimulationManager.runSimulation();
	}
}
