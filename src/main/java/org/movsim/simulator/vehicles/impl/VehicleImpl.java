/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.impl;

import org.movsim.input.model.VehicleInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Noise;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGUI;
import org.movsim.simulator.vehicles.impl.CyclicBufferImpl;
import org.movsim.simulator.vehicles.impl.NoiseImpl;
import org.movsim.simulator.vehicles.longmodel.Memory;
import org.movsim.simulator.vehicles.longmodel.TrafficLightApproaching;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.impl.MemoryImpl;
import org.movsim.simulator.vehicles.longmodel.impl.TrafficLightApproachingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class VehicleImpl.
 */
public class VehicleImpl implements Vehicle, VehicleGUI {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleImpl.class);

    /** The label. */
    private final String label;
    
    /** The length. */
    private final double length;
    
    /** The position. */
    private double position;
    
    /** The old position. */
    private double oldPosition;
    
    /** The speed. */
    private double speed;
    
    /** The acc model. */
    private double accModel;
    
    /** The acc. */
    private double acc;

    /** The reaction time. */
    private final double reactionTime;

    /** The max decel. */
    private final double maxDecel;

    /** The distance to trafficlight. */
    private double distanceToTrafficlight;

    /** The id. */
    private final int id;

    /** The veh number. */
    private int vehNumber;

    /** The lane. */
    private double lane;
    
    /** The target lane. */
    private int targetLane;

   
    /** The speedlimit. */
    private double speedlimit; // state variable

    /** The long model. */
    private final AccelerationModel longModel;

    /** The memory. */
    private Memory memory = null;

    /** The noise. */
    private Noise noise = null;
    
    /** The traffic light approaching. */
    private final TrafficLightApproaching trafficLightApproaching;
    

    /** The cyclic buffer. */
    private final CyclicBufferImpl cyclicBuffer; // TODO

    /**
     * Instantiates a new vehicle impl.
     *
     * @param label the label
     * @param id the id
     * @param longModel the long model
     * @param vehInput the veh input
     * @param cyclicBuffer the cyclic buffer
     */
    public VehicleImpl(String label, int id, AccelerationModel longModel, VehicleInput vehInput, CyclicBufferImpl cyclicBuffer) {
    	this.label = label;
        this.id = id;

        length = vehInput.getLength();
        reactionTime = vehInput.getReactionTime();
        maxDecel = vehInput.getMaxDeceleration();

        this.longModel = longModel;
        this.cyclicBuffer = cyclicBuffer;

        oldPosition = 0;
        position = 0;
        speed = 0;
        acc = 0;

        speedlimit = Constants.MAX_VEHICLE_SPEED;
        
        
        // TODO set isFromOnramp

        // effekt wirkungslos, wenn Modell nicht ueber entsprechenden Modelparameter verfuegt.
        if (vehInput.isWithMemory()) {
            memory = new MemoryImpl(vehInput.getMemoryInputData());
        }

        if (vehInput.isWithNoise()) {
            noise = new NoiseImpl(vehInput.getNoiseInputData());
        }

        trafficLightApproaching = new TrafficLightApproachingImpl();
		
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#init(double, double, int)
     */
    @Override
    public void init(double pos, double v, int lane) {
        this.position = pos;
        this.oldPosition = pos;
        this.speed = v;
        this.lane = lane;
        this.targetLane = lane;
    }

    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.Vehicle#getLabel()
     */
    public String getLabel() {
        return label;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#length()
     */
    @Override
    public double length() {
        return length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#position()
     */
    @Override
    public double position() {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#posFrontBumper()
     */
    @Override
    public double posFrontBumper() {
        return position + 0.5 * length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#posReadBumper()
     */
    @Override
    public double posReadBumper() {
        return position - 0.5 * length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#oldPosition()
     */
    @Override
    public double oldPosition() {
        return oldPosition;
    }

    /**
     * Sets the position.
     * 
     * @param position
     *            the new position
     */
    public void setPosition(double position) {
        this.position = position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#speed()
     */
    @Override
    public double speed() {
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
    @Override
    public double speedlimit() {
        return speedlimit;
    }

    // externally given speedlimit
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#setSpeedlimit(double)
     */
    @Override
    public void setSpeedlimit(double speedlimit) {
        this.speedlimit = speedlimit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#acc()
     */
    @Override
    public double acc() {
        return acc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#accModel()
     */
    @Override
    public double accModel() {
        return accModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#distanceToTrafficlight()
     */
    @Override
    public double distanceToTrafficlight() {
        return distanceToTrafficlight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#id()
     */
    @Override
    public int id() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#isFromOnramp()
     */
    @Override
    public boolean isFromOnramp() {
        return (vehNumber < 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getVehNumber()
     */
    @Override
    public int getVehNumber() {
        return vehNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#setVehNumber(int)
     */
    @Override
    public void setVehNumber(int vehNumber) {
        this.vehNumber = vehNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.Vehicle#netDistance(org.movsim.simulator
     * .vehicles.Vehicle)
     */
    @Override
    public double netDistance(Vehicle vehFront) {
        if (vehFront == null)
            return Constants.GAP_INFINITY;
        return (vehFront.position() - position - 0.5 * (length() + vehFront.length()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.Vehicle#relSpeed(org.movsim.simulator.vehicles
     * .Vehicle)
     */
    @Override
    public double relSpeed(Vehicle vehFront) {
        if (vehFront == null)
            return 0;
        return (speed - vehFront.speed());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#calcAcceleration(double,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double)
     */
    @Override
    public void calcAcceleration(double dt, VehicleContainer vehContainer, double alphaT, double alphaV0) {

        // new: acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            final Vehicle vehFront = vehContainer.getLeader(this);
            if (netDistance(vehFront) < 2.0) {
                accError = Math.min(accError, 0.); // !!!
            }
            // logger.debug("accError = {}", accError);
        }

        // TODO extract to super class
        double alphaTLocal = alphaT;
        double alphaV0Local = alphaV0;
        double alphaALocal = 1;

        // TODO check concept here: kombination mit alphaV0: man sollte das
        // Referenz-V0 nehmen
        // und NICHT das dynamische, durch Speedlimits beeinflusste v0
        if (memory != null) {
            final double v0 = longModel.parameterV0();
            memory.update(dt, speed, v0);
            alphaTLocal *= memory.alphaT();
            alphaV0Local *= memory.alphaV0();
            alphaALocal *= memory.alphaA();
        }

        // TODO gekapseltere Aufruf
        accModel = longModel.acc(this, vehContainer, alphaTLocal, alphaV0Local, alphaALocal);

        // consider red or amber/yellow traffic light:
        if (trafficLightApproaching.considerTrafficLight()) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#updatePostionAndSpeed(double)
     */
    @Override
    public void updatePostionAndSpeed(double dt) {

        // logger.debug("dt = {}", dt);
        // increment first s; then increment s with NEW v (2th order: -0.5 a
        // dt^2)

        oldPosition = position;

        if (longModel.isCA()) {
            speed = (int) (speed + dt * acc + 0.5);
            position = (int) (position + dt * speed + 0.5);
        } else {
            // continuous micro models and iterated maps
            if (speed < 0) {
                speed = 0;
            }
            final double advance = (acc * dt >= -speed) ? speed * dt + 0.5 * acc * dt * dt : -0.5 * speed * speed / acc;
            position += advance;
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
    @Override
    public double getLane() {
        return lane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getIntLane()
     */
    @Override
    public int getIntLane() {
        return targetLane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#hasReactionTime()
     */
    @Override
    public boolean hasReactionTime() {
        return (reactionTime + Constants.SMALL_VALUE > 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.Vehicle#getDesiredSpeedParameter()
     */
    @Override
    public double getDesiredSpeedParameter() {
        return longModel.parameterV0();

    }

	/* (non-Javadoc)
	 * @see org.movsim.simulator.vehicles.Vehicle#updateTrafficLight(double, org.movsim.simulator.roadSection.TrafficLight)
	 */
	@Override
	public void updateTrafficLight(double time, TrafficLight trafficLight) {
		trafficLightApproaching.update(this, time, trafficLight, longModel);
		
	}

}
