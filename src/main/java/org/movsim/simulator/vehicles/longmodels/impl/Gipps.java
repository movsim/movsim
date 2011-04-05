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

import org.movsim.input.model.vehicle.longModel.ModelInputDataGipps;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodels.LongModelCategory;
import org.movsim.simulator.vehicles.longmodels.LongitudinalModel;

// paper reference and modifications ...


public class Gipps extends LongitudinalModelImpl implements LongitudinalModel{

    private double T; // ergibt sich aus dt !!
    private double v0;
    private double a;
    private double b;
    private double s0;
   
    public Gipps(String modelName, ModelInputDataGipps parameters){
        super(modelName, LongModelCategory.INTERATED_MAP_MODEL);
        this.T = parameters.getDt();  // Gipps: dt=T=Tr=tau_relax!
        this.v0 = parameters.getV0();
        this.a = parameters.getA();
        this.b = parameters.getB();
        this.s0 = parameters.getS0();
    }
    
    // copy constructor
    public Gipps(Gipps modelToCopy){
        super(modelToCopy.modelName(), modelToCopy.getModelCategory());
        this.T = modelToCopy.getT();
        this.v0 = modelToCopy.getV0();
        this.a = modelToCopy.getA();
        this.b = modelToCopy.getB();
        this.s0 = modelToCopy.getS0();
    }
    
    public double getT(){ return T; }
    public double getV0(){ return v0; }
    public double getA(){ return a; }
    public double getB(){ return b; }
    public double getS0(){ return s0; }
    

    public double accSimple(double s, double v, double dv){
       return acc(s, v, dv, v0, T);
    }
    
    public double acc(double s, double v, double dv, double v0Loc, double aLoc){
      double vp=v-dv;
      double vSafe=-b*T+Math.sqrt(b*b*T*T+vp*vp+2*b*Math.max(s-s0,0.)); // safe velocity
      double vNew=Math.min(vSafe, Math.min(v+aLoc*T, v0Loc));
      double aWanted = (vNew-v)/T;
      return aWanted;
    }

    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        
//        // Local dynamical variables
//        double s = cyclicBuf->get_s(iveh); //cyclicBuf->get_x(iveh-1) - cyclicBuf->get_l(iveh-1) - cyclicBuf->get_x(iveh);
//        //xveh[iveh-1]-length[iveh-1]-xveh[iveh];
//        double v= cyclicBuf->get_v(iveh); //vveh[iveh];
//        double dv= v - cyclicBuf->get_v(iveh-1); //vveh[iveh];
      
      // Local dynamical variables
      Vehicle vehFront = vehContainer.getLeader(me);
      double s  = me.netDistance(vehFront); 
      double v  = me.speed(); 
      double dv = (vehFront==null) ? 0 : v - vehFront.speed();

      // space dependencies modeled by speedlimits, alpha's
      // TODO check 
//      final double Tloc  = alphaT*T; 
      final double v0Loc = Math.min(alphaV0*v0, me.speedlimit());  // consider external speedlimit
      final double aLoc = alphaA*a;
      
      // actual Gipps formula
      return  acc(s, v, dv, v0Loc, aLoc);
        
    }

    public double parameterV0() {
        return v0;
    }


	public double getRequiredUpdateTime() {
		return this.T; // iterated map requires specific timestep!!
	}
   

}
