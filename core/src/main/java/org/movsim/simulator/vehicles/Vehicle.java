/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles;

import org.movsim.input.model.vehicle.VehicleInput;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.vehicles.consumption.FuelConsumption;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel.LaneChangeDecision;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.movsim.simulator.vehicles.longitudinalmodel.Memory;
import org.movsim.simulator.vehicles.longitudinalmodel.TrafficLightApproaching;
import org.movsim.utilities.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Model for a vehicle in a traffic simulation.
 * </p>
 * 
 * <p>
 * Each vehicle has its own unique immutable id which is assigned when the vehicle is created.
 * </p>
 * 
 * <p>
 * A vehicle has a size, given by its length and width.
 * </p>
 * 
 * <p>
 * A vehicle has the kinematic attributes of position, velocity and acceleration. A vehicle's position is given by the
 * position of the front of the vehicle on the road segment and also by the vehicle's lane.
 * </p>
 * 
 * <p>
 * A vehicle possesses two intelligence modules:
 * <ul>
 * <li>a LongitudinalModel which determines its acceleration in the direction of travel.</li>
 * <li>a LaneChangeModel which determines when it changes lanes.</li>
 * </ul>
 * </p>
 * <p>
 * Vehicles are quite frequently created and destroyed, so by design they have few allocated properties.
 * </p>
 */
public class Vehicle {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Vehicle.class);

    protected static final int INITIAL_ID = 1;
    protected static final int INITIAL_TEMPLATE_ID = -1;

    private static long nextId = INITIAL_ID;
    private static long nextTemplateId = INITIAL_TEMPLATE_ID;

    /**
     * 'Not Set' vehicle id value, guaranteed not to be used by any vehicles.
     */
    public static final int ID_NOT_SET = -1;
    private static final int VEHICLE_NUMBER_NOT_SET = -1;
    private static final int LANE_NOT_SET = -1;
    
    /**
     * 'Not Set' road segment id value, guaranteed not to be used by any vehicles.
     */
    public static final int ROAD_SEGMENT_ID_NOT_SET = -1;

    /** in m/s^2 */
    private final static double THRESHOLD_BRAKELIGHT_ON = 0.2;

    /** in m/s^2 */
    private final static double THRESHOLD_BRAKELIGHT_OFF = 0.1;

    /** needs to be > 0 */
    private final static double FINITE_LANE_CHANGE_TIME_S = 5;

    private final String label;

    private final double length;
    private final double width;

    /** The front position of the vehicle. The reference position. */
    private double frontPosition;

    /** The old front position of the vehicle. */
    private double frontPositionOld;

    /** The total distance travelled */
    private double totalTraveledDistance;

    private double speed;

    /** The acceleration as calculated by the longitudinal driver model. */
    private double accModel;

    /**
     * The actual acceleration. This is the acceleration calculated by the LDM moderated by other
     * factors, such as traffic lights
     */
    private double acc;

    private double accOld;

    /** The reaction time. */
    private final double reactionTime;

    /** The max deceleration . */
    private final double maxDeceleration;

    /** The unique id of the vehicle */
    final long id;

    /** The vehicle number. */
    private int vehNumber = VEHICLE_NUMBER_NOT_SET;

    private int lane = LANE_NOT_SET;
    private int laneOld;

    /** variable for remembering new target lane when assigning to new laneSegment */
    private int targetLane;

    /** finite lane-changing duration */
    private double tLaneChangeDelay;

    private double speedlimit;
    private double slope;

    private LongitudinalModelBase longitudinalModel;
    private LaneChangeModel laneChangeModel;

    private Memory memory = null;
    private Noise noise = null;

    private int color;
    private Object colorObject; // color object cache
   
    private final TrafficLightApproaching trafficLightApproaching;
    private final FuelConsumption fuelModel; // can be null
    private final Route route;
    private int routeIndex;

    private boolean isBrakeLightOn;

    private PhysicalQuantities physQuantities;

    // Exit Handling
    private int roadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
    private double roadSegmentLength;
    private int exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

    /**
     * The type of numerical integration.
     */
    public static enum IntegrationType {
        /**
         * Euler (first order) numerical integration.
         */
        EULER,
        /**
         * Kinematic (second order) numerical integration.
         */
        KINEMATIC,
        /**
         * Runge-Kutta (fourth order) numerical integration.
         */
        RUNGE_KUTTA
    }

    private static IntegrationType integrationType = IntegrationType.KINEMATIC;

    /**
     * Resets the next id.
     */
    public static void resetNextId() {
        nextId = INITIAL_ID;
        nextTemplateId = INITIAL_TEMPLATE_ID;
    }

    /**
     * Returns the id of the last vehicle created.
     * 
     * @return the id of the last vehicle created
     */
    public static long lastIdSet() {
        return nextId - 1;
    }

    /**
     * Returns the number of vehicles that have been created. Used for instrumentation.
     * 
     * @return the number of vehicles that have been created
     */
    public static long count() {
        return nextId - INITIAL_ID;
    }

    public Vehicle(String label, LongitudinalModelBase longitudinalModel, VehicleInput vehInput, Object cyclicBuffer,
            LaneChangeModel lcModel, FuelConsumption fuelModel, Route route) {
        this.label = label;
        id = nextId++;
        this.fuelModel = fuelModel;

        length = vehInput.getLength();
        width = vehInput.getWidth();
        reactionTime = vehInput.getReactionTime();
        maxDeceleration = vehInput.getMaxDeceleration();

        initialize();
        this.longitudinalModel = longitudinalModel;
        physQuantities = new PhysicalQuantities(this);

        this.laneChangeModel = lcModel;
        laneChangeModel.initialize(this);

        // no effect if model is not configured with memory effect
        if (vehInput.isWithMemory()) {
            memory = new Memory(vehInput.getMemoryInputData());
        }

        if (vehInput.isWithNoise()) {
            noise = new Noise(vehInput.getNoiseInputData());
        }

        trafficLightApproaching = new TrafficLightApproaching();
        this.route = route;

        // needs to be > 0 to avoid lane-changing over 2 lanes in one update step
        assert FINITE_LANE_CHANGE_TIME_S > 0;

        this.color = Colors.randomColor();
    }

    /**
     * Constructor.
     */
    public Vehicle(double rearPosition, double speed, int lane, double length, double width) {
        assert rearPosition >= 0.0;
        assert speed >= 0.0;
        id = nextId++;
        this.length = length;
        setRearPosition(rearPosition);
        this.speed = speed;
        this.lane = lane;
        this.laneOld = lane;
        this.width = width;
        this.color = 0;
        fuelModel = null;
        trafficLightApproaching = null;
        reactionTime = 0.0;
        maxDeceleration = 0.0;
        laneChangeModel = null;
        longitudinalModel = null;
        label = "";
        physQuantities = new PhysicalQuantities(this);
        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
        slope = 0;
        route = null;
    }

    /**
     * Copy constructor.
     * 
     * @param source
     */
    public Vehicle(Vehicle source) {
        id = source.id;  // TODO id not unique in this case 
        type = source.type;
        frontPosition = source.frontPosition;
        speed = source.speed;
        lane = source.lane;
        laneOld = source.laneOld;
        length = source.length;
        width = source.width;
        color = source.color;
        fuelModel = source.fuelModel;
        trafficLightApproaching = source.trafficLightApproaching;
        reactionTime = source.reactionTime;
        maxDeceleration = source.maxDeceleration;
        laneChangeModel = source.laneChangeModel;
        longitudinalModel = source.longitudinalModel;
        label = source.label;
        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
        slope = source.slope;
        route = source.route;
    }

    /**
     * Constructor.
     */
    public Vehicle(LongitudinalModelBase ldm, Object lcm, double length, double width) {
        id = nextId++;
        this.length = length;
        setRearPosition(0.0);
        this.speed = 0.0;
        this.lane = Lane.NONE;
        this.laneOld = Lane.NONE;
        this.width = width;
        this.color = 0;
        fuelModel = null;
        trafficLightApproaching = null;
        reactionTime = 0.0;
        maxDeceleration = 0.0;
        laneChangeModel = null;
        longitudinalModel = ldm;
        label = "";
        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
        slope = 0;
        route = null;
    }

    private void initialize() {
        frontPositionOld = 0;
        frontPosition = 0;
        speed = 0;
        acc = 0;
        isBrakeLightOn = false;
        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
        slope = 0;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Sets this vehicle's color.
     * 
     * @param color
     *            RGB integer color value
     */
    public final void setColor(int color) {
        this.color = color;
    }

    /**
     * Returns this vehicle's color.
     * 
     * @return vehicle's color, as an RGB integer
     */
    public final int color() {
        return color;
    }

    /**
     * Sets this vehicle's color object cache value. Primarily of use by AWT which rather inefficiently uses objects
     * rather than integers to represent color values. Note that an object is cached so Vehicle.java has no dependency
     * on AWT.
     * 
     * @param colorObject
     */
    public final void setColorObject(Object colorObject) {
        this.colorObject = colorObject;
    }

    /**
     * Returns the previously cached object associated with this vehicle's color.
     * 
     * @return vehicle's previously cached color object
     */
    public final Object colorObject() {
        return colorObject;
    }

    /**
     * Returns this vehicle's length.
     * 
     * @return vehicle's length, in meters
     */
    public final double getLength() {
        return length;
    }

    /**
     * Returns this vehicle's width.
     * 
     * @return vehicle's width, in meters
     */
    public final double getWidth() {
        return width;
    }

    /**
     * Returns position of the front of this vehicle.
     * 
     * @return position of the front of this vehicle
     */
    public final double getFrontPosition() {
        return frontPosition;
    }

    /**
     * Sets the position of the rear of this vehicle.
     * 
     * @param frontPosition
     *            new front position
     */
    public final void setFrontPosition(double frontPosition) {
        this.frontPosition = frontPosition;
    }

    /**
     * Returns the position of the mid-point of this vehicle.
     * 
     * @return position of the mid-point of this vehicle
     */
    public final double getMidPosition() {
        return frontPosition - (0.5 * length);
    }

    /**
     * Sets the reference position of this vehicle by the rear position.
     * 
     * @param rearPosition
     *            new rear position
     */
    public final void setRearPosition(double rearPosition) {
        this.frontPosition = rearPosition + length;
    }

    /**
     * Returns the position of the rear of this vehicle.
     * 
     * @return position of the rear of this vehicle
     */
    public final double getRearPosition() {
        return frontPosition - length;
    }

    public final double getFrontPositionOld() {
        return frontPositionOld;
    }

    /**
     * Returns this vehicle's speed. in m/s
     * 
     * @return this vehicle's speed, in m/s
     */
    public final double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed. in m/s
     * 
     * @param speed
     *            in m/s
     *            the new speed in m/s
     */
    public final void setSpeed(double speed) {
        this.speed = speed;
    }

    public final double getSpeedlimit() {
        return speedlimit;
    }

    /**
     * externally given speedlimit
     * 
     * @param speedlimit
     */
    public final void setSpeedlimit(double speedlimit) {
        this.speedlimit = speedlimit;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getSlope() {
        return slope;
    }

    public double getAcc() {
        return acc;
    }

    public double accModel() {
        return accModel;
    }

    public double getDistanceToTrafficlight() {
        return trafficLightApproaching.getDistanceToTrafficlight();
    }

    /**
     * Returns this vehicle's id.
     * 
     * @return vehicle's id
     * 
     */
    public final long getId() {
        return id;
    }

    public final int getVehNumber() {
        return vehNumber == VEHICLE_NUMBER_NOT_SET ? (int) id : vehNumber;
    }

    public void setVehNumber(int vehNumber) {
        this.vehNumber = vehNumber;
    }

    public double getNetDistance(Vehicle vehFront) {
        if (vehFront == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        final double netGap = vehFront.getRearPosition() - getFrontPosition();
        return netGap;
    }

    public double getBrutDistance(Vehicle vehFront) {
        if (vehFront == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return vehFront.getFrontPosition() - getFrontPosition();
    }

    public final double getRelSpeed(Vehicle vehFront) {
        if (vehFront == null) {
            return 0;
        }
        return speed - vehFront.getSpeed();
    }

    public void updateAcceleration(double dt, RoadSegment roadSegment, LaneSegment laneSegment, LaneSegment leftLaneSegment, double alphaT,
            double alphaV0) {

        accOld = acc;
        // acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            final Vehicle vehFront = laneSegment.frontVehicle(this);
            if (getNetDistance(vehFront) < MovsimConstants.CRITICAL_GAP) {
                accError = Math.min(accError, 0.); // !!!
            }
        }

        // TODO extract to super class
        double alphaTLocal = alphaT;
        double alphaV0Local = alphaV0;
        double alphaALocal = 1;

        // TODO check concept here: combination with alphaV0 (consideration of reference v0 instead of dynamic v0 which
        // depends on speedlimits)
        if (memory != null) {
            final double v0 = longitudinalModel.getDesiredSpeed();
            memory.update(dt, speed, v0);
            alphaTLocal *= memory.alphaT();
            alphaV0Local *= memory.alphaV0();
            alphaALocal *= memory.alphaA();
        }

        accModel = calcAccModel(laneSegment, leftLaneSegment, alphaTLocal, alphaV0Local, alphaALocal);

        // moderate acceleration by traffic lights or for preparing mandatory lane changes to exit sliproads
        acc = moderateAcceleration(accModel, roadSegment);

        acc = Math.max(acc + accError, -maxDeceleration); // limited to maximum deceleration
    }
    /**
     * Moderates this vehicle's acceleration according to factors other than the LDM. For
     * example, the presence of traffic lights, motorway exits or tactical considerations (say,
     * the desire to make a lane change).
     * 
     * @param acc
     *            acceleration as calculated by LDM
     * @param roadSegment 
     * @return moderated acceleration
     */
    protected final double moderateAcceleration(double acc, RoadSegment roadSegment) {
        double moderatedAcc = acc;
        // if (acc < -7.5) {
        //     System.out.println("High braking, vehicle:" + id + " acc:" + acc); //$NON-NLS-1$ //$NON-NLS-2$
        // }
        if (trafficLightApproaching != null) {
            moderatedAcc = accelerationConsideringTrafficLight(moderatedAcc);
        }
        moderatedAcc = accelerationConsideringExit(moderatedAcc, roadSegment);
        return moderatedAcc;
    }

    /**
     * Returns this vehicle's acceleration considering the traffic light.
     * 
     * @return acceleration considering traffic light
     */
    protected double accelerationConsideringTrafficLight(double acc) {
        double moderatedAcc = acc;
        // consider red or amber/yellow traffic light:
        if (trafficLightApproaching.considerTrafficLight()) {
            moderatedAcc = Math.min(moderatedAcc, trafficLightApproaching.accApproaching());
        }
        return moderatedAcc;
    }

    /**
     * Returns this vehicle's acceleration considering the exit.
     * @param roadSegment 
     * 
     * @return acceleration considering exit
     */
    protected double accelerationConsideringExit(double acc, RoadSegment roadSegment) {
        assert roadSegment.id()==roadSegmentId;
        
        double moderatedAcc = acc;
        if (exitRoadSegmentId == roadSegment.id() && getLane() != Lane.LANE1) {
            // the vehicle is in the exit road segment, but not in the exit lane
            // react to vehicle ahead in exit lane
            final LaneSegment exitLaneSegment = roadSegment.laneSegment(Lane.MOST_RIGHT_LANE);
            if(exitLaneSegment!=null && exitLaneSegment.type() == Lane.Type.EXIT){
                // System.out.println("on roadsegment with exit lane. veh id="+id);
                //final double distanceToExit = getDistanceToRoadSegmentEnd();
                //if (distanceToExit > 0) { // if negative have passed exit
                //if (distanceToExit < LaneChangeModel.distanceBeforeExitMustChangeLanes) {
                // treat the end of the exit lane as a stopped vehicle
                double accToVehicleInExitLane = longitudinalModel.calcAcc(this, exitLaneSegment.frontVehicle(this));
                // limit deceleration
                accToVehicleInExitLane = Math.max(accToVehicleInExitLane, -4.0);
                logger.error("accToVehicleInExitLane={}, own speed={}", accToVehicleInExitLane, speed);  // TODO
                //moderatedAcc = Math.min(acc, this.longitudinalModel.calcAccSimple(distanceToExit, getSpeed(), getSpeed()));
                moderatedAcc = Math.min(acc, accToVehicleInExitLane);
                //}
            }
        }
        
        
//        // consider upstream (source) road segment
//        final RoadSegment sinkRoadSegment = roadSegment.sinkRoadSegment(Lane.MOST_RIGHT_LANE);
//        if(sinkRoadSegment!=null && exitRoadSegmentId == sinkRoadSegment.id()){
//            // consider most upstream vehicle in exit lane downstream
//            final LaneSegment exitLaneSegmentDownstream = sinkRoadSegment.laneSegment(Lane.MOST_RIGHT_LANE);
//            if(exitLaneSegmentDownstream!=null && exitLaneSegmentDownstream.type() == Lane.Type.EXIT){
//                System.out.println("on roadsegment upstream of exit lane. veh id="+id);
//                double accToVehicleInExitLaneDownstream = longitudinalModel.calcAcc(this, exitLaneSegmentDownstream.rearVehicle());
//                // limit deceleration
//                accToVehicleInExitLaneDownstream = Math.max(accToVehicleInExitLaneDownstream, -4.0);
//                logger.error("accToVehicleInExitLane downstream={}, own speed={}", accToVehicleInExitLaneDownstream, speed);  // TODO
//                //moderatedAcc = Math.min(acc, this.longitudinalModel.calcAccSimple(distanceToExit, getSpeed(), getSpeed()));
//                moderatedAcc = Math.min(acc, accToVehicleInExitLaneDownstream);
//            }
//            
//        }
        
        return moderatedAcc;
    }

    // TODO this acceleration is the base for MOBIL decision: could consider
    // also noise (for transfering stochasticity to lane-changing) and other
    // relevant traffic situations!

    public double calcAccModel(LaneSegment laneSegment, LaneSegment leftLaneSegment) {
        return calcAccModel(laneSegment, leftLaneSegment, 1.0, 1.0, 1.0);
    }

    private double calcAccModel(LaneSegment laneSegment, LaneSegment leftLaneSegment, double alphaTLocal,
            double alphaV0Local, double alphaALocal) {
        if (longitudinalModel == null) {
            return 0.0;
        }

        final double acc;

        if (laneChangeModel != null && laneChangeModel.isInitialized() && laneChangeModel.withEuropeanRules()) {
            acc = longitudinalModel.calcAccEur(laneChangeModel.vCritEurRules(), this, laneSegment, leftLaneSegment,
                    alphaTLocal, alphaV0Local, alphaALocal);
        } else {
            acc = longitudinalModel.calcAcc(this, laneSegment, alphaTLocal, alphaV0Local, alphaALocal);
        }

        return acc;
    }

    /**
     * Update position and speed. Case distinction between cellular automata, Newell and continuos models/iterated maps
     * 
     * @param dt
     *            delta-t, simulation time interval, seconds
     */
    public void updatePositionAndSpeed(double dt) {
        frontPositionOld = frontPosition;
        if (longitudinalModel != null && longitudinalModel.isCA()) {
            speed = (int) (speed + dt * acc + 0.5);
            final int advance = (int) (frontPosition + dt * speed + 0.5);
            totalTraveledDistance += (advance - frontPosition);
            frontPosition = advance;
        } else if (longitudinalModel != null && (longitudinalModel.modelName() == ModelName.NEWELL)) {
            // Newell position update: Different to continuous microscopic models and iterated maps.
            // See chapter 10.7 english book version
            if (speed < 0) {
                speed = 0;
            }
            final double advance = speed * dt + acc * dt * dt;
            speed += dt * acc;
            frontPosition += advance;
            totalTraveledDistance += advance;
            if (speed < 0) {
                speed = 0;
                acc = 0;
            }
        } else {
            // continuous microscopic models and iterated maps
            if (speed < 0) {
                speed = 0;
            }
            final double advance = (acc * dt >= -speed) ? speed * dt + 0.5 * acc * dt * dt : -0.5 * speed * speed / acc;
            frontPosition += advance;
            totalTraveledDistance += advance;
            speed += dt * acc;
            if (speed < 0) {
                speed = 0;
                acc = 0;
            }
        }
    }

    public final int getLane() {
        return lane;
    }

    public final void setLane(int lane) {
        assert this.lane != lane;
        laneOld = this.lane;
        this.lane = lane;
        targetLane = Lane.NONE;
    }

    public boolean hasReactionTime() {
        return (reactionTime + MovsimConstants.SMALL_VALUE > 0);
    }

    /**
     * Update.
     * 
     * @param simulationTime
     *            current simulation time, seconds
     * @param trafficLight
     */
    public void updateTrafficLightApproaching(double simulationTime, TrafficLight trafficLight) {
        trafficLightApproaching.update(this, simulationTime, trafficLight, longitudinalModel);
    }

    public LaneChangeModel getLaneChangeModel() {
        return laneChangeModel;
    }

    public void setLaneChangeModel(LaneChangeModel lcModel) {
        this.laneChangeModel = lcModel;
    }

    public LongitudinalModelBase getLongitudinalModel() {
        return longitudinalModel;
    }

    public void setLongitudinalModel(LongitudinalModelBase longitudinalModel) {
        this.longitudinalModel = longitudinalModel;
    }

    // ---------------------------------------------------------------------------------
    // lane-changing related methods
    // ---------------------------------------------------------------------------------

    public boolean considerLaneChange(double dt, RoadSegment roadSegment) {

        // no lane changing when not configured in xml.
        if (laneChangeModel == null || !laneChangeModel.isInitialized()) {
            return false;
        }

        // no lane-changing decision necessary for one-lane road
        if (roadSegment.laneCount() < 2) {
            return false;
        }

        if (inProcessOfLaneChange()) {
            updateLaneChangeDelay(dt);
            return false;
        }

        // if not in lane-changing process do determine if new lane is more attractive and lane change is possible
        LaneChangeDecision lcDecision = laneChangeModel.makeDecision(roadSegment);
        final int laneChangeDirection = lcDecision.getDirection();

        // initiates a lane change: set targetLane to new value the lane will be assigned by the vehicle container !!
        if (laneChangeDirection != Lane.NO_CHANGE) {
            setTargetLane(lane + laneChangeDirection);
            resetDelay();
            updateLaneChangeDelay(dt);
            logger.debug("do lane change to={} into target lane={}", laneChangeDirection, targetLane);
            return true;
        }
        return false;
    }

    public int getTargetLane() {
        return targetLane;
    }

    /**
     * Sets the target lane.
     * 
     * @param targetLane
     *            the new target lane
     */
    private void setTargetLane(int targetLane) {
        assert targetLane >= 0;
        assert targetLane != lane;
        this.targetLane = targetLane;
    }

    public boolean inProcessOfLaneChange() {
        return (tLaneChangeDelay > 0 && tLaneChangeDelay < FINITE_LANE_CHANGE_TIME_S);
    }

    /**
     * Reset delay.
     */
    private void resetDelay() {
        tLaneChangeDelay = 0;
    }

    /**
     * Update lane changing delay.
     * 
     * @param dt
     *            the dt
     */
    private void updateLaneChangeDelay(double dt) {
        tLaneChangeDelay += dt;
    }

    public double getContinousLane() {
        if (inProcessOfLaneChange()) {
            final double fractionTimeLaneChange = Math.min(1, tLaneChangeDelay / FINITE_LANE_CHANGE_TIME_S);
            return fractionTimeLaneChange * lane + (1 - fractionTimeLaneChange) * laneOld;
        }
        return getLane();
    }

    // ---------------------------------------------------------------------------------
    // braking lights for neat viewers
    // ---------------------------------------------------------------------------------

    public boolean isBrakeLightOn() {
        updateBrakeLightStatus();
        return isBrakeLightOn;
    }

    /**
     * Update brake light status.
     */
    private void updateBrakeLightStatus() {
        if (isBrakeLightOn) {
            if (acc > -THRESHOLD_BRAKELIGHT_OFF || speed <= 0.0001) {
                isBrakeLightOn = false;
            }
        } else if (accOld > -THRESHOLD_BRAKELIGHT_ON && acc < -THRESHOLD_BRAKELIGHT_ON) {
            isBrakeLightOn = true;
        }
    }

    // ---------------------------------------------------------------------------------
    // converter for scaled quantities in cellular automata
    // ---------------------------------------------------------------------------------

    public PhysicalQuantities physicalQuantities() {
        return physQuantities;
    }

    public double getActualFuelFlowLiterPerS() {
        if (fuelModel == null) {
            return 0;
        }
        return fuelModel.getFuelFlowInLiterPerS(speed, acc);
    }

    // Added as part of xodr merge
    /**
     * 'Not Set' road exit position value, guaranteed not to be used by any vehicles.
     */
    public static final double EXIT_POSITION_NOT_SET = -1.0;

    /**
     * Vehicle type.
     */
    public static enum Type {
        /**
         * Vehicle type has not been set.
         */
        NONE,
        /**
         * Vehicle is an immovable obstacle.
         */
        OBSTACLE,
        /**
         * Standard vehicle.
         */
        VEHICLE,
        /**
         * The vehicle is a floating car, used to gather data about traffic conditions.
         */
        FLOATING_CAR
    }

    private Type type;

    /**
     * Returns this vehicle's type.
     * 
     * @return vehicle's type
     * 
     */
    public final Vehicle.Type type() {
        return type;
    }

    /**
     * Sets this vehicle's type.
     * 
     * @param type
     * 
     */
    public final void setType(Vehicle.Type type) {
        this.type = type;
    }

    /**
     * <p>
     * Called when vehicle changes road segments (and possibly also lanes) at a link or junction.
     * </p>
     * <p>
     * Although the change of lanes is immediate, <code>lane</code>, <code>prevLane</code> and
     * <code>timeAtWhichLastChangedLanes</code> are used to interpolate this vehicle's lateral position and so give the
     * appearance of a smooth lane change.
     * </p>
     * 
     * @param newLane
     * @param exitPos
     */
    public void moveToNewRoadSegment(RoadSegment newRoadSegment, int newLane, double newRearPosition, double exitPos) {
        // distanceTravelledToStartOfRoadSegment += rearPosition - newRearPos;
        final int delta = laneOld - lane;
        lane = newLane;
        laneOld = lane + delta;
        if (laneOld >= newRoadSegment.laneCount()) {
            laneOld = newRoadSegment.laneCount() - 1;
        } else if (laneOld < Lane.LANE1) {
            laneOld = Lane.LANE1;
        }
        setRearPosition(newRearPosition);
        setRoadSegment(newRoadSegment.id(), newRoadSegment.roadLength());
        // this.exitEndPos = exitPos;
        // trafficLight = null;
        // speedLimit = 0.0;
    }

    /**
     * Sets the road segment properties for this vehicle. Invoked after a vehicle has moved onto a new road segment.
     * 
     * @param roadSegmentId
     * @param roadSegmentLength
     * 
     */
    public final void setRoadSegment(int roadSegmentId, double roadSegmentLength) {
        this.roadSegmentId = roadSegmentId;
        this.roadSegmentLength = roadSegmentLength;
        // assume this vehicle does not exit on this road segment
        if (roadSegmentId != exitRoadSegmentId) {
            exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
        }
        if (route != null && routeIndex < route.size()) {
            final RoadSegment routeRoadSegment = route.get(routeIndex);
            ++routeIndex;
            if (routeRoadSegment.id() == roadSegmentId) {
                // this vehicle is on the route
                if (routeIndex < route.size()) {
                    // there is another roadSegment on the route
                    // so check if the next roadSegment is joined to an exit lane
                    // of the current roadSegment
                    final RoadSegment nextRouteRoadSegment = route.get(routeIndex);
                    if (routeRoadSegment.exitsOnto(nextRouteRoadSegment.id())) {
                        // this vehicle needs to exit on this roadSegment
                        exitRoadSegmentId = roadSegmentId;
                    } else {
                        if (routeIndex + 1 < route.size()) {
                            // there is another roadSegment on the route
                            // so check if the next roadSegment is joined to an exit lane
                            // of the current roadSegment
                            final RoadSegment nextNextRouteRoadSegment = route.get(routeIndex + 1);
                            if (nextRouteRoadSegment.exitsOnto(nextNextRouteRoadSegment.id())) {
                                // this vehicle needs to exit on this roadSegment
                                exitRoadSegmentId = roadSegmentId;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the id of the road segment currently occupied by this vehicle.
     * 
     * @return id of the road segment currently occupied by this vehicle
     */
    public final int roadSegmentId() {
        return roadSegmentId;
    }

    /**
     * Returns the id of the road segment in which this vehicle wishes to exit.
     * 
     * @return id of exit road segment
     */
    public final int exitRoadSegmentId() {
        return exitRoadSegmentId;
    }

    /**
     * Sets the id of the road segment in which this vehicle wishes to exit.
     */
    public final void setExitRoadSegmentId(int exitRoadSegmentId) {
        this.exitRoadSegmentId = exitRoadSegmentId;
    }

    public final double totalTraveledDistance() {
        return totalTraveledDistance;
    }

    public double getMaxDeceleration() {
        return maxDeceleration;
    }
    
    public double getDistanceToRoadSegmentEnd(){
        if(roadSegmentLength<=0){
            return -1;  
        }
        return roadSegmentLength - getFrontPosition();
    }
}
