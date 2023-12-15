package org.workflow;


public class Main {


    public static void main(String[] args) {
        EntityManager.init();
        Importer.importOWL();
        SimulationManager.init();
    }
}