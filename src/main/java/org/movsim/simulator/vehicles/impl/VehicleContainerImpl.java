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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class VehicleContainerImpl.
 */
public class VehicleContainerImpl implements VehicleContainer {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleContainerImpl.class);

    // array sorted in x-position with decreasing positions
    // lowest index has most downstream position on road

    /** The vehicles. */
    private final List<Vehicle> vehicles;

    /** The veh counter. */
    private int vehCounter;

    
    private final int laneIndex;  // TODO laneInit not necessary anymore ?!
    
    /**
     * Instantiates a new vehicle container impl.
     *
     * @param laneIndex the lane index
     */
    public VehicleContainerImpl(int laneIndex) {
        this.laneIndex = laneIndex;
        vehicles = new ArrayList<Vehicle>();
        vehCounter = 0;
    }

    
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#getLaneIndex()
     */
    @Override
    public int getLaneIndex(){
        return laneIndex;
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleContainer#getVehicles()
     */
    @Override
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleContainer#get(int)
     */
    @Override
    public Vehicle get(int index) {
        return vehicles.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleContainer#size()
     */
    @Override
    public int size() {
        return vehicles.size();
    }

    // vehicle most downstream (largest long position)
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleContainer#getMostDownstream()
     */
    @Override
    public Vehicle getMostDownstream() {
        if (vehicles.isEmpty())
            return null;
        return vehicles.get(0);
    }

    // vehicle most upstream (smallest long position), greatest index
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.VehicleContainer#getMostUpstream()
     */
    @Override
    public Vehicle getMostUpstream() {
        if (vehicles.isEmpty())
            return null;
        return vehicles.get(vehicles.size() - 1);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#add(org.movsim.simulator
     * .vehicles.Vehicle, double, double, int)
     */
    
    @Override
    public void add(final Vehicle veh, double xInit, double vInit) {
        add(veh, xInit, vInit, laneIndex);
    }

    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#add(org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public void add(final Vehicle veh){
        add(veh, veh.getPosition(), veh.getSpeed(), laneIndex);
    }
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#addFromToRamp(org.movsim.simulator.vehicles.Vehicle, double, double, int)
     */
    @Override
    public void addFromToRamp(final Vehicle veh, double xInit, double vInit, int oldLane){
        add(veh, xInit, vInit, laneIndex);
        veh.initLaneChangeFromRamp(oldLane);  // TODO quick hack for continous lane change (special case treatment) 
    }
    

    /**
     * Adds the.
     *
     * @param veh the veh
     * @param xInit the x init
     * @param vInit the v init
     * @param laneInit the lane init
     */
    private void add(final Vehicle veh, double xInit, double vInit, int laneInit) {
        vehCounter++;
        veh.setVehNumber(vehCounter);

        veh.init(xInit, vInit, laneInit); // sets new lane index after lane change

        if (vehicles.isEmpty()) {
            vehicles.add(veh);
        } else if (veh.getPosition() < getMostUpstream().getPosition()) {
            // add after entry with greatest index
            vehicles.add(veh);
        } else if (veh.getPosition() > getMostDownstream().getPosition()) {
            // add before first entry
            vehicles.add(0, veh);
        } else {
            vehicles.add(0, veh);
            sort(); // robust but runtime performance ?
        }
        logger.debug("vehicleContainerImpl: vehicle added: x={}, v={}", veh.getPosition(), veh.getSpeed());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#removeVehiclesDownstream
     * (double)
     */
    @Override
    public void removeVehiclesDownstream(double roadLength) {
        while (!vehicles.isEmpty() && getMostDownstream().getPosition() > roadLength) {
            vehicles.get(0).removeObservers(); // delete references when leaving
                                               // the simulation
            vehicles.remove(0);
            logger.debug(" remove veh ... size = {}", vehicles.size());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#removeVehicleMostDownstream
     * ()
     */
    @Override
    public void removeVehicleMostDownstream() {
        if (!vehicles.isEmpty()) {
            vehicles.remove(0);
        }
    }
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#removeVehicle(org.movsim.simulator.vehicles.Vehicle)
     */
    @Override
    public void removeVehicle(final Vehicle veh){
        if (!vehicles.isEmpty()) {
            vehicles.remove(veh);
        }
    }

  
   
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#getLeader(org.movsim.simulator.vehicles.Moveable)
     */
    @Override
    public Vehicle getLeader(final Moveable veh) {
        final int index = vehicles.indexOf(veh);
        if ( index == 0){
            // no leader downstream
            return null;    
        }
        else if (index == -1 ){
            // return virtual leader for vehicle veh which is not not considered lane
            return findVirtualLeader(veh); 
        }
        return vehicles.get(index - 1);
    }
    
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.VehicleContainer#getFollower(org.movsim.simulator.vehicles.Moveable)
     */
    @Override
    public Vehicle getFollower(final Moveable veh) {
        final int index = vehicles.indexOf(veh);
        if ( index == vehicles.size()-1 ){
            return null;
        }
        else if (index == -1){
            // veh is not contained in this lane
            // return virtual leader for vehicle veh which is not not considered lane
            return findVirtualFollower(veh); 
        }
        return vehicles.get(index + 1);
    }
    

    /**
     * Sort.
     */
    private void sort() {
        // sort order determined by pos2.compareTo(pos1) in descending order
        Collections.sort(vehicles, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle o1, Vehicle o2) {
                final Double pos1 = new Double((o1).getPosition());
                final Double pos2 = new Double((o2).getPosition());
                return pos2.compareTo(pos1);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.MoveableContainer#getMoveables()
     */
    @Override
    public List<Moveable> getMoveables() {
        List<Moveable> moveables = new ArrayList<Moveable>();
        for (final Vehicle veh : vehicles) {
            moveables.add(veh);
        }
        return moveables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.MoveableContainer#getMoveable(int)
     */
    @Override
    public Moveable getMoveable(int index) {
        return vehicles.get(index);
    }

    
    /**
     * Find virtual leader.
     *
     * @param veh the veh
     * @return the vehicle
     */
    private Vehicle findVirtualLeader(final Moveable veh) {
        // TODO efficient implementation with interval intersection
        final double position = veh.getPosition();
        // decrease index for traversing in downstream direction
        // return first vehicle on lane with *higher* position than veh
        for (int i = vehicles.size()-1; i>=0; i--) {
            final Vehicle vehOnLane = vehicles.get(i);
            if(vehOnLane.getPosition() >= position){
                return vehOnLane;
            }
        }
        return null;
    }

    /**
     * Find virtual follower.
     *
     * @param veh the veh
     * @return the vehicle
     */
    private Vehicle findVirtualFollower(final Moveable veh) {
     // TODO efficient implementation 
        final double position = veh.getPosition();
        // increase index for traversing in downstream direction
        // return first vehicle on lane with *lower* position than veh
        for (int i = 0, N = vehicles.size(); i < N; i++) {
            final Vehicle vehOnLane = vehicles.get(i);
            if(vehOnLane.getPosition() <= position){
                return vehOnLane;
            }
        }
        return null;
    }

    
    
}
