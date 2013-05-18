package org.movsim.simulator.roadnetwork.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.ElevationProfile;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.ElevationProfile.Elevation;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.roadnetwork.SignalPoint.SignalPointType;
import org.movsim.simulator.vehicles.Vehicle;

public class GradientProfile extends RoadObjectController {

    private final double endPosition;

    /** mapping of positions to gradients along track */
    private final SortedMap<Double, Double> gradients = new TreeMap<>();

    private final Set<Vehicle> controlledVehicles = new HashSet<>();

    public GradientProfile(ElevationProfile elevationProfile, RoadSegment roadSegment) {
        super(RoadObjectType.GRADIENT_PROFILE, elevationProfile.getElevation().get(0).getS(), roadSegment);
        createGradientProfile(elevationProfile.getElevation());
        if(position != gradients.firstKey()){
            throw new IllegalArgumentException("first given track position="+position+" > lowest position=" + gradients.firstKey()+" in elevation profile");
        }
        endPosition = gradients.lastKey();
        if (endPosition > roadSegment().roadLength()) {
            throw new IllegalArgumentException("elevation profile track position s=" + endPosition
                    + " exceeds roadlength.");
        }
    }

    @Override
    public void createSignalPositions() {
        roadSegment.signalPoints().add(new SignalPoint(SignalPointType.START, position, this));
        roadSegment.signalPoints().add(new SignalPoint(SignalPointType.END, endPosition, this));
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        LOG.debug("controlledVehicles.size={}, vehiclesPassedEnd={}", controlledVehicles.size(),
                vehiclesPassedEnd.size());
        controlledVehicles.addAll(vehiclesPassedStart);
        for (Vehicle vehicle : vehiclesPassedEnd) {
            vehicle.setSlope(0); // reset
            controlledVehicles.remove(vehicle);
        }
        for (Vehicle vehicle : controlledVehicles) {
            apply(vehicle);
        }
        if (controlledVehicles.size() > 100) {
            // precautionary measure: check if removing mechanism is working properly
            LOG.warn("Danger of memory leak: controlledVehicles.size={}", controlledVehicles.size());
        }
    }

    private void apply(Vehicle vehicle) {
        assert vehicle.getFrontPosition() >= position;
        assert vehicle.getFrontPosition() <= endPosition;
        Double posUpstream = gradients.headMap(vehicle.getFrontPosition()).lastKey();
        double gradient = gradients.get(posUpstream);
        vehicle.setSlope(gradient);
        LOG.info("pos={} --> slope gradient={}", vehicle.getFrontPosition(), gradient);
    }

    private void createGradientProfile(List<Elevation> elevationProfile) {
        SortedMap<Double, Double> elevation = new TreeMap<>();
        for (Elevation basePoint : elevationProfile) {
            elevation.put(basePoint.getS(), basePoint.getA());
        }
        
        Entry<Double, Double> previousElevationPoint = null;
        for(Entry<Double, Double> elevationPoint : elevation.entrySet()){
            if(previousElevationPoint==null){
                previousElevationPoint = elevationPoint;
                continue;
            }
            double deltaPosition = elevationPoint.getKey()-previousElevationPoint.getKey();
            double deltaHeight = elevationPoint.getValue()-previousElevationPoint.getValue();
            if(deltaPosition>0){
                gradients.put(previousElevationPoint.getKey(), deltaHeight/deltaPosition);
                previousElevationPoint = elevationPoint;
            }
        }
        gradients.put(elevation.lastKey(), 0.0);
//        // note: perhaps is iterating the sorted map even faster?!
//        Double posUpstream = elevation.headMap(vehiclePosition).lastKey();
//        Double posDownstream = elevation.headMap(vehiclePosition).lastKey();
//        double diff = posDownstream - posUpstream;
//        return diff == 0 ? 0 : (elevation.get(posDownstream) - elevation.get(posUpstream)) / diff;
    }
    
    public Set<Entry<Double, Double>> gradientEntries() {
        return Collections.unmodifiableSet(gradients.entrySet());
    }

}
