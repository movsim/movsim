/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles;

import org.movsim.consumption.FuelConsumption;
import org.movsim.input.model.VehicleInput;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.vehicles.lanechanging.LaneChangingModel;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.simulator.vehicles.longitudinalmodel.Memory;
import org.movsim.simulator.vehicles.longitudinalmodel.TrafficLightApproaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Vehicle.
 */
public class Vehicle {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Vehicle.class);
    // constants
    protected static final int INITIAL_ID = 1;
    protected static final int INITIAL_TEMPLATE_ID = -1;
    /**
     * 'Not Set' vehicle id value, guaranteed not to be used by any vehicles.
     */
    public static final int ID_NOT_SET = -1;
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

    /** The label. */
    private final String label;

    /** The length. */
    private final double length;
    private final double width;

    /** The position. */
    private double midPosition;

    /** The old position. */
    private double positionOld;
    
    /** The total distance travelled */
    private double totalTraveledDistance;

    /** The speed. */
    private double speed;

    /** The acceleration as calculated by the longitudinal driver model. */
    private double accModel;

    /** The actual acceleration. This is the acceleration calculated by the LDM moderated by other
     * factors, such as traffic lights
     **/
    private double acc;

    private double accOld;

    /** The reaction time. */
    private final double reactionTime;

    /** The max deceleration . */
    private final double maxDecel;

    /** The id. */
    long id;

    /** The vehicle number. */
    private int vehNumber;

    /** The lane. */
    private int lane;
    private int laneOld;

    /** variable for remembering new target lane when assigning to new vehContainerLane */
    private int targetLane;

    /** finite lane-changing duration */
    private double tLaneChangingDelay;

    /** The speed limit. */
    private double speedlimit;

    /** The longitudinal model. */
    private LongitudinalModelBase longitudinalModel;

    /** The lane-changing model. */
    private LaneChangingModel lcModel;

    /** The memory. */
    private Memory memory = null;

    /** The noise. */
    private Noise noise = null;
    // Color
    private int color;
    private Object colorObject; // color object cache

    /** The traffic light approaching. */
    private final TrafficLightApproaching trafficLightApproaching;

    private final FuelConsumption fuelModel; // can be null

    private boolean isBrakeLightOn;

    private PhysicalQuantities physQuantities;
    // Exit Handling
    private int roadSegmentId;
    private double roadSegmentLength;
    private final int exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

    private long roadId;  // FIXME meaning? on single roadsegment with roadSegmentId=1 this roadId is 0!? 
    private static long nextId = INITIAL_ID;
    private static long nextTemplateId = INITIAL_TEMPLATE_ID;

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

    /**
     * Instantiates a new vehicle impl.
     * 
     * @param label
     *            the label
     * @param id
     *            the id
     * @param longitudinalModel
     *            the longitudinal ("car-following") model.
     * @param vehInput
     *            the veh input
     * @param cyclicBuffer
     *            the cyclic buffer
     * @param lcModel
     *            the lanechange model
     */
    public Vehicle(String label, final LongitudinalModelBase longitudinalModel, final VehicleInput vehInput,
            final Object cyclicBuffer, final LaneChangingModel lcModel, final FuelConsumption fuelModel) {
        this.label = label;
        id = nextId++;
        this.fuelModel = fuelModel;

        length = vehInput.getLength();
        width = MovsimConstants.VEHICLE_WIDTH;
        reactionTime = vehInput.getReactionTime();
        maxDecel = vehInput.getMaxDeceleration();

        initialize();
        // longitudinal ("car-following") model
        this.longitudinalModel = longitudinalModel;
        physQuantities = new PhysicalQuantities(this);

        // lane-changing model
        this.lcModel = lcModel;
        lcModel.initialize(this);

        // no effect if model is not configured with memory effect
        if (vehInput.isWithMemory()) {
            memory = new Memory(vehInput.getMemoryInputData());
        }

        if (vehInput.isWithNoise()) {
            noise = new Noise(vehInput.getNoiseInputData());
        }

        trafficLightApproaching = new TrafficLightApproaching();

        // needs to be > 0 to avoid lane-changing over 2 lanes in one update
        // step
        assert FINITE_LANE_CHANGE_TIME_S > 0;

    }

    /**
     * Constructor.
     */
    public Vehicle(double rearPosition, double speed, int lane, double length, double width) {
        assert rearPosition >= 0.0;
        assert speed >= 0.0;
        // assert lane >= Lane.LANE1;
        id = nextId++;
        this.length = length;
        setRearPosition(rearPosition);
        this.speed = speed;
        this.lane = lane;
        this.width = width;
        this.color = 0;
        fuelModel = null;
        trafficLightApproaching = null;
        reactionTime = 0.0;
        maxDecel = 0.0;
        lcModel = null;
        longitudinalModel = null;
        label = "";
        physQuantities = new PhysicalQuantities(this);
    }

    /**
     * Copy constructor.
     * 
     * @param source
     */
    public Vehicle(Vehicle source) {
        id = source.id;
        type = source.type;
        midPosition = source.midPosition;
        speed = source.speed;
        lane = source.lane;
        length = source.length;
        width = source.width;
        color = source.color;
        fuelModel = source.fuelModel;
        trafficLightApproaching = source.trafficLightApproaching;
        reactionTime = source.reactionTime;
        maxDecel = source.maxDecel;
        lcModel = source.lcModel;
        longitudinalModel = source.longitudinalModel;
        label = source.label;
    }

    /**
     * Constructor.
     */
    public Vehicle(org.movsim.simulator.vehicles.Vehicle.Type car, LongitudinalModelBase ldm, Object lcm, double length,
            double width, int i) {
        id = nextId++;
        this.length = length;
        setRearPosition(0.0);
        this.speed = 0.0;
        this.lane = Lane.NONE;
        this.width = width;
        this.color = 0;
        fuelModel = null;
        trafficLightApproaching = null;
        reactionTime = 0.0;
        maxDecel = 0.0;
        lcModel = null;
        longitudinalModel = ldm;
        label = "";
    }

    private void initialize() {
        positionOld = 0;
        midPosition = 0;
        speed = 0;
        acc = 0;
        isBrakeLightOn = false;

        speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#init(double, double, int)
     */

    // central book-keeping of lanes (lane and laneOld)

    public void init(double pos, double v, int lane, long roadId) {
        this.laneOld = this.lane; // remember previous lane
        this.roadId = roadId;
        this.midPosition = pos;
        this.positionOld = pos;
        this.speed = v;
        // targetlane not needed anymore for book-keeping, vehicle is in new
        // lane
        this.targetLane = this.lane = lane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getLabel()
     */
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
     * Sets this vehicle's color object cache value. Primarily of use by AWT which rather inefficiently uses objects rather than integers to
     * represent color values. Note that an object is cached so Vehicle.java has no dependency on AWT.
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
    public double getLength() {
        return length;
    }

    /**
     * Returns this vehicle's width.
     * 
     * @return vehicle's width, in meters
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns position of the front of this vehicle.
     * 
     * @return position of the front of this vehicle
     */
    public double getFrontPosition() {
        return midPosition + 0.5 * length;
    }

    /**
     * Sets the position of the rear of this vehicle.
     * 
     * @param rearPosition
     *            new rear position
     */
    public final void setFrontPosition(double frontPosition) {
        this.midPosition = frontPosition - 0.5 * length;
    }

    /**
     * Sets the position of the midpoint of this vehicle.
     * 
     * @param midPosition
     *            new  mid position
     */
    public void setMidPosition(double position) {
        this.midPosition = position;
    }

    /**
     * Returns the position of the mid-point of this vehicle.
     * 
     * @return position of the mid-point of this vehicle
     */
    public double getMidPosition() {
        return midPosition;
    }

    /**
     * Sets the position of the rear of this vehicle.
     * 
     * @param rearPosition
     *            new rear position
     */
    public final void setRearPosition(double rearPosition) {
        this.midPosition = rearPosition + 0.5 * length;
    }

    /**
     * Returns the position of the rear of this vehicle.
     * 
     * @return position of the rear of this vehicle
     */
    public double getRearPosition() {
        return midPosition - 0.5 * length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#oldPosition()
     */

    public double getPositionOld() {
        return positionOld;
    }

    /**
     * Returns this vehicle's speed.
     * 
     * @return this vehicle's speed, in m/s
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed.
     * 
     * @param speed
     *            the new speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#speedlimit()
     */

    public double getSpeedlimit() {
        return speedlimit;
    }

    // externally given speedlimit
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#setSpeedlimit(double)
     */

    public void setSpeedlimit(double speedlimit) {
        this.speedlimit = speedlimit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#acc()
     */

    public double getAcc() {
        return acc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#accModel()
     */

    public double accModel() {
        return accModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#distanceToTrafficlight()
     */
    public double getDistanceToTrafficlight() {
        return trafficLightApproaching.getDistanceToTrafficlight();
    }

    /**
     * Returns this vehicle's id.
     * 
     * @return vehicle's id
     * 
     */
    public long getId() {
        return id;
    }

    public long getRoadId() {
        return roadId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getVehNumber()
     */

    public int getVehNumber() {
        return vehNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#setVehNumber(int)
     */

    public void setVehNumber(int vehNumber) {
        this.vehNumber = vehNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#netDistance(org.movsim.simulator .vehicles.Vehicle)
     */
    public double getNetDistance(final Vehicle vehFront) {
        if (vehFront == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        final double netGap = vehFront.getMidPosition() - midPosition - 0.5 * (getLength() + vehFront.getLength());
        return netGap;
    }
    
    public double getBrutDistance(final Vehicle vehFront) {
        if (vehFront == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return vehFront.getMidPosition() - midPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#relSpeed(org.movsim.simulator.vehicles .Vehicle)
     */

    public double getRelSpeed(Vehicle vehFront) {
        if (vehFront == null) {
            return 0;
        }
        return (speed - vehFront.getSpeed());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#calcAcceleration(double, org.movsim.simulator.vehicles.VehicleContainer, double, double)
     */

    public void calcAcceleration(double dt, final LaneSegment vehContainer, final LaneSegment vehContainerLeftLane,
            double alphaT, double alphaV0) {

        accOld = acc;
        // acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            final Vehicle vehFront = vehContainer.frontVehicle(this);
            if (getNetDistance(vehFront) < MovsimConstants.CRITICAL_GAP) {
                accError = Math.min(accError, 0.); // !!!
            }
            // logger.debug("accError = {}", accError);
        }

        // TODO extract to super class
        double alphaTLocal = alphaT;
        double alphaV0Local = alphaV0;
        double alphaALocal = 1;

        // TODO check concept here: combination with alphaV0 (consideration of
        // reference v0 instead of dynamic v0 which depends on speedlimits)
        if (memory != null) {
            final double v0 = longitudinalModel.getDesiredSpeedParameterV0();
            memory.update(dt, speed, v0);
            alphaTLocal *= memory.alphaT();
            alphaV0Local *= memory.alphaV0();
            alphaALocal *= memory.alphaA();
        }

        accModel = calcAccModel(vehContainer, vehContainerLeftLane, alphaTLocal, alphaV0Local, alphaALocal);

        // consider red or amber/yellow traffic light:
        if (trafficLightApproaching != null && trafficLightApproaching.considerTrafficLight()) {
            acc = Math.min(accModel, trafficLightApproaching.accApproaching());
            // logger.debug("accModel = {}, accTrafficLight = {}", accModel,
            // accTrafficLight );
        } else {
            acc = accModel;
        }

        acc = Math.max(acc + accError, -maxDecel); // limited to maximum
                                                   // deceleration
        // logger.debug("acc = {}", acc );
    }

    // TODO this acceleration is the base for MOBIL decision: could consider
    // also noise (for transfering stochasticity to lane-changing) and other
    // relevant traffic situations!
    //

    public double calcAccModel(final LaneSegment vehContainer, final LaneSegment vehContainerLeftLane) {
        return calcAccModel(vehContainer, vehContainerLeftLane, 1, 1, 1);
    }

    private double calcAccModel(final LaneSegment vehContainer, final LaneSegment vehContainerLeftLane,
            double alphaTLocal, double alphaV0Local, double alphaALocal) {
        if (longitudinalModel == null) {
            return 0.0;
        }

        final double acc;

        if (lcModel != null && lcModel.isInitialized() && lcModel.withEuropeanRules()) {
            acc = longitudinalModel.calcAccEur(lcModel.vCritEurRules(), this, vehContainer, vehContainerLeftLane,
                    alphaTLocal, alphaV0Local, alphaALocal);
        } else {
            acc = longitudinalModel.calcAcc(this, vehContainer, alphaTLocal, alphaV0Local, alphaALocal);
        }

        return acc;
    }

    /**
     * Update.
     *  
     * @param dt
     *            delta-t, simulation time interval, seconds
     */
    public void updatePositionAndSpeed(double dt) {

        // logger.debug("dt = {}", dt);
        // first increment postion,
        // then increment s with *new* v (second order: -0.5 a dt^2)

        positionOld = midPosition;

        if (longitudinalModel != null && longitudinalModel.isCA()) {
            speed = (int) (speed + dt * acc + 0.5);
            final int advance = (int) (midPosition + dt * speed + 0.5); 
            midPosition = advance;
            totalTraveledDistance += advance;

        } else {
            // continuous microscopic models and iterated maps
            if (speed < 0) {
                speed = 0;
            }
            final double advance = (acc * dt >= -speed) ? speed * dt + 0.5 * acc * dt * dt : -0.5 * speed * speed / acc;
            midPosition += advance;
            totalTraveledDistance += advance;
            speed += dt * acc;
            if (speed < 0) {
                speed = 0;
                acc = 0;
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getLane()
     */

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#hasReactionTime()
     */

    public boolean hasReactionTime() {
        return (reactionTime + MovsimConstants.SMALL_VALUE > 0);
    }

    /**
     * Update..
     * 
     * @param simulationTime
     *            current simulation time, seconds
     * @param trafficLight
     */
    public void updateTrafficLight(double simulationTime, TrafficLight trafficLight) {
        trafficLightApproaching.update(this, simulationTime, trafficLight, longitudinalModel);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#removeObservers()
     */

    public void removeObservers() {
        longitudinalModel.removeObserver();
    }

    public LaneChangingModel getLaneChangingModel() {
        return lcModel;
    }

    public void setLaneChangingModel(LaneChangingModel lcModel) {
        this.lcModel = lcModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getAccelerationModel()
     */

    public LongitudinalModelBase getLongitudinalModel() {
        return longitudinalModel;
    }

    public void setLongitudinalModel(LongitudinalModelBase longitudinalModel) {
        this.longitudinalModel = longitudinalModel;
    }

    // ---------------------------------------------------------------------------------
    // lane-changing related methods
    // ---------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#considerLaneChanging(double, java.util.List)
     */

    public boolean considerLaneChanging(double dt, RoadSegment roadSegment) {

        // no lane changing when not configured in xml.
        if (lcModel == null || !lcModel.isInitialized()) {
            return false;
        }

        // no lane-changing decision necessary for one-lane road
        if (roadSegment.laneCount() < 2) {
            return false;
        }

        if (inProcessOfLaneChanging()) {
            updateLaneChangingDelay(dt);
            return false;
        }

        // if not in lane-changing process do determine if new lane is more
        // attractive and lane change is possible
        final int laneChangingDirection = lcModel.determineLaneChangingDirection(roadSegment);

        // initiates a lane change: set targetLane to new value
        // the lane will be assigned by the vehicle container !!
        if (laneChangingDirection != MovsimConstants.NO_CHANGE) {
            setTargetLane(lane + laneChangingDirection);
            resetDelay();
            updateLaneChangingDelay(dt);
            logger.debug("do lane change to={} into target lane={}", laneChangingDirection, targetLane);
            return true;
        }

        return false;
    }

    public void initLaneChangeFromRamp(int oldLane) {
        laneOld = oldLane; // MovsimConstants.MOST_RIGHT_LANE + MovsimConstants.TO_RIGHT; //
                           // virtual lane index from onramp
        resetDelay();
        final double delayInit = 0.2; // needs only to be > 0;
        updateLaneChangingDelay(delayInit);
        logger.debug("do lane change from ramp: virtual old lane (origin)={}, contLane={}", lane, getContinousLane());
        if (oldLane == MovsimConstants.TO_LEFT) {
            // System.out.printf(".......... do lane change from ramp: virtual old lane (origin)=%d, contLane=%.4f", lane,
            // getContinousLane());
            logger.debug("do lane change from ramp: virtual old lane (origin)={}, contLane={}", lane,
                    getContinousLane());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getTargetLane()
     */

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
        this.targetLane = targetLane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#inProcessOfLaneChanging()
     */
    public boolean inProcessOfLaneChanging() {
        return (tLaneChangingDelay > 0 && tLaneChangingDelay < FINITE_LANE_CHANGE_TIME_S);
    }

    /**
     * Reset delay.
     */
    private void resetDelay() {
        tLaneChangingDelay = 0;
    }

    /**
     * Update lane changing delay.
     * 
     * @param dt
     *            the dt
     */
    private void updateLaneChangingDelay(double dt) {
        tLaneChangingDelay += dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Moveable#getContinousLane()
     */

    public double getContinousLane() {
        if (inProcessOfLaneChanging()) {
            final double fractionTimeLaneChange = Math.min(1, tLaneChangingDelay / FINITE_LANE_CHANGE_TIME_S);
            return fractionTimeLaneChange * lane + (1 - fractionTimeLaneChange) * laneOld;
        }
        return getLane();
    }

    // ---------------------------------------------------------------------------------
    // braking lights for neat viewers
    // ---------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Moveable#isBrakeLightOn()
     */

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
     * Although the change of lanes is immediate, <code>lane</code>, <code>prevLane</code> and <code>timeAtWhichLastChangedLanes</code> are
     * used to interpolate this vehicle's lateral position and so give the appearance of a smooth lane change.
     * </p>
     * 
     * @param newLane
     * @param newPos
     * @param exitPos
     */
    public void moveToNewRoadSegment(int newLane, double newRearPos, double exitPos) {
        // distanceTravelledToStartOfRoadSegment += rearPosition - newRearPos;
        final int delta = laneOld - lane;
        lane = newLane;
        laneOld = lane + delta;
        setRearPosition(newRearPos);
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
    
    
    public final double totalTraveledDistance(){
        return totalTraveledDistance;
    }

}
