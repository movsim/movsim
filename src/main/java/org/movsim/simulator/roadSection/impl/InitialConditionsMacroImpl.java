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
package org.movsim.simulator.roadSection.impl;

import java.util.List;

import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.InitialConditionsMacro;
import org.movsim.utilities.Tables;


public class InitialConditionsMacroImpl implements InitialConditionsMacro{

   // final static double SMALL_VAL = 1e-7;
    
    double[] pos;
    double[] rho;
    double[] speed;
    
    
    
    public InitialConditionsMacroImpl(List<ICMacroData> icData){
    
        final int size = icData.size();
        
        pos = new double[size];
        rho = new double[size];
        speed = new double[size];
        
        // case speed = 0 --> set vehicle ast equilibrium speed  
        
        // generateMacroFields: rho guaranteed to be > RHOMIN, v to be < VMAX
        
        for(int i=0; i<size; i++){
            final double rhoLocal = icData.get(i).getRho();
            if(rhoLocal > Constants.SMALL_VALUE){
                pos[i] = icData.get(i).getX();
                rho[i] = rhoLocal;
                final double speedLocal = icData.get(i).getSpeed();
                speed[i] = (speedLocal<= Constants.MAX_VEHICLE_SPEED) ? speedLocal : 0;
            }
        }
    }


    public double vInit(double x) {
        return Tables.intpextp(pos, speed, x);
    }

    public double rho(double x) {
        return Tables.intpextp(pos, rho, x);
    }
    
}
