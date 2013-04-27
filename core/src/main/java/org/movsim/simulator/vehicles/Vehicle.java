/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import javax.annotation.Nullable;

import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.consumption.model.EnergyFlowModel;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegment.TrafficLightLocationWithDistance;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.trafficlights.TrafficLightLocation;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel.LaneChangeDecision;
import org.movsim.simulator.vehicles.longitudinalmodel.Memory;
import org.movsim.simulator.vehicles.longitudinalmodel.TrafficLightApproaching;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase.ModelName;
import org.movsim.utilities.Colors;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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
 * A vehicle has the kinematic attributes of position, velocity and acceleration. A vehicle's position is given by the position of the front
 * of the vehicle on the road segment and also by the vehicle's lane.
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Vehicle.class);

    protected static final int INITIAL_ID = 1;
    protected static final int INITIAL_TEMPLATE_ID = -1;

    private static long nextId = INITIAL_ID;
    private static long nextTemplateId = INITIAL_TEMPLATE_ID;

    /**
     * 'Not Set' vehicle id value, guaranteed not to be used by any vehicles.
     */
    public static final int ID_NOT_SET = -1;
    private static final int VEHICLE_NUMBER_NOT_SET = -1;
    public static final int LANE_NOT_SET = -1;

    /**
     * 'Not Set' road segment id value, guaranteed not to be used by any vehicles.
     */
    public static final int ROAD_SEGMENT_ID_NOT_SET = -1;

    /** in m/s^2 */
    private final static double THRESHOLD_BRAKELIGHT_ON = 0.2;

    /** in m/s^2 */
    private final static double THRESHOLD_BRAKELIGHT_OFF = 0.1;

    /** needs to be > 0 */
    private final static double FINITE_LANE_CHANGE_TIME_S = 7;

    private final String label;

    private double length; // can be set in micro-boundary conditions
    private final double width;
    private double weight; // used in a project, not in fuel consumption calculation!

    /** The front position of the vehicle. The reference position. */
    private double frontPosition;

    /** The old front position of the vehicle. */
    private double frontPositionOld;

    /** The total distance traveled */
    private double totalTravelDistance;
    private double totalTravelTime;
    private double totalFuelUsedLiters;

    private double speed;

    /** The acceleration as calculated by the longitudinal driver model. */
    private double accModel;

    /**
     * The actual acceleration. This is the acceleration calculated by the LDM moderated by other
     * factors, such as traffic lights
     */
    private double acc;

    private double accOld;

    /** The max deceleration . */
    private final double maxDeceleration;

    /** The unique id of the vehicle. */
    final long id;

    /** constant random number between 0 and 1 used for random output selections */
    final double randomFix;

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
    /** can be null */
    private LaneChangeModel laneChangeModel;

    /** can be null */
    private Memory memory = null;
    /** Acceleration noise model. Can be null */
    private Noise noise = null;

    private int color;
    /** color object cache */
    private Object colorObject;

    private final TrafficLightApproaching trafficLightApproaching;

    /** can be null */
    private EnergyFlowModel fuelModel;
    /** can be null */
    private Route route;

    private boolean isBrakeLightOn;

    private PhysicalQuantities physQuantities;

    // Exit Handling
    private int roadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
    private double roadSegmentLength;
    private int exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
    private int originRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

    // information handling
    private String infoComment = "";

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

    public Vehicle(String label, LongitudinalModelBase longitudinalModel, VehiclePrototypeConfiguration vehInput,
            @Nullable LaneChangeModel lcModel) {
        Preconditions.checkNotNull(longitudinalModel);
        Preconditions.checkNotNull(vehInput);
        this.label = label;
        this.length = vehInput.getLength();
        this.width = vehInput.getWidth();
        this.maxDeceleration = vehInput.getMaximumDeceleration();

        id = nextId++;
        randomFix = MyRandom.nextDouble();

        initialize();
        this.longitudinalModel = longitudinalModel;
        physQuantities = new PhysicalQuantities(this);

        this.laneChangeModel = lcModel;
        if (laneChangeModel != null) {
            laneChangeModel.initialize(this);
        }
        trafficLightApproaching = new TrafficLightApproaching();

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
        randomFix = MyRandom.nextDouble();
        this.length = length;
        setRearPosition(rearPosition);
        this.speed = speed;
        this.lane = lane;
        this.laneOld = lane;
        this.width = width;
        this.color = 0;
        fuelModel = null;
        trafficLightApproaching = null;
        maxDeceleration = 10.0;
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
        id = source.id; // TODO id not unique in this case
        randomFix = source.randomFix;
        type = source.type;
        frontPosition = source.frontPosition;
        speed = source.speed;
        lane = source.lane;
        laneOld = source.laneOld;
        length = source.length;
        width = source.width;
        color = source.color;
        trafficLightApproaching = source.trafficLightApproaching;
        maxDeceleration = source.maxDeceleration;
        laneChangeModel = source.laneChangeModel;
        longitudinalModel = source.longitudinalModel;
        label = source.label;
        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
        slope = source.slope;
        route = source.route;
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
     * Returns the vehicle's physical length plus the dynamic contribution from a model's minimum gap.
     * 
     * @return the effective length of a vehicle in a standstill
     */
    public double getEffectiveLength() {
        return getLength() + getLongitudinalModel().getMinimumGap();
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
     * Sets the front position of this vehicle.
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
     * Sets the rear position of this vehicle.
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

    /**
     * Returns the actual acceleration. This is the acceleration calculated by the LDM moderated by other
     * factors, such as traffic lights
     * 
     * @return the vehicle acceleration
     */
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

    /**
     * returns the net distance (from front bumper to rear bumper) to the front vehicle, returns infinity gap if front
     * vehicle is null.
     * 
     * @param frontVehicle
     * @return
     */
    public double getNetDistance(Vehicle frontVehicle) {
        if (frontVehicle == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return frontVehicle.getRearPosition() - getFrontPosition();
    }

    /**
     * returns the brut distance (net distance plus vehicle length of front vehicle) to the front vehicle, returns
     * infinity gap if front vehicle is null.
     * 
     * @param frontVehicle
     * @return
     */
    public double getBrutDistance(Vehicle frontVehicle) {
        if (frontVehicle == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return frontVehicle.getFrontPosition() - getFrontPosition();
    }

    /**
     * returns the net distance (from rear bumper to front bumper) to the rear vehicle, returns infinity gap if front
     * vehicle is null.
     * 
     * @param rearVehicle
     * @return
     */
    public double getNetDistanceToRearVehicle(Vehicle rearVehicle) {
        if (rearVehicle == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return getRearPosition() - rearVehicle.getFrontPosition();
    }

    public final double getRelSpeed(Vehicle frontVehicle) {
        if (frontVehicle == null) {
            return 0;
        }
        return speed - frontVehicle.getSpeed();
    }

    public void updateAcceleration(double dt, RoadSegment roadSegment, LaneSegment laneSegment,
            LaneSegment leftLaneSegment, double alphaT, double alphaV0) {

        accOld = acc;
        // acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            final Vehicle frontVehicle = laneSegment.frontVehicle(this);
            if (getNetDistance(frontVehicle) < MovsimConstants.CRITICAL_GAP) {
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
            moderatedAcc = accelerationConsideringTrafficLight(moderatedAcc, roadSegment);
        }
        moderatedAcc = accelerationConsideringExit(moderatedAcc, roadSegment);
        return moderatedAcc;
    }

    /**
     * Returns this vehicle's acceleration considering the traffic light.
     * 
     * @param roadSegment
     * 
     * @return acceleration considering traffic light
     */
    protected double accelerationConsideringTrafficLight(double acc, RoadSegment roadSegment) {
        double moderatedAcc = acc;
        TrafficLightLocationWithDistance location = roadSegment.getNextDownstreamTrafficLight(
                getFrontPosition(), lane(), TrafficLightApproaching.MAX_LOOK_AHEAD_DISTANCE);
        if (location != null) {
            LOG.debug("consider trafficlight={}", location.toString());
            updateTrafficLightApproaching(location.trafficLightLocation, location.distance);
            if (trafficLightApproaching.considerTrafficLight()) {
                moderatedAcc = Math.min(acc, trafficLightApproaching.accApproaching());
            }
        }
        return moderatedAcc;
    }

    private void updateTrafficLightApproaching(TrafficLightLocation trafficLightLocation, double distance) {
        assert distance >= 0 : "distance=" + distance;
        trafficLightApproaching.update(this, trafficLightLocation.getTrafficLight(), distance);
    }


    /**
     * Returns this vehicle's acceleration considering the exit.
     * 
     * @param roadSegment
     * 
     * @return acceleration considering exit
     */
    protected double accelerationConsideringExit(double acc, RoadSegment roadSegment) {
        assert roadSegment.id() == roadSegmentId;
        if (exitRoadSegmentId == roadSegment.id() && lane() != Lanes.LANE1) {
            // the vehicle is in the exit road segment, but not in the exit lane
            // react to vehicle ahead in exit lane
            // TODO this reaction is in same situations too short-sighted so that the vehicle in the exit lane must be
            // considered already in the upstream segment
            final LaneSegment exitLaneSegment = roadSegment.laneSegment(roadSegment.trafficLaneMax());
            if (exitLaneSegment != null && exitLaneSegment.type() == Lanes.Type.EXIT) {
                // this front vehicle could also result in negative net distances
                // but the deceleration is limited anyway
                Vehicle frontVehicle = exitLaneSegment.frontVehicle(this);
                double accToVehicleInExitLane = longitudinalModel.calcAcc(this, frontVehicle);
                final double decelLimit = 4.0;
                accToVehicleInExitLane = Math.max(accToVehicleInExitLane, -decelLimit);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String
                            .format("considering exit=%d: veh=%d, distance to front veh in exit lane=%.2f, speed=%.2f, accLimit=%.2f",
                                    exitRoadSegmentId, getId(), getNetDistance(frontVehicle), getSpeed(),
                                    accToVehicleInExitLane));
                }
                return Math.min(acc, accToVehicleInExitLane);
            }
        }
        return acc;
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

        double acc;

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
        totalTravelTime += dt;
        frontPositionOld = frontPosition;
        if (longitudinalModel != null && longitudinalModel.isCA()) {
            speed = (int) (speed + dt * acc + 0.5);
            final int advance = (int) (frontPosition + dt * speed + 0.5);
            totalTravelDistance += (advance - frontPosition);
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
            totalTravelDistance += advance;
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
            totalTravelDistance += advance;
            speed += dt * acc;
            if (speed < 0) {
                speed = 0;
                acc = 0;
            }
        }
        if (fuelModel != null) {
            totalFuelUsedLiters += fuelModel.getFuelFlowInLiterPerS(speed, acc) * dt;
        }
    }

    public final int lane() {
        return lane;
    }

    public final void setLane(int lane) {
        assert lane >= Lanes.MOST_INNER_LANE;
        assert this.lane != lane;
        laneOld = this.lane;
        this.lane = lane;
        targetLane = Lanes.NONE;
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
        if (laneChangeDirection != Lanes.NO_CHANGE) {
            setTargetLane(lane + laneChangeDirection);
            resetDelay();
            updateLaneChangeDelay(dt);
            LOG.debug("do lane change to={} into target lane={}", laneChangeDirection, targetLane);
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
        assert targetLane >= Lanes.MOST_INNER_LANE;
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
        return lane();
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

    private Type type = Type.VEHICLE; // init!

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
     * Although the change of lanes is immediate, <code>lane</code>, <code>prevLane</code> and <code>timeAtWhichLastChangedLanes</code> are
     * used to interpolate this vehicle's lateral position and so give the appearance of a smooth lane change.
     * </p>
     * 
     * @param newLane
     * @param exitPos
     */
    public void moveToNewRoadSegment(RoadSegment newRoadSegment, int newLane, double newRearPosition, double exitPos) {
        // distanceTravelledToStartOfRoadSegment += rearPosition - newRearPos;
        final int delta = 0; // laneOld - lane;
        this.lane = newLane;
        this.laneOld = lane + delta;
        laneOld = Math.max(Lanes.MOST_INNER_LANE, Math.min(laneOld, newRoadSegment.laneCount()));
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
    
    int routeIndex = 0;

    public final void setRoadSegment(int roadSegmentId, double roadSegmentLength) {
        if (originRoadSegmentId == ROAD_SEGMENT_ID_NOT_SET) {
            originRoadSegmentId = roadSegmentId;
        }
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
        // determineExitRoadSegmentId();
    }

    private void determineExitRoadSegmentId() {
        Preconditions.checkArgument(originRoadSegmentId != ROAD_SEGMENT_ID_NOT_SET);
        Preconditions.checkArgument(roadSegmentId != ROAD_SEGMENT_ID_NOT_SET);
        // assume this vehicle does not exit on this road segment
        if (roadSegmentId != exitRoadSegmentId) {
            exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
        }

        int routeIndex = 0;
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
     * Returns the id of the first road segment occupied by this vehicle.
     * 
     * @return id of the first road segment occupied by this vehicle
     */
    public final int originRoadSegmentId() {
        return originRoadSegmentId;
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

    /**
     * Returns the total distance this vehicle has traveled.
     * 
     * @return total travel distance
     */
    public final double totalTravelDistance() {
        return totalTravelDistance;
    }

    /**
     * Returns the total time this vehicle has been on the road network.
     * 
     * @return total travel time
     * 
     * @return
     */
    public final double totalTravelTime() {
        return totalTravelTime;
    }

    /**
     * Returns the total fuel used by this vehicle.
     * 
     * @return total fuel used
     */
    public final double totalFuelUsedLiters() {
        return totalFuelUsedLiters;
    }

    public double getMaxDeceleration() {
        return maxDeceleration;
    }

    public double getDistanceToRoadSegmentEnd() {
        if (roadSegmentLength <= 0) {
            return -1;
        }
        return roadSegmentLength - getFrontPosition();
    }

    @Override
    public String toString() {
        return "Vehicle [label=" + label + ", length=" + length + ", frontPosition=" + frontPosition
                + ", frontPositionOld=" + frontPositionOld + ", speed=" + speed + ", accModel=" + accModel + ", acc="
                + acc + ", accOld=" + accOld + ", id=" + id + ", vehNumber=" + vehNumber + ", lane=" + lane + "]";
    }

    /** returns a constant random number between 0 and 1 */
    public double getRandomFix() {
        return randomFix;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setNoise(Noise noise) {
        this.noise = noise;
    }

    public void setFuelModel(EnergyFlowModel fuelModel) {
        this.fuelModel = fuelModel;
    }

    /**
     * @return the infoComment
     */
    public String getInfoComment() {
        return infoComment;
    }

    /**
     * @param infoComment
     *            the infoComment to set
     */
    public void setInfoComment(String infoComment) {
        this.infoComment = infoComment;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

}
