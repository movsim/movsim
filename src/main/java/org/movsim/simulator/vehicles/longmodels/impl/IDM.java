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
package org.movsim.simulator.vehicles.longmodels.impl;

import org.movsim.input.model.vehicle.longModel.ModelInputDataIDM;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.impl.VehicleImpl;
import org.movsim.simulator.vehicles.longmodels.LongModelCategory;
import org.movsim.simulator.vehicles.longmodels.LongitudinalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




//Reference: 
//Martin Treiber, Ansgar Hennecke, and Dirk Helbing
//Congested traffic states in empirical observations and microscopic simulations
//Physical Review E 62, 1805–1824 (2000)

public class IDM extends LongitudinalModelImpl implements LongitudinalModel{
    
    final static Logger logger = LoggerFactory.getLogger(IDM.class);
    
    // IDM parameters
    private double v0;// start_stop=15; // desired velocity (m/s)
    private double T; // time headway (s)
    private double s0; // bumper-to-bumper distance in jams or queues
    private double s1;
    private double a; // acceleration (m/s^2)
    private double b; // comfortable (desired) deceleration (m/s^2)
    private double delta; // acceleration exponent
    
    
    public IDM(String modelName, ModelInputDataIDM parameters){
        super(modelName, LongModelCategory.CONTINUOUS_MODEL);
        this.v0 = parameters.getV0();
        this.T = parameters.getT();
        this.s0 = parameters.getS0();
        this.s1 = parameters.getS1();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.delta = parameters.getDelta();
    }
    
    
    // copy constructor
    public IDM(IDM idmToCopy){
        super(idmToCopy.modelName(), idmToCopy.getModelCategory());
        this.v0 = idmToCopy.getV0();
        this.T = idmToCopy.getT();
        this.s0 = idmToCopy.getS0();
        this.s1 = idmToCopy.getS1();
        this.a = idmToCopy.getA();
        this.b = idmToCopy.getB();
        this.delta = idmToCopy.getDelta();
    }
    
    
    public double getV0(){ return v0;}
    public double getT(){ return T; }
    public double getS0(){ return s0;}
    public double getS1(){ return s1;}
    public double getDelta(){ return delta;}
    public double getA(){ return a;}
    public double getB(){ return b;}
    
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        // Local dynamical variables
        final Vehicle vehFront = vehContainer.getLeader(me);
        final double s  = me.netDistance(vehFront); 
        final double v  = me.speed(); 
        final double dv = me.relSpeed(vehFront);

        // space dependencies modeled by speedlimits, alpha's

        final double TLocal  = alphaT*T; 
        final double v0Local = Math.min(alphaV0*v0, me.speedlimit());  // consider external speedlimit
        final double aLocal = alphaA*a;
        
        double sstar  = s0 + TLocal*v + s1*Math.sqrt((v+0.0001)/v0Local) + (0.5*v*dv)/Math.sqrt(aLocal*b);

//        if(sstar<s0+0.2*v*Tloc){
//            sstar=s0+0.2*v*Tloc;
//        }
        if(sstar<s0){ sstar=s0;}  
        

        final double aWanted = aLocal*( 1.- Math.pow((v/v0Local), delta) - (sstar/s)*(sstar/s));

        // logger.debug("aWantet = {}", aWanted);
        return  aWanted; // limit to -bMax in Vehicle
    }

    public double accSimple(double s, double v, double dv){
      double sstar  = s0 + T*v + 0.5*v*dv /Math.sqrt(a*b); // desired distance
      sstar += s1*Math.sqrt((v+0.000001)/v0);
      if(sstar<s0){ sstar=s0;}
      final double aWanted = a * ( 1.- Math.pow((v/v0), delta) - (sstar/s)*(sstar/s));
      return  aWanted;  // limit to -bMax in Vehicle
    }


    public double parameterV0() {
        return v0;
    }


	@Override
	public double getRequiredUpdateTime() {
		return 0; // continuous model requires no specific timestep
	}


    
}
