/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel.LaneChangeDecision;
import org.movsim.simulator.vehicles.longitudinalmodel.Memory;
import org.movsim.simulator.vehicles.longitudinalmodel.Noise;
import org.movsim.simulator.vehicles.longitudinalmodel.TrafficLightApproaching;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase;
import org.movsim.utilities.Colors;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

/**
 * <p>
 * Model for a vehicle in a traffic simulation.
 * </p>
 * <p>
 * Each vehicle has its own unique immutable id which is assigned when the vehicle is created.
 * </p>
 * <p>
 * A vehicle has a size, given by its length and width.
 * </p>
 * <p>
 * A vehicle has the kinematic attributes of position, velocity and acceleration. A vehicle's position is given by the position of the front
 * of the vehicle on the road segment and also by the vehicle's lane.
 * </p>
 * <p>
 * A vehicle possesses two intelligence modules:
 * <ul>
 * <li>a LongitudinalModel which determines its acceleration in the direction of travel.</li>
 * <li>a LaneChangeModel which determines when it changes lanes.</li>
 * </ul>
 * <p>
 * Vehicles are quite frequently created and destroyed, so by design they have few allocated properties.
 * </p>
 */
public class Vehicle {

    private static final Logger LOG = LoggerFactory.getLogger(Vehicle.class);

    protected static final int INITIAL_ID = 1;

    protected static final int INITIAL_TEMPLATE_ID = -1;

    private static long nextId = INITIAL_ID;

    private static long nextTemplateId = INITIAL_TEMPLATE_ID;

    /**
     * 'Not Set' vehicle id value, guaranteed not to be used by any vehicles.
     */
    // private static final int ID_NOT_SET = -1;

    private static final int VEHICLE_NUMBER_NOT_SET = -1;

    public static final int LANE_NOT_SET = -1;

    /**
     * 'Not Set' road segment id value, guaranteed not to be used by any
     * vehicles.
     */
    public static final int ROAD_SEGMENT_ID_NOT_SET = -1;

    /**
     * in m/s^2
     */
    private final static double THRESHOLD_BRAKELIGHT_ON = 0.2;

    /**
     * in m/s^2
     */
    private final static double THRESHOLD_BRAKELIGHT_OFF = 0.1;

    /**
     * needs to be  &gt; 0
     */
    private final static double FINITE_LANE_CHANGE_TIME_S = 7;

    private final VehicleDimensions dimensions;

    private final String label;

    /**
     * The front position of the vehicle. The reference position.
     */
    private double frontPosition;

    /**
     * The old front position of the vehicle.
     */
    private double frontPositionOld;

    /**
     * The total distance traveled
     */
    private double totalTravelDistance;

    private double totalTravelTime = 0;

    private double speed;

    /**
     * The acceleration as calculated by the longitudinal driver model.
     */
    private double accModel;

    /**
     * The actual acceleration. This is the acceleration calculated by the LDM moderated by other factors, such as traffic
     * lights
     */
    private double acc;

    private double accOld;

    /**
     * The max deceleration .
     */
    private final double maxDeceleration;

    private double externalAcceleration = Double.NaN;

    /**
     * The unique id of the vehicle.
     */
    final long id;

    /**
     * constant random number between 0 and 1 used for random output selections
     */
    final double randomFix;

    /**
     * The vehicle number.
     */
    private int vehNumber = VEHICLE_NUMBER_NOT_SET;

    private int lane = LANE_NOT_SET;

    private int laneOld;

    /**
     * variable for remembering new target lane when assigning to new
     * laneSegment
     */
    private int targetLane;

    /**
     * finite lane-changing duration
     */
    private double tLaneChangeDelay;

    private double speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;

    private double slope;

    private int color;

    /**
     * color object cache
     */
    private Object colorObject;

    private LongitudinalModelBase longitudinalModel;

    /**
     * can be null
     */
    private LaneChangeModel laneChangeModel;

    /**
     * Memory model. Can be null
     */
    private Memory memory = null;

    /**
     * Acceleration noise model. Can be null
     */
    private Noise noise = null;

    private final TrafficLightApproaching trafficLightApproaching;

    private final InhomogeneityAdaption inhomogeneity;

    private final EnergyModel energyModel = new EnergyModel(this);

    /**
     * can be null
     */
    private Route route;

    private int routeIndex;

    private boolean brakeLightOn;

    private PhysicalQuantities physQuantities;

    private final VehicleUserData userData;

    private final RoutingDecisions routingDecisions = new RoutingDecisions(this);

    // Exit Handling
    private int roadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

    private RoadSegment roadSegment;

    private int exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

    private int originRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;

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
        dimensions = new VehicleDimensions(vehInput.getLength(), vehInput.getWidth());
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

        // needs to be > 0 to avoid lane-changing over 2 lanes in one update
        // step
        assert FINITE_LANE_CHANGE_TIME_S > 0;

        this.color = Colors.randomColor();

        trafficLightApproaching = new TrafficLightApproaching();
        inhomogeneity = new InhomogeneityAdaption();
        userData = new VehicleUserData();
    }

    /**
     * Constructor.
     */
    public Vehicle(double rearPosition, double speed, int lane, double length, double width) {
        assert rearPosition >= 0.0;
        assert speed >= 0.0;
        id = nextId++;
        randomFix = MyRandom.nextDouble();
        dimensions = new VehicleDimensions(length, width);
        setRearPosition(rearPosition);
        this.speed = speed;
        this.lane = lane;
        this.laneOld = lane;
        this.color = 0;
        maxDeceleration = 10.0;
        laneChangeModel = null;
        longitudinalModel = null;
        label = "";
        physQuantities = new PhysicalQuantities(this);
        slope = 0;
        route = null;
        trafficLightApproaching = new TrafficLightApproaching();
        inhomogeneity = new InhomogeneityAdaption();
        userData = new VehicleUserData();
    }

    /**
     * Copy constructor.
     *
     * @param source
     */
    public Vehicle(Vehicle source) {
        id = source.id;
        randomFix = source.randomFix;
        type = source.type;
        frontPosition = source.frontPosition;
        speed = source.speed;
        lane = source.lane;
        laneOld = source.laneOld;
        dimensions = new VehicleDimensions(source.getDimensions());
        color = source.color;
        trafficLightApproaching = source.trafficLightApproaching;
        inhomogeneity = source.inhomogeneity;
        maxDeceleration = source.maxDeceleration;
        laneChangeModel = source.laneChangeModel;
        longitudinalModel = source.longitudinalModel;
        label = source.label;
        slope = source.slope;
        route = source.route;
        routeIndex = source.routeIndex;
        userData = source.userData;
        if (source.routingDecisions.hasServiceProvider()) {
            routingDecisions.setServiceProvider(source.routingDecisions().getServiceProvider());
            routingDecisions.setUncertainty(source.routingDecisions().getUncertainty());
            routingDecisions.setReroutingThreshold(source.routingDecisions.getReroutingThreshold());
        }
    }

    private void initialize() {
        frontPositionOld = 0;
        frontPosition = 0;
        speed = 0;
        acc = 0;
        brakeLightOn = false;
        slope = 0;
        unsetSpeedlimit();
        routeIndex = 0;
        roadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
        exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
        originRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Sets this vehicle's color.
     *
     * @param color RGB integer color value
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
     * Sets this vehicle's color object cache value. Primarily of use by AWT which rather inefficiently uses objects rather than
     * integers to represent color values. Note that an object is cached so Vehicle.java has no dependency on AWT.
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
        return dimensions.getLength();
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
     * @param frontPosition new front position
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
        return frontPosition - (0.5 * getLength());
    }

    /**
     * Sets the rear position of this vehicle.
     *
     * @param rearPosition new rear position
     */
    public final void setRearPosition(double rearPosition) {
        this.frontPosition = rearPosition + getLength();
    }

    /**
     * Returns the position of the rear of this vehicle.
     *
     * @return position of the rear of this vehicle
     */
    public final double getRearPosition() {
        return frontPosition - getLength();
    }

    public final double getFrontPositionOld() {
        return frontPositionOld;
    }

    public final double getRearPositionOld() {
        return frontPositionOld - getLength();
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
     * @param speed in m/s the new speed in m/s
     */
    public final void setSpeed(double speed) {
        this.speed = speed;
    }

    public final double getEffectiveSpeedlimit() {
        LOG.debug("speedlimit={}, maxAllowedSpeedOnRoad={}", speedlimit, getMaxAllowedSpeedOnRoad());
        return Math.min(speedlimit, getMaxAllowedSpeedOnRoad());
    }

    public final double getSpeedlimit() {
        return speedlimit;
    }

    public final double getMaxAllowedSpeedOnRoad() {
        if (roadSegment == null) {
            return MovsimConstants.MAX_VEHICLE_SPEED;
        }
        return roadSegment.getFreeFlowSpeed();
    }

    /**
     * externally given speedlimit
     *
     * @param speedlimit
     */
    public final void setSpeedlimit(double speedlimit) {
        this.speedlimit = speedlimit;
    }

    public final void unsetSpeedlimit() {
        this.speedlimit = MovsimConstants.MAX_VEHICLE_SPEED;
    }

    // TODO acceleration model for slopes, Chapter 20 of "TrafficFlowDynamics"
    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getSlope() {
        return slope;
    }

    /**
     * Returns the actual acceleration. This is the acceleration calculated by the LDM moderated by other factors, such as
     * traffic lights
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
     * returns the net distance (from front bumper to rear bumper) to the front vehicle, returns infinity gap if front vehicle
     * is null.
     *
     * @param frontVehicle
     */
    public double getNetDistance(Vehicle frontVehicle) {
        if (frontVehicle == null) {
            return MovsimConstants.GAP_INFINITY;
        }
        return frontVehicle.getRearPosition() - getFrontPosition();
    }

    /**
     * returns the brut distance (net distance plus vehicle length of front vehicle) to the front vehicle, returns infinity gap
     * if front vehicle is null.
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
     * returns the net distance (from rear bumper to front bumper) to the rear vehicle, returns infinity gap if front vehicle is
     * null.
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
                                   LaneSegment leftLaneSegment) {

        accOld = acc;
        // acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            Vehicle frontVehicle = laneSegment.frontVehicle(this);
            if (getNetDistance(frontVehicle) < MovsimConstants.CRITICAL_GAP) {
                accError = Math.min(accError, 0.);
            }
        }

        double alphaTLocal = inhomogeneity.alphaT();
        double alphaV0Local = inhomogeneity.alphaV0();
        double alphaALocal = 1;

        // TODO check concept here: combination with alphaV0 (consideration of
        // reference v0 instead of dynamic v0 which
        // depends on speedlimits)
        if (memory != null) {
            double v0 = longitudinalModel.getDesiredSpeed();
            memory.update(dt, speed, v0);
            alphaTLocal *= memory.alphaT();
            alphaV0Local *= memory.alphaV0();
            alphaALocal *= memory.alphaA();
        }

        acc = accModel = calcAccModel(laneSegment, leftLaneSegment, alphaTLocal, alphaV0Local, alphaALocal);

        if (lane() != Lanes.OVERTAKING) {
            // moderate acceleration by traffic lights or for preparing
            // mandatory lane changes to exit sliproads
            acc = moderateAcceleration(accModel, roadSegment);
        }

        acc = Math.max(acc + accError, -maxDeceleration); // limited to maximum
        // deceleration
    }

    /**
     * Moderates this vehicle's acceleration according to factors other than the LDM. For example, the presence of traffic
     * lights, motorway exits or tactical considerations (say, the desire to make a lane change). Due to basic safety reasons it
     * is crucial to use the minimum acceleration.
     *
     * @param acc         acceleration as calculated by LDM
     * @param roadSegment
     * @return moderated acceleration
     */
    private final double moderateAcceleration(double acc, RoadSegment roadSegment) {
        double moderatedAcc = acc;
        if (type == Vehicle.Type.OBSTACLE) {
            return moderatedAcc; // quick hack, better structure needed here
        }

        double accTrafficLight = trafficLightApproaching.accelerationConsideringTrafficLight(this, roadSegment);
        if (!Double.isNaN(accTrafficLight)) {
            moderatedAcc = Math.min(moderatedAcc, accTrafficLight);
        }

        double accExit = accelerationConsideringExit(roadSegment);
        if (!Double.isNaN(accExit)) {
            moderatedAcc = Math.min(moderatedAcc, accExit);
        }

        if (hasExternalAcceleration()) {
            moderatedAcc = Math.min(moderatedAcc, externalAcceleration);
        }
        return moderatedAcc;
    }

    /**
     * Returns this vehicle's acceleration considering the exit.
     *
     * @param roadSegment
     * @return acceleration considering exit
     */
    private double accelerationConsideringExit(RoadSegment roadSegment) {
        double accToVehicleInExitLane = Double.NaN;
        // valid exit road segment id indicates that exit has to be reached
        // ASSUMPTION: routing horizon looks one roadSegment ahead
        if (exitRoadSegmentId == EXIT_POSITION_NOT_SET) {
            return accToVehicleInExitLane;
        }
        LOG.debug("exitRoadSectionId={}, current roadSegment={}", exitRoadSegmentId, roadSegment.id());
        // unfortunately we have to distinguish the following two cases:
        // (1) vehicle is on roadsegment with exit lane
        if (roadSegment.id() == exitRoadSegmentId) {
            LaneSegment firstExitLaneSegment = roadSegment.laneSegment(roadSegment.trafficLaneMax() + Lanes.TO_RIGHT);
            assert firstExitLaneSegment != null && firstExitLaneSegment.type() == Lanes.Type.EXIT :
                    "no exitLaneSegment=" + firstExitLaneSegment;
            Vehicle frontVehicle = firstExitLaneSegment.frontVehicle(this);
            accToVehicleInExitLane = longitudinalModel.calcAcc(this, frontVehicle);
            accToVehicleInExitLane = Math.max(accToVehicleInExitLane, -maxDeceleration);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "considering exit=%d: veh=%d, distance to front veh in exit lane=%.2f, speed=%.2f, calcAcc=%.2f, accLimit=%.2f",
                        exitRoadSegmentId, getId(), getNetDistance(frontVehicle), getSpeed(),
                        longitudinalModel.calcAcc(this, frontVehicle), accToVehicleInExitLane));
            }
            return accToVehicleInExitLane;
        }

        // (2) exit lane is one roadSegment ahead but cannot be reached via sink connection
        RoadSegment sinkRoadSegmentWithExit = roadSegment.sinkRoadSegmentPerId(exitRoadSegmentId);
        if (sinkRoadSegmentWithExit != null) {
            LaneSegment exitLaneSegment = sinkRoadSegmentWithExit
                    .laneSegment(sinkRoadSegmentWithExit.trafficLaneMax() + Lanes.TO_RIGHT);
            assert exitLaneSegment != null && exitLaneSegment.type() == Lanes.Type.EXIT :
                    "no exitLaneSegment=" + exitLaneSegment;
            Vehicle frontVehicle = exitLaneSegment.rearVehicle();
            if (frontVehicle != null) {
                double s = roadSegment.roadLength() - this.getFrontPosition() + frontVehicle.getRearPosition();
                double dv = getSpeed() - frontVehicle.getSpeed();
                accToVehicleInExitLane = longitudinalModel.calcAccSimple(s, getSpeed(), dv);
                accToVehicleInExitLane = Math.max(accToVehicleInExitLane, -maxDeceleration);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "considering exit=%d: veh=%d, distance to front veh in exit lane=%.2f, speed=%.2f, accLimit=%.2f",
                            exitRoadSegmentId, getId(), getNetDistance(frontVehicle), getSpeed(),
                            accToVehicleInExitLane));
                }
                return accToVehicleInExitLane;
            }
        }
        return Double.NaN;
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
            acc = longitudinalModel
                    .calcAccEur(laneChangeModel.vCritEurRules(), this, laneSegment, leftLaneSegment, alphaTLocal,
                            alphaV0Local, alphaALocal);
        } else {
            acc = longitudinalModel.calcAcc(this, laneSegment, alphaTLocal, alphaV0Local, alphaALocal);
        }

        return acc;
    }

    /**
     * Update position and speed. Case distinction between cellular automata, Newell and continuos models/iterated maps
     *
     * @param dt delta-t, simulation time interval, seconds
     */
    public void updatePositionAndSpeed(double dt) {
        totalTravelTime += dt;
        frontPositionOld = frontPosition;
        if (longitudinalModel != null && longitudinalModel.isCA()) {
            speed = (int) (speed + dt * acc + 0.5);
            final int advance = (int) (frontPosition + dt * speed + 0.5);
            totalTravelDistance += (advance - frontPosition);
            frontPosition = advance;
        } else if (longitudinalModel != null && (longitudinalModel.isIteratedMap())) {
            // Newell, Gipps and Krauss model: modified first-order positional update (simple Euler scheme)
            // See chapter 10.8 of "Traffic Flow Dynamics" book
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
        } else if (type == Type.EXTERNAL_CONTROL) {
            if (speed < 0) {
                LOG.error("external speed set to negative value={}, reset to 0", speed);
                speed = 0;
            }
            final double advance = speed * dt;
            frontPosition += advance;
            totalTravelDistance += advance;
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
        energyModel.incrementConsumption(speed, acc, dt);
    }

    public final int lane() {
        return lane;
    }

    public final void setLane(int lane) {
        assert lane >= Lanes.MOST_INNER_LANE || lane == Lanes.OVERTAKING;
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

    public boolean considerOvertakingViaPeer(double dt, RoadSegment roadSegment) {
        LaneChangeDecision lcDecision = LaneChangeDecision.NONE;
        if (!roadSegment.hasPeer() || roadSegment.laneCount() > 1 || lane() != Lanes.MOST_INNER_LANE
                || laneChangeModel == null || !laneChangeModel.isInitialized() || inProcessOfLaneChange()) {
            return false;
        }
        lcDecision = laneChangeModel.makeDecisionForOvertaking(roadSegment);
        if (lcDecision == LaneChangeDecision.OVERTAKE_VIA_PEER) {
            setTargetLane(Lanes.OVERTAKING);
            resetDelay(dt);
            LOG.debug("do overtaking lane change to={} into target lane={}", lcDecision, targetLane);
        }
        return lcDecision == LaneChangeDecision.OVERTAKE_VIA_PEER;
    }

    public boolean considerFinishOvertaking(double dt, LaneSegment laneSegment) {
        assert lane() == Lanes.OVERTAKING;
        assert !inProcessOfLaneChange();
        if (laneChangeModel == null || !laneChangeModel.isInitialized()) {
            return false;
        }
        LaneChangeDecision lcDecision = laneChangeModel.finishOvertakingViaPeer(laneSegment);
        if (lcDecision == LaneChangeDecision.MANDATORY_TO_RIGHT) {
            setTargetLane(Lanes.MOST_INNER_LANE);
            resetDelay(dt);
            LOG.debug("finish overtaking, turn from lane={} into target lane={}", laneOld, targetLane);
            return true;
        }
        return false;
    }

    public boolean considerLaneChange(double dt, RoadSegment roadSegment) {

        if (roadSegment.laneCount() <= 1) {
            // no lane-changing decision necessary for one-lane road. already
            // checked before
            return false;
        }

        // no lane changing when not configured in xml.
        if (laneChangeModel == null || !laneChangeModel.isInitialized()) {
            return false;
        }
        assert !inProcessOfLaneChange();

        // if not in lane-changing process do determine if new lane is more
        // attractive and lane change is possible
        LaneChangeDecision lcDecision = laneChangeModel.makeDecision(roadSegment);
        final int laneChangeDirection = lcDecision.getDirection();

        // initiates a lane change: set targetLane to new value the lane will be
        // assigned by the vehicle container !!
        if (laneChangeDirection != Lanes.NO_CHANGE) {
            setTargetLane(lane + laneChangeDirection);
            resetDelay(dt);
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
     * @param targetLane the new target lane
     */
    private void setTargetLane(int targetLane) {
        assert targetLane >= Lanes.MOST_INNER_LANE || targetLane == Lanes.OVERTAKING;
        assert targetLane != lane;
        this.targetLane = targetLane;
    }

    public boolean inProcessOfLaneChange() {
        return (tLaneChangeDelay > 0 && tLaneChangeDelay < FINITE_LANE_CHANGE_TIME_S);
    }

    private void resetDelay(double dt) {
        tLaneChangeDelay = 0;
        updateLaneChangeDelay(dt); // TODO hack that updateLaneChangeDelay must
        // be called for inProcessOfLaneChange being
        // true
    }

    /**
     * Update lane changing delay.
     *
     * @param dt the dt
     */
    public void updateLaneChangeDelay(double dt) {
        tLaneChangeDelay += dt;
    }

    public double getContinuousLane() {
        if (inProcessOfLaneChange()) {
            final double fractionTimeLaneChange = Math.min(1, tLaneChangeDelay / FINITE_LANE_CHANGE_TIME_S);
            return fractionTimeLaneChange * lane + (1 - fractionTimeLaneChange) * laneOld;
        }
        return lane();
    }

    // ---------------------------------------------------------------------------------
    // braking lights for viewer
    // ---------------------------------------------------------------------------------
    public boolean isBrakeLightOn() {
        updateBrakeLightStatus();
        return brakeLightOn;
    }

    /**
     * Update brake light status.
     */
    private void updateBrakeLightStatus() {
        if (brakeLightOn) {
            if (acc > -THRESHOLD_BRAKELIGHT_OFF || speed <= 0.001) {
                brakeLightOn = false;
            }
        } else if (accOld > -THRESHOLD_BRAKELIGHT_ON && acc < -THRESHOLD_BRAKELIGHT_ON && speed > 0.001) {
            brakeLightOn = true;
        }
    }

    // ---------------------------------------------------------------------------------
    // converter for scaled quantities in cellular automata
    // ---------------------------------------------------------------------------------

    public PhysicalQuantities physicalQuantities() {
        return physQuantities;
    }

    // Added as part of xodr merge
    /**
     * 'Not Set' road exit position value, guaranteed not to be used by any
     * vehicles.
     */
    public static final double EXIT_POSITION_NOT_SET = -1.0;

    /**
     * Vehicle type.
     */
    public enum Type {
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
         * Externally controlled vehicle.
         */
        EXTERNAL_CONTROL
    }

    private Type type = Type.VEHICLE;

    /**
     * Returns this vehicle's type.
     *
     * @return vehicle's type
     */
    public final Vehicle.Type type() {
        return type;
    }

    /**
     * Sets this vehicle's type.
     *
     * @param type
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
        this.lane = newLane;
        this.laneOld = newLane;
        laneOld = Math.max(Lanes.MOST_INNER_LANE, Math.min(laneOld, newRoadSegment.laneCount()));
        double offsetPosition = getRearPosition() - newRearPosition;
        LOG.debug("move to new segment: rearPosOld={} --> new rearPosOld={}", frontPositionOld - getLength(),
                frontPositionOld - offsetPosition - getLength());
        this.frontPositionOld -= offsetPosition;

        setRearPosition(newRearPosition);
        setRoadSegment(newRoadSegment);
    }

    /**
     * Sets the road segment properties for this vehicle. Invoked after a vehicle has moved onto a new road segment.
     */
    public final void setRoadSegment(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        if (originRoadSegmentId == ROAD_SEGMENT_ID_NOT_SET) {
            originRoadSegmentId = roadSegment.id();
        }
        this.roadSegmentId = roadSegment.id();

        updateRoute();
    }

    private void updateRoute() {
        if (roadSegmentId != exitRoadSegmentId) {
            exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET; // reset
        }

        if (route != null && routeIndex < route.size()) {
            RoadSegment routeRoadSegment = route.get(routeIndex);
            ++routeIndex;
            if (routeRoadSegment.id() != roadSegmentId) {
                LOG.warn("vehicle={} has left its route={}.", this, route.getName());
                routeIndex = Integer.MAX_VALUE; // skip further warning logs
                exitRoadSegmentId = ROAD_SEGMENT_ID_NOT_SET;
                return;
            }
            // vehicle is still on track following its route
            if (routeIndex < route.size()) {
                // there is another roadSegment on the route, so check if the
                // next roadSegment is joined to an exit lane
                RoadSegment nextRouteRoadSegment = route.get(routeIndex);
                if (routeRoadSegment.exitsOnto(nextRouteRoadSegment.id())) {
                    // this vehicle needs to exit on this roadSegment
                    exitRoadSegmentId = roadSegmentId;
                } else if (routeIndex + 1 < route.size()) {
                    // there is another roadSegment on the route
                    // so check if the next roadSegment is joined to an exit
                    // lane
                    // of the current roadSegment
                    final RoadSegment nextNextRouteRoadSegment = route.get(routeIndex + 1);
                    if (nextRouteRoadSegment.exitsOnto(nextNextRouteRoadSegment.id())) {
                        // this vehicle needs to exit on this roadSegment
                        exitRoadSegmentId = nextRouteRoadSegment.id(); // roadSegmentId;
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
     * @return
     */
    public final double totalTravelTime() {
        return totalTravelTime;
    }

    public double getMaxDeceleration() {
        return maxDeceleration;
    }

    public double getDistanceToRoadSegmentEnd() {
        if (roadSegment.roadLength() <= 0) {
            return -1;
        }
        return roadSegment.roadLength() - getFrontPosition();
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.###");
        return "Vehicle [id=" + id + ", label=" + label + ", length=" + df.format(getLength()) + ", frontPosition=" + df
                .format(frontPosition) + ", frontPositionOld=" + df.format(frontPositionOld) + ", speed=" + df
                .format(speed) + ", accModel=" + df.format(accModel) + ", acc=" + df.format(acc) + ", accOld=" + df
                .format(accOld) + ", vehNumber=" + vehNumber + ", lane=" + lane + ", brakeLightOn=" + brakeLightOn
                + "]";
    }

    /**
     * returns a constant random number between 0 and 1
     */
    public double getRandomFix() {
        return randomFix;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setNoise(Noise noise) {
        this.noise = noise;
    }

    public void setRoute(Route newRoute) {
        LOG.debug("set route={} to vehicle {}", getRouteName(), id);
        if (this.route != null && newRoute != null && !newRoute.getName().equals(route.getName())) {
            LOG.info("vehicle changed route from={} to new route={}", this.route, newRoute);
        }
        this.route = newRoute;
    }

    public String getRouteName() {
        return route != null ? route.getName() : "noRoute";
    }

    public InhomogeneityAdaption inhomogeneityAdaptation() {
        return inhomogeneity;
    }

    public VehicleUserData getUserData() {
        return userData;
    }

    public double getExternalAcceleration() {
        return externalAcceleration;
    }

    public boolean hasExternalAcceleration() {
        return !Double.isNaN(externalAcceleration);
    }

    public void setExternalAcceleration(double externalAcceleration) {
        this.externalAcceleration = externalAcceleration;
    }

    public void unsetExternalAcceleration() {
        this.externalAcceleration = Double.NaN;
    }

    public EnergyModel getEnergyModel() {
        return energyModel;
    }

    public VehicleDimensions getDimensions() {
        return dimensions;
    }

    public TrafficLightApproaching getTrafficLightApproaching() {
        return trafficLightApproaching;
    }

    public RoutingDecisions routingDecisions() {
        return routingDecisions;
    }

}
