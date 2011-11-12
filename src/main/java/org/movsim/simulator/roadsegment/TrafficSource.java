/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator.roadsegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.movsim.simulator.vehicles.Vehicle;

/**
 * Class representing a traffic source, the in-flow into a road segment.
 * 
 */
public class TrafficSource extends TrafficFlowBase {
    // for TrafficSource and subclasses roadSegment is the sink road
    public interface TrafficSourceCallback {
        /**
         * Callback to allow the application to process or record the traffic source data.
         * 
         * @param simulationTime
         *            the current logical time in the simulation
         * @param trafficSource 
         * @param vehicle 
         * @param vehiclesAdded 
         * @param inflowSum 
         * @param currentInflow 
         */
        public void recordData(double simulationTime, TrafficSource trafficSource, Vehicle vehicle,
                int vehiclesAdded, double inflowSum, double currentInflow);
    }
    private TrafficSourceCallback trafficSourceCallback;

    // inflow
    private double inflowStatic; // static traffic inflow, vehicles per second, all lanes
    private double inflowSum; // integrated inflow mod 1.0, regulates inflow
    private boolean inflowBlocked; // set true when vehicle gap too small to allow inflow
    /**
     * Dynamic (time-dependent) inflow
     */
    static class Inflow {
        double t0;
        double t1;
        double q0;
        double q1;
        double dqdt;
        int count;
        int index;
        final ArrayList<Double> times = new ArrayList<Double>();
        final ArrayList<Double> inflows = new ArrayList<Double>();

        @SuppressWarnings("boxing")
        void add (double simulationTime, double inflowValue) {
            final int searchIndex = Collections.binarySearch(times, simulationTime);
            if (searchIndex < 0) {
                final int insertionPoint = -searchIndex - 1;
                times.add(insertionPoint, simulationTime);
                inflows.add(insertionPoint, inflowValue);
            } else if (searchIndex == 0) {
                times.add(0, simulationTime);
                inflows.add(0, inflowValue);
            } else {
                // inflow is the same as an existing value - this should not happen
                assert false;
            }
            count = times.size();
            index = 1;
            t0 = times.get(0);
            q0 = inflows.get(0);
            if (index < count) {
                t1 = times.get(index);
                q1 = inflows.get(index);
            } else {
                // use large value for t1 to avoid special past end of array handling
                t1 = Double.MAX_VALUE;
                q1 = q0;
            }
            // slope for linear interpolation
            dqdt = (q1 - q0) / (t1 - t0);
        }

        @SuppressWarnings("boxing")
        double get(double simulationTime) {
            // note, for efficiency this code assumes simulation time increases by small value each time
            // this function is called. it does not support the direct calculation of inflow
            if (simulationTime >= t1) {
                // passed end of interval, so advance to next interval
                t0 = t1;
                q0 = q1;
                ++index;
                if (index < count) {
                    t1 = times.get(index);
                    q1 = inflows.get(index);
                } else {
                    // use large value for t1 to avoid special past end of array handling
                    t1 = Double.MAX_VALUE;
                    q1 = q0;
                }
                dqdt = (q1 - q0) / (t1 - t0);
            }
            // linearly interpolate inflow value
            return q0 + dqdt * (simulationTime - t0);
//            final int index = Collections.binarySearch(times, simulationTime);
//            if (index >= 0) {
//                return inflows.get(index);
//            }
//            final int insertionPoint = -index - 1;
//            if (insertionPoint == 0) {
//                return inflows.get(0);
//            }
//            final double t0 = times.get(insertionPoint - 1);
//            final double t1 = times.get(insertionPoint);
//            final double q0 = inflows.get(insertionPoint - 1);
//            final double q1 = inflows.get(insertionPoint);
//            return q0 + (simulationTime - t0) * (q1 - q0) / (t1 - t0);
        }
    }
    private Inflow inflow;
    private int vehiclesAdded;
    private final double gapMin = 25.0; // minimum headway for new vehicle, meters
    public static final double DENSITY_FIXED = -1.0;
    private double density = DENSITY_FIXED;

    // random number generation
    private final long seed = 987654321L;
    protected final Random random = new Random(seed);
    protected boolean setNewRandomExit;

    // vehicle templates
    public static Vehicle.Type CAR = Vehicle.Type.CAR;
    public static Vehicle.Type TRUCK = Vehicle.Type.TRUCK;
    protected Vehicle carTemplate;
    protected Vehicle truckTemplate;
    protected double proportionTrucks;
    static class Templates {
        double proportionsTotal;
        final ArrayList<Double> proportions = new ArrayList<Double>();
        final ArrayList<Double> cumulativeProportions = new ArrayList<Double>();
        final ArrayList<String> names = new ArrayList<String>();
        //final ArrayList<Vehicle> vehicleTemplates = new ArrayList<Vehicle>();
        final HashMap<String, Vehicle> vehicleTemplates = new HashMap<String, Vehicle>();

        @SuppressWarnings("boxing")
        void add(String name, Vehicle vehicleTemplate, double proportion) {
            proportions.add(proportion);
            proportionsTotal += proportion;
            cumulativeProportions.add(0.0);
            double sum = 0.0;
            for (int i = 0; i < proportions.size(); ++ i) {
                sum += proportions.get(i);
                cumulativeProportions.set(i, sum / proportionsTotal);
            }
            names.add(name);
            // templates.vehicleTemplates.add(vehicleTemplate);
            vehicleTemplates.put(name, vehicleTemplate);
        }
        Vehicle get(double proportion) {
            @SuppressWarnings("boxing")
            final int index = Collections.binarySearch(cumulativeProportions, proportion);
            final String name;
            if (index >= 0) {
                name = names.get(index);
            } else {
                final int insertionPoint = -index - 1;
                if (insertionPoint == 0) {
                    name = names.get(0);
                } else {
                    name = names.get(insertionPoint);
                }
            }
            return vehicleTemplates.get(name);
        }
    }
    private Templates templates;

    // test cars
    private HashMap<Integer, Integer> testCarIds;

    protected TrafficSource(Type type) {
        super(type);
    }

    /**
     * Constructor.
     */
    public TrafficSource() {
        this(Type.SOURCE);
    }

    /**
     * Copy constructor.
     * 
     * @param source
     */
    public TrafficSource(TrafficSource source) {
        this(source.type);
        copy(source);
    }

    /**
     * Copies a traffic source.
     * 
     * @param source
     */
    protected void copy(TrafficSource source) {
        this.inflowStatic = source.inflowStatic;
        this.proportionTrucks = source.proportionTrucks;
        this.density = source.density;
        //carTemplate = source.carTemplate.copy();
        //truckTemplate = source.truckTemplate.copy();
    }

    /**
     * Returns the sink road for this TrafficSource.
     * 
     * @return the sink road
     */
    public RoadSegment sinkRoad() {
        // for TrafficSource and subclasses roadSegment is the sink road
        return roadSegment;
    }

    /**
     * Sets the traffic source callback.
     * 
     * @param trafficSourceCallback
     */
    public void setTrafficSourceCallback(TrafficSourceCallback trafficSourceCallback) {
        this.trafficSourceCallback = trafficSourceCallback;
    }

    /**
     * Sets the sink road for this TrafficSource.
     * 
     * @param sinkRoad
     */
    public void setSinkRoad(RoadSegment sinkRoad) {
        assert sinkRoad != null;
        roadSegment = sinkRoad;
    }

    /**
     * Adds a vehicle template.
     * @param name 
     * @param vehicleTemplate 
     * @param proportion 
     * 
     */
    public void addTemplate(String name, Vehicle vehicleTemplate, double proportion) {
        if (templates == null) {
            templates = new Templates();
        }
        templates.add(name, vehicleTemplate, proportion);
    }

    public Vehicle template(String name) {
        if (templates == null) {
            return null;
        }
        return templates.vehicleTemplates.get(name);
    }

    /**
     * Sets the template for the given vehicle type.
     * 
     * @param vehicleType
     * @param vehicle template vehicle
     */
    public void setTemplate(Vehicle.Type vehicleType, Vehicle vehicle) {
     }

    /**
     * Returns a random vehicle template. The frequency of each type of template is determined
     * by their relative proportions.
     * @return random vehicle template
     */
    public Vehicle randomVehicleTemplate() {
        final double rand = random.nextDouble();
        if (templates == null) {
            return rand <= proportionTrucks ? truckTemplate : carTemplate;
        }
        return templates.get(rand);
    }

    /**
     * Returns the template for the given vehicle type.
     * 
     * @param vehicleType
     * @return vehicle template
     */
    public Vehicle template(Vehicle.Type vehicleType) {
        if (vehicleType == Vehicle.Type.CAR) {
            return carTemplate;
        }
        return truckTemplate;
    }

    /**
     * Adds the testCarId to the list of ids that are for test cars.
     * @param testCarId
     */
    @SuppressWarnings("boxing")
    public void addTestCarId(int testCarId) {
        if (testCarIds == null) {
            testCarIds = new HashMap<Integer, Integer>();
        }
        testCarIds.put(testCarId, 0);
    }

    /**
     * Returns true if the given id is that of a test car.
     * @param testCarId
     * @return true if the given id is that of a test car
     */
    @SuppressWarnings("boxing")
    public boolean isTestCarId(int testCarId) {
        if (testCarIds == null) {
            return false;
        }
        if (testCarIds.get(testCarId) == null) {
            return false;
        }
        return true;
    }


    /**
     * Convenience function, sets the lane change politeness and threshold values for the given
     * vehicle type.
     * 
     * @param vehicleType 
     * @param politeness
     * @param threshold
     */
    public final void setLaneChange(Vehicle.Type vehicleType, double politeness, double threshold) {
        final Vehicle template = vehicleType == Vehicle.Type.CAR ? carTemplate : truckTemplate;
    }

     /**
     * Returns the inflow at a given simulation time.
     * @param simulationTime
     * @return inflow, vehicles per second
     */
    protected double inflow(double simulationTime) {
        if (inflow == null) {
            return inflowStatic;
        }
        return inflow.get(simulationTime);
    }

    /**
     * Adds an inflow for a given simulation time.
     * @param simulationTime
     * @param inflowValue inflow, vehicles per second
     */
    public void addInflow(double simulationTime, double inflowValue) {
        assert inflowValue >= 0.0;
        assert inflowValue < 2.0; // user probably setting vehicles per hour, rather than per second
        if (inflow == null) {
            inflow = new Inflow();
        }
        inflow.add(simulationTime, inflowValue);
    }

    /**
     * Convenience function, adds an inflow in vehicles per hour for a given simulation time.
     * @param simulationTime
     * @param inflow inflow, vehicles per hour
     */
    public final void addInflowVehiclesPerHour(double simulationTime, double inflow) {
        addInflow(simulationTime, inflow / 3600.0);
    }

    /**
     * Returns the inflow
     * @return inflow, vehicles per second
     */
    public double inflow() {
        return inflowStatic;
    }

    /**
     * Sets the traffic inflow for this source.
     * 
     * @param inflow
     *            traffic inflow, vehicles per second all lanes
     */
    public void setInflow(double inflow) {
        assert inflow >= 0.0;
        assert inflow < 2.0; // user probably setting vehicles per hour, rather than per second
        inflowStatic = inflow;
        //System.out.println("TrafficInflow.setInflow (per hour):" + inflow * 3600.0);//$NON-NLS-1$
    }

    /**
     * Convenience function to set inflow in vehicles per hour
     * 
     * @param inflow
     *            traffic flow, vehicles per hour all lanes
     */
    public void setInflowVehiclesPerHour(double inflow) {
        setInflow(inflow / 3600.0);
    }

    /**
     * Returns true if the inflow is block, that is there is no room to insert a vehicle.
     * 
     * @return true if the inflow is blocked
     */
    public boolean inflowBlocked() {
        return inflowBlocked;
    }

    /**
     * Sets the proportion of trucks in this source's inflow.
     * 
     * @param proportionTrucks
     *            proportion of vehicles that are trucks
     */
    public void setProportionTrucks(double proportionTrucks) {
        this.proportionTrucks = proportionTrucks;
    }

    /**
     * Sets the traffic density for a closed loop flow.
     * 
     * @param density
     *            traffic density for a closed loop flow, vehicles per km all lanes
     */
    public void setDensity(double density) {
        this.density = density;
    }

    /**
     * Returns the traffic density for a closed loop flow.
     * @return the traffic density for a closed loop flow
     */
    public double density() {
        return density;
    }

    /**
     * Add a vehicle to the roadSegment as dictated by the source's inflow value.
     * A vehicle will only be added if there is room on the roadSegment.
     * 
     * @param dt
     *            time interval
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        final RoadSegment sinkRoad = sinkRoad();
        assert sinkRoad != null;
        final int laneCount = sinkRoad.laneCount();
        assert laneCount > 0;
        final double currentInflow = inflow(simulationTime);
        inflowSum +=  currentInflow * dt;
     }

    /**
     * Creates a new vehicle, which will subsequently be added to this source's sink road.
     * 
     * @param lane
     * @param pos
     * @param vel
     * @param vehicleTemplate 
     * @return the vehicle created
     */
    public Vehicle newVehicle(int lane, double pos, double vel, Vehicle vehicleTemplate) {

        assert lane <= RoadSegment.MAX_LANE_COUNT;

        if (vehicleTemplate == null) {
            vehicleTemplate = randomVehicleTemplate();
        }
//        final Vehicle vehicle = new Vehicle(vehicleTemplate, pos, vel, lane);
//        if (isTestCarId(vehicle.getId())) {
//            vehicle.setType(Vehicle.Type.TEST_CAR);
//        }
//        final Vehicle.Noise noise = vehicleTemplate.noise();
//        if (noise != null) {
//            vehicle.setNoise(noise);
//        }
//        return vehicle;
        return null;
    }

    /**
     * Convenience function, creates a new vehicle with its velocity set to the equilibrium velocity
     * for the given gap.
     * 
     * @param lane
     * @param pos
     * @param gap
     * @return the vehicle created
     */
    public final Vehicle newVehicleAtEquilibriumVelocity(int lane, double pos, double gap) {

        final Vehicle vehicle = newVehicle(lane, pos, 0.0, null);
        return vehicle;
    }

    /**
     * Convenience function, creates a new vehicle with its velocity set to its desired velocity.
     * 
     * @param lane
     * @param pos
     * @return the vehicle created
     */
    public final Vehicle newVehicleAtDesiredVelocity(int lane, double pos) {

        final Vehicle vehicle = newVehicle(lane, pos, 0.0, null);
//        vehicle.setVelocity(vehicle.longitudinalDriverModel().desiredVelocity());
        return vehicle;
    }
}
