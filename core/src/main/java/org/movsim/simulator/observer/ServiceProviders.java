package org.movsim.simulator.observer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.autogen.ServiceProvidersType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Routing;

import com.google.common.base.Preconditions;

public final class ServiceProviders implements Iterable<ServiceProvider>, SimulationTimeStep {

    private final Map<String, ServiceProvider> serviceProviders = new HashMap<>();

    public ServiceProviders(ServiceProvidersType configuration, Routing routing, RoadNetwork roadNetwork) {
        Preconditions.checkNotNull(routing);
        Preconditions.checkNotNull(roadNetwork);
        for (ServiceProviderType serviceProviderType : configuration.getServiceProvider()) {
            ServiceProvider provider = new ServiceProvider(serviceProviderType, routing, roadNetwork);
            String key = provider.getLabel();
            if (serviceProviders.containsKey(key)) {
                throw new IllegalArgumentException("service provider label " + key + " already exists.");
            }
            serviceProviders.put(key, provider);
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        for (ServiceProvider provider : serviceProviders.values()) {
            provider.timeStep(dt, simulationTime, iterationCount);
        }
    }

    @Override
    public Iterator<ServiceProvider> iterator() {
        return serviceProviders.values().iterator();
    }

    @CheckForNull
    public ServiceProvider get(String serviceProviderName) {
        return serviceProviders.get(serviceProviderName);
    }

}
