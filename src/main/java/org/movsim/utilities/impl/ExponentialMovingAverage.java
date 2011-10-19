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
package org.movsim.utilities.impl;

import java.util.List;


public class ExponentialMovingAverage {

    private double tau;
    
    public ExponentialMovingAverage(double tau) {
        this.tau = tau;
    } 
        
    
    public double calcEMA(double time, final List<XYDataPoint> timeSeries){
        if(timeSeries.isEmpty() ){
            return 0;
        }
        double norm = 0;
        double result = 0;
        for(XYDataPoint dp : timeSeries){
            final double phi = weight(time, dp.getX());
            norm += phi;
            result += phi*dp.getY();
        }
        return result/norm;
    }
    
    private double weight(double t1, double t2){
        return Math.exp(-Math.abs( (t1-t2)/tau ));
    }
    

    
}
