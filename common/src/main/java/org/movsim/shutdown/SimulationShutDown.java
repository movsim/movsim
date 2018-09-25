package org.movsim.shutdown;

public interface SimulationShutDown {

    /**
     * Callback to allow the application to close all open resources in an ordered way before the program exits.
     */
     void onShutDown();
}