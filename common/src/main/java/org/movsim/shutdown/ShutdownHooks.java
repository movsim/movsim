package org.movsim.shutdown;

import java.util.ArrayList;
import java.util.List;

public enum ShutdownHooks implements SimulationShutDown {

    INSTANCE;

    private final List<SimulationShutDown> callbacks = new ArrayList<>();

    public void addCallback(final SimulationShutDown callback) {
	if (callback != null) {
	    callbacks.add(callback);
	}
    }

    @Override
    public void onShutDown() {
        System.out.println("perform shutdown callbacks=" + callbacks.size());
        for (final SimulationShutDown shutDownCallback : callbacks) {
            shutDownCallback.onShutDown();
        }
    }

    public void clear() {
	callbacks.clear();
    }

}
