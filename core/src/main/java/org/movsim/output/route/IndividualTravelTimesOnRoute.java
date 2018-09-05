package org.movsim.output.route;

import com.google.common.collect.Iterators;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.Vehicle;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndividualTravelTimesOnRoute extends OutputOnRouteBase {

    private final SignalPoint entrySignalPoint;

    private final SignalPoint exitSignalPoint;

    /**
     * mapping from vehicle to its entry time on route
     */
    private final Map<Vehicle, Double> vehiclesOnRoute = new LinkedHashMap<>();

    private final FileIndividualTravelTimesOnRoute fileWriter;

    public IndividualTravelTimesOnRoute(RoadNetwork roadNetwork, Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.fileWriter = writeOutput ? new FileIndividualTravelTimesOnRoute(route) : null;

        RoadSegment firstRoadSegmentOnRoute = route.get(0);
        entrySignalPoint = new SignalPoint(0, firstRoadSegmentOnRoute);
        firstRoadSegmentOnRoute.signalPoints().add(entrySignalPoint);

        RoadSegment lastRoadSegmentOnRoute = Iterators.getLast(route.iterator());
        exitSignalPoint = new SignalPoint(lastRoadSegmentOnRoute.roadLength(), lastRoadSegmentOnRoute);
        lastRoadSegmentOnRoute.signalPoints().add(exitSignalPoint);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        addNewVehicles(entrySignalPoint.passedVehicles(), simulationTime);
        calculateTravelTimes(exitSignalPoint.passedVehicles(), simulationTime);
        LOG.debug("vehiclesOnRoute.size={}", vehiclesOnRoute.size());
    }

    private void calculateTravelTimes(Collection<Vehicle> vehicles, double simulationTime) {
        for (Vehicle vehicle : vehicles) {
            Double entryTime = vehiclesOnRoute.remove(vehicle);
            if (entryTime == null) {
                // happens if sources like onramps etc along the route are present
                LOG.debug("vehicle not passed whole route, ignore for individual travel time output: {}", vehicle);
                continue;
            }

            if (fileWriter != null) {
                // writes out in order of having traversed route
                fileWriter.write(vehicle, entryTime.doubleValue(), simulationTime, route.getLength());
            }
        }
    }

    private void addNewVehicles(Collection<Vehicle> vehicles, double simulationTime) {
        for (Vehicle vehicle : vehicles) {
            vehiclesOnRoute.put(vehicle, simulationTime);
        }
    }

}
