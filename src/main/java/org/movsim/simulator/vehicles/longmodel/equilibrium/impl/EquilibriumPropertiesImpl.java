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
package org.movsim.simulator.vehicles.longmodel.equilibrium.impl;

import java.io.PrintWriter;

import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.FileUtils;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class EquilibriumPropertiesImpl implements EquilibriumProperties{
    final static Logger logger = LoggerFactory.getLogger(EquilibriumPropertiesImpl.class);
    
    final static int NRHO = 51; // time critical 
    
    final double rhoMax;
    final double length;
    
    double qMax;
    double rhoQMax;
    
    double[] vEqTab;
    
    public EquilibriumPropertiesImpl(double length){
    	this.length = length;
        vEqTab = new double[NRHO];
        rhoMax = 1./length;
    }
    
    public double getQMax() {
        return qMax;
    }

    public double getRhoMax() {
        return rhoMax;
    }

    public double getRhoQMax() {
        return rhoQMax;
    }
    
    protected double getNetDistance(double rho){
        return rho!=0 ? (1./rho - 1./rhoMax) : 0;
    }
    
    // calculate Qmax, and abszissa rhoQmax from veqtab (necessary for BC)
    protected void calcRhoQMax(){
        int ir = 1;
        qMax = -1.;
        while(vEqTab[ir]*rhoMax*ir/vEqTab.length > qMax){
            qMax = vEqTab[ir]*rhoMax*ir/vEqTab.length;
            ir++;
        }
        rhoQMax = rhoMax*ir/vEqTab.length;
        logger.debug("rhoQMax = {} = {}/km", rhoQMax, rhoQMax*1000);
  }
    
   public double getVEq(double rho){
      return Tables.intp(vEqTab, rho, 0, rhoMax);
   }
   
   protected double getRho(int i){
       return rhoMax * i/(vEqTab.length-1);
   }

   public void writeOutput(String filename){
       PrintWriter fstr = FileUtils.getWriter(filename);
       fstr.printf(Constants.COMMENT_CHAR + " rho at max Q = %8.3f%n", 1000 * rhoQMax);
       fstr.printf(Constants.COMMENT_CHAR + " max Q        = %8.3f%n", 3600 * qMax);   
       fstr.printf(Constants.COMMENT_CHAR + " rho(1/km)  s(m)  velEq(km/h)   Q(veh/h)%n");
       for (int i = 0; i < vEqTab.length; i++) {
           final double rho = getRho(i);
           final double s = getNetDistance(rho);  
           fstr.printf("%8.2f  %8.2f  %8.2f  %8.2f%n", 1000*rho, s, 3.6*vEqTab[i], 3600*rho*vEqTab[i]);
       }
       fstr.close();
   }
   
   
    
    
}
