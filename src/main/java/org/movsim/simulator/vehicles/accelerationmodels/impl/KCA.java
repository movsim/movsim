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

import org.movsim.input.model.vehicle.longModel.ModelInputDataKCA;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModelCategory;
import org.movsim.simulator.vehicles.accelerationmodels.AccelerationModel;


// paper reference / Kerner book 
public class KCA extends LongitudinalModelImpl implements AccelerationModel{

    private static final double dtCA = 1; // update timestep for CA !!
    
	private double v0;
	private double k; //Multiplikator fuer sync-Abstand D=lveh+k*v*tau
	private double pb0; //"Troedelwahrsch." for standing vehicles
	private double pb1; //  "Troedelwahrsch." for moving vehicles
	private double pa1; //"Beschl.=Anti-Troedelwahrsch." falls v<vp
	private double pa2; // "Beschl.=Anti-Troedelwahrsch." falls v>=vp
	private double vp; // Geschw., ab der weniger "anti-getroedelt" wird
	
	private double length;
	
    public KCA(String modelName, ModelInputDataKCA parameters, double length){
        super(modelName, AccelerationModelCategory.CELLULAR_AUTOMATON);
        
        this.v0  = parameters.getV0();
        this.k   = parameters.getK();
        this.pb0 = parameters.getPb0();
        this.pb1 = parameters.getPb1();
        this.pa1 = parameters.getPa1();
        this.pa2 = parameters.getPa2();
        this.vp  = parameters.getVp();
        
        this.length = length; // model parameter!
    }
    
    
    // copy constructor
    public KCA(KCA kcaToCopy){
        super(kcaToCopy.modelName(), kcaToCopy.getModelCategory());
        this.v0  = kcaToCopy.getV0();
        this.k   = kcaToCopy.getK();
        this.pb0 = kcaToCopy.getPb0();
        this.pb1 = kcaToCopy.getPb1();
        this.pa1 = kcaToCopy.getPa1();
        this.pa2 = kcaToCopy.getPa2();
        this.vp  = kcaToCopy.getVp();
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
        
        final int v0Loc = (int) (alphaV0 * v0 + 0.5);    // adapt v0 spatially
        final int vLoc = (int) (v + 0.5);
        
        final double kLoc = alphaT*k;
        final int a = 1;    // cell length/dt^2  with dt=1 s and length 0.5 m => 0.5 m/s^2

        final double pa=(vLoc<vp) ? pa1 : pa2;
        final double pb=(vLoc<1) ? pb0 : pb1;
        final double D=length + kLoc*vLoc*dtCA; // double bei Kerner, da k reelle Zahl

        // dynamic part
        final int vSafe=(int)s;     // (Delta x-d)/tau mit s=Delta x-d und tau=1 (s)
        final int dvSign=(dv<-0.5) ? 1 : (dv>0.5) ? -1 : 0;
        final int vC = (s > D-length) ? vLoc+a*(int)dtCA : vLoc + a*(int)dtCA*dvSign;
        int vtilde=Math.min(Math.min(v0Loc,vSafe), vC);
        vtilde=Math.max(0, vtilde);

        // stochastic part
        double r1 = MyRandom.nextDouble();  // noise terms ~ G(0,1)
        final int xi = (r1<pb) ? -1 : (r1<pb+pa) ? 1 : 0;
        
        int vNew = 0;
        vNew = Math.min(vtilde + a*(int)dtCA*xi, vLoc+a*(int)dtCA);
        vNew = Math.min(Math.min(v0Loc, vSafe), vNew);
        vNew = Math.max(0, vNew);

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

    public double getK() {
        return k;
    }

    public double getPb0() {
        return pb0;
    }

    public double getPb1() {
        return pb1;
    }

    public double getPa1() {
        return pa1;
    }

    public double getPa2() {
        return pa2;
    }

    public double getVp() {
        return vp;
    }
    
    


}
