/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class VehicleContainerImpl.
 */
public class VehicleContainerImpl implements VehicleContainer {
    final static Logger logger = LoggerFactory.getLogger(VehicleContainerImpl.class);

    // array sorted in x-position with decreasing positions
    // lowest index has most downstream position on road

    private final List<Vehicle> vehicles;

    private int vehMainroadCounter;

    private int vehRampCounter;

    /**
     * Instantiates a new vehicle container impl.
     */
    public VehicleContainerImpl() {
        vehicles = new ArrayList<Vehicle>();
        vehMainroadCounter = 0;
        vehRampCounter = -1; // count negative to distinguish from vehicle
                             // entered from mainroad
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

    // sollte damit immer aufsteigend in pos sortiert sein
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#add(org.movsim.simulator
     * .vehicles.Vehicle, double, double, int)
     */
    @Override
    public void add(Vehicle veh, double xInit, double vInit, int laneInit) {
        vehMainroadCounter++;
        add(vehMainroadCounter, veh, xInit, vInit, laneInit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#addFromRamp(org.movsim
     * .simulator.vehicles.Vehicle, double, double, int)
     */
    @Override
    public void addFromRamp(Vehicle veh, double xInit, double vInit, int laneInit) {
        vehRampCounter--; // count negative
        add(vehRampCounter, veh, xInit, vInit, laneInit);
    }

    /**
     * Adds the.
     * 
     * @param vehNumber
     *            the veh number
     * @param veh
     *            the veh
     * @param xInit
     *            the x init
     * @param vInit
     *            the v init
     * @param laneInit
     *            the lane init
     */
    private void add(int vehNumber, Vehicle veh, double xInit, double vInit, int laneInit) {
        veh.setVehNumber(vehNumber);

        veh.init(xInit, vInit, laneInit);

        if (vehicles.isEmpty()) {
            vehicles.add(veh);
        } else if (veh.position() < getMostUpstream().position()) {
            // add after entry with greatest index
            vehicles.add(veh);
        } else if (veh.position() > getMostDownstream().position()) {
            // add before first entry
            vehicles.add(0, veh);
        } else {
            vehicles.add(0, veh);
            sort(); // robust but runtime performance ?
        }
        logger.info("vehicleContainerImpl: vehicle added: x={}, v={}", veh.position(), veh.speed());
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
        while (!vehicles.isEmpty() && getMostDownstream().position() > roadLength) {
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.VehicleContainer#getLeader(org.movsim.simulator
     * .vehicles.Vehicle)
     */
    @Override
    public Vehicle getLeader(Vehicle veh) {
        final int index = vehicles.indexOf(veh);
        if (index == -1 || index == 0)
            return null;
        return vehicles.get(index - 1);
    }

    // for multi-lane extensions: sort will be needed
    /**
     * Sort.
     */
    private void sort() {
        // sortierreihenfolge festgelegt durch "pos2.compareTo(pos1) -->
        // absteigend in pos! OK
        Collections.sort(vehicles, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle o1, Vehicle o2) {
                final Double pos1 = new Double((o1).position());
                final Double pos2 = new Double((o2).position());
                return pos2.compareTo(pos1);
            }
        });
    }

}
