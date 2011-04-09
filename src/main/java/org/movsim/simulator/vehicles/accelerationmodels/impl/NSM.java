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

import org.movsim.input.model.vehicle.longModel.ModelInputDataNSM;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModelCategory;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModel;


// Nagel-Schreckenberg or Barlovic-Model
// paper reference
public class NSM extends LongitudinalModelImpl implements AccelerationModel {

    // unit time for CA:
	private static final double dtCA = 1; // update timestep for CA !!
	
	private double v0;
	private double pTroedel; 
	private double pSlowToStart; // slow-to-start rule for Barlovic model
    
    public NSM(String modelName, ModelInputDataNSM parameters){
        super(modelName, AccelerationModelCategory.CELLULAR_AUTOMATON);
        this.v0 = parameters.getV0();
        this.pTroedel = parameters.getTroedel();
        this.pSlowToStart = parameters.getSlowToStart(); 
    }
    
    
    // copy constructor
    public NSM(NSM nsmToCopy){
        super(nsmToCopy.modelName(), nsmToCopy.getModelCategory());
        this.v0 = nsmToCopy.getV0();
        this.pTroedel = nsmToCopy.getTroedel();
        this.pSlowToStart = nsmToCopy.getSlowToStart();
    }
    
    
    
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        // Local dynamical variables
        Vehicle vehFront = vehContainer.getLeader(me);
        double s  = me.netDistance(vehFront); 
        double v  = me.speed(); 
        double dv = me.relSpeed(vehFront);
        return  accSimple(s, v, dv, alphaT, alphaV0);
    }

    public double accSimple(double s, double v, double dv) {
        return accSimple(s, v, dv, 1, 1);
    }
    
    
    private double accSimple(double s, double v, double dv, double alphaT, double alphaV0) {
        int v0Loc = (int) (alphaV0 * v0 + 0.5);    // adapt v0 spatially
        int vLoc = (int) (v + 0.5);
        int vNew = 0;

        final double r1 = MyRandom.nextDouble();
        final double pb = (vLoc < 1) ? pSlowToStart : pTroedel;
        final int troedel = (r1 < pb) ? 1 : 0;

        final int sLoc = (int) (s + 0.5);
        vNew = Math.min(vLoc + 1, v0Loc);
        vNew = Math.min(vNew, sLoc);
        vNew = Math.max(0, vNew - troedel);
        

        return ((vNew-vLoc)/dtCA);
    }


    public double parameterV0() {
        return v0;
    }


	public double getRequiredUpdateTime() {
		return dtCA; // cellular automaton requires specific dt
	}


    public double getV0() {
        return v0;
    }


    public double getTroedel() {
        return pTroedel;
    }


    public double getSlowToStart() {
        return pSlowToStart;
    }
	
}
