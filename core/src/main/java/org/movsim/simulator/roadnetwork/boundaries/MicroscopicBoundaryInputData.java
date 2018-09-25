package org.movsim.simulator.roadnetwork.boundaries;

import com.google.common.base.Preconditions;
import org.movsim.scenario.boundary.autogen.BoundaryConditionType;
import org.movsim.scenario.boundary.autogen.BoundaryConditionsType;
import org.movsim.scenario.boundary.autogen.VehicleUserDataType;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.TimeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MicroscopicBoundaryInputData {

    private static final Logger LOG = LoggerFactory.getLogger(MicroscopicBoundaryInputData.class);

    private final BoundaryConditionsType boundaryConditions;
    private final long timeOffsetMillis;
    private final String timeFormat;
    private final Routing routing;

    public MicroscopicBoundaryInputData(BoundaryConditionsType boundaryConditions, String timeFormat,
            long timeOffsetMillis, Routing routing) {
        this.boundaryConditions = Preconditions.checkNotNull(boundaryConditions);
        this.routing = Preconditions.checkNotNull(routing);
        this.timeFormat = timeFormat;
        this.timeOffsetMillis = timeOffsetMillis;
    }

    public Map<Long, Vehicle> createVehicles(TrafficSourceMicro trafficSource) {
        Preconditions.checkNotNull(trafficSource);
        Map<Long, Vehicle> vehicles = new HashMap<>();
        int size = boundaryConditions.getBoundaryCondition().size();
        LOG.info("number of vehicleBoundaryCondition entries={}", size);
        for (BoundaryConditionType boundaryCondition : boundaryConditions.getBoundaryCondition()) {
            Vehicle vehicle = createVehicle(boundaryCondition, trafficSource);
            // round to seconds
            long time = Math
                    .round(TimeUtilities.convertToSeconds(boundaryCondition.getTime(), timeFormat, timeOffsetMillis));
            Preconditions.checkArgument(!vehicles.containsKey(time),
                    "time=" + time + " already used as micro boundary condition");
            vehicles.put(time, vehicle);
        }
        return vehicles;
    }

    private Vehicle createVehicle(BoundaryConditionType record, TrafficSourceMicro trafficSource) {
        final Vehicle vehicle = trafficSource.vehGenerator.createVehicle(record.getLabel());

        if (record.isSetSpeed()) {
            vehicle.setSpeed(record.getSpeed());
        }

        if (record.isSetLength()) {
            vehicle.getDimensions().setLength(record.getLength());
        }

        Route route = null;
        if (record.isSetRoute()) {
            if (routing.hasRoute(record.getRoute())) {
                route = routing.get(record.getRoute());
                LOG.info("overwrites vehicle's default route by input file: route={}", route.getName());
            } else {
                throw new IllegalStateException("cannot find route=" + record.getRoute());
            }
        }
        if (record.isSetDestination()) {
            if (record.isSetRoute()) {
                throw new IllegalStateException(
                        "ambiguous microscopic boundary condition: route=" + record.getRoute() + " and destination="
                                + record.getDestination());
            }
            // determine route by destination
            route = routing.findRoute(trafficSource.roadSegment.userId(), record.getDestination());
            if (route == null) {
                LOG.error("no route assigned to vehicle={}", vehicle);
                throw new IllegalStateException("cannot find route by destination node=" + record.getDestination());
            }
        }

        if (route != null) {
            LOG.debug("found route and overwrites vehicle's default route: route={}", route.getName());
            vehicle.setRoute(route);
        }

        if (record.isSetLane()) {
            int lane = record.getLane();
            int laneCount = trafficSource.roadSegment.laneCount();
            if (lane > trafficSource.roadSegment.laneCount()) {
                LOG.warn("input lane={} not available on road={}, set to laneCount={}", lane,
                        trafficSource.roadSegment.userId(), laneCount);
                lane = laneCount;
            }
            if (lane < Lanes.MOST_INNER_LANE) {
                LOG.warn("input lane={} not available on road={}, set lane to lane={}", lane,
                        trafficSource.roadSegment.userId(), Lanes.MOST_INNER_LANE);
                lane = Lanes.MOST_INNER_LANE;
            }
            record.setLane(lane);
        }

        if (!record.getVehicleUserData().isEmpty()) {
            for (VehicleUserDataType vehUserData : record.getVehicleUserData()) {
                vehicle.getUserData().put(vehUserData.getKey(), vehUserData.getValue());
            }
        }

        return vehicle;
    }

}
