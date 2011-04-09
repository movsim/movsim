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
package org.movsim.simulator.vehicles.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.ModelInputDataNewell;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModelCategory;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModel;


// paper reference ...
// TODO implementation
public class Newell extends LongitudinalModelImpl implements AccelerationModel{

    private final double dt;
    
    public Newell(String modelName, ModelInputDataNewell parameters){
        super(modelName, AccelerationModelCategory.INTERATED_MAP_MODEL);
        this.dt = 1; // model parameter
    }
    
    
    // copy constructor
    public Newell(Newell newellToCopy){
        super(newellToCopy.modelName(), newellToCopy.getModelCategory());
        this.dt = newellToCopy.getRequiredUpdateTime();
    }
    
    
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        // TODO Auto-generated method stub
//    	// space dependencies modeled by speedlimits, alpha's
//
//        final double Tloc  = alphaT*T; 
//        final double v0loc = Math.min(alphaV0*v0, me.speedlimit());  // consider external speedlimit
//        final double aloc = alphaA*a;

        return 0;
    }

    public double accSimple(double s, double v, double dv) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public double parameterV0() {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public double getRequiredUpdateTime() {
        return dt; // cellular automaton requires specific dt
    }


}
