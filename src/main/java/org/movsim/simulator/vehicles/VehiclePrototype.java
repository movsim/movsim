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
package org.movsim.simulator.vehicles;

import org.movsim.input.model.VehicleInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties;

public class VehiclePrototype {

    private double length;
    
    private double fraction;
    
    private double reactionTime;
    
    private AccelerationModel longModel;
    
    private EquilibriumProperties equiProperties;
    
    private VehicleInput vehicleInput;
    
    
    public VehiclePrototype(double fraction, AccelerationModel longModel, EquilibriumProperties equilProperties, VehicleInput vehicleInput){
        this.length = vehicleInput.getLength();
        this.reactionTime = vehicleInput.getReactionTime();
        this.fraction = fraction;
        this.longModel = longModel;
        this.equiProperties = equilProperties;
        this.vehicleInput = vehicleInput;
    }
    
    public double length(){ return length; }
    
    public double reactionTime(){ return reactionTime; }
    
    public boolean hasReactionTime(){ return (reactionTime + Constants.SMALL_VALUE > 0); }
    
    public double fraction(){ return fraction; }
    
    public void setFraction(double normFraction){
        this.fraction = normFraction;
    }

    public AccelerationModel getLongModel() {
        return longModel;
    }
    
    
    public double getRhoQMax(){
        return equiProperties.getRhoQMax();
    }
    public double getEquilibriumSpeed(double rho){
        return equiProperties.getVEq(rho);
    }
    public void writeFundamentalDiagram(String filename){
        equiProperties.writeOutput(filename);
    }
    
    public VehicleInput getVehicleInput() {
        return vehicleInput;
    }
    
}
