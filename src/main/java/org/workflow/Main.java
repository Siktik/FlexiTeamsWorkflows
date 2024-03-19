package org.workflow;


import org.workflow.Classes.ResourceManager;

public class Main {

    /**
     * currently everything is started from here
     * 1. init Entity Manager
     * 2. import Ontology
     * 3. init Simulation
     * 4. run Simulation
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