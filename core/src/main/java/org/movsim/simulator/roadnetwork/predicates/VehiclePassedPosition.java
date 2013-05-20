package org.movsim.simulator.roadnetwork.predicates;

import javax.annotation.Nullable;

import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Predicate;

// rearPosition aligned with handling of trafficSinks !!!
public final class VehiclePassedPosition implements Predicate<Vehicle> {

    private final double position;

    public VehiclePassedPosition(double position) {
        this.position = position;
    }

    @Override
    public boolean apply(@Nullable Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        assert vehicle.getRearPositionOld() <= vehicle.getRearPosition() : "oldPos > pos!";
        return vehicle.getRearPositionOld() <= position && vehicle.getRearPosition() > position;
    }

}