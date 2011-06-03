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
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNSM;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataNewell;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference ...
// TODO implementation
/**
 * The Class Newell.
 */
public class Newell extends LongitudinalModelImpl implements AccelerationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Newell.class);
   
    /** The dt. */
    private final double dt;

    /**
     * Instantiates a new newell.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public Newell(String modelName, AccelerationModelInputDataNewell parameters) {
        super(modelName, AccelerationModelCategory.INTERATED_MAP_MODEL, parameters);
        this.dt = 1; // model parameter
        initParameters();
    }

    @Override
    protected void initParameters() {
        logger.debug("init model parameters");
        //this.v0 = ((AccelerationModelInputDataNewell) parameters).getV0();
        
    }
    
    // copy constructor
    /**
     * Instantiates a new newell.
     * 
     * @param newellToCopy
     *            the newell to copy
     */
//    public Newell(Newell newellToCopy) {
//        super(newellToCopy.modelName(), newellToCopy.getModelCategory());
//        this.dt = newellToCopy.getRequiredUpdateTime();
//    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        // TODO Auto-generated method stub
        // // space dependencies modeled by speedlimits, alpha's
        //
        // final double Tloc = alphaT*T;
        // final double v0loc = Math.min(alphaV0*v0, me.speedlimit()); //
        // consider external speedlimit
        // final double aloc = alphaA*a;

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double accSimple(double s, double v, double dv) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#parameterV0()
     */
    @Override
    public double parameterV0() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return dt; // cellular automaton requires specific dt
    }

}
