package org.movsim.simulator.roadnetwork.predicates;

import javax.annotation.Nullable;

import org.movsim.simulator.vehicles.Vehicle;

import com.google.common.base.Predicate;

public class VehicleWithRange implements Predicate<Vehicle> {
    private final double begin;
    private final double end;

    public VehicleWithRange(double begin, double end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean apply(@Nullable Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        return vehicle.getFrontPosition() >= begin && vehicle.getFrontPosition() <= end;
    }
}
