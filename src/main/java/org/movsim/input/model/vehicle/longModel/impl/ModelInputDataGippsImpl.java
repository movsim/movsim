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
package org.movsim.input.model.vehicle.longModel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longModel.ModelInputDataGipps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelInputDataGippsImpl extends ModelInputDataImpl implements ModelInputDataGipps{
    
    final static Logger logger = LoggerFactory.getLogger(ModelInputDataGippsImpl.class);
    
    private double v0;
    private double a;
    private double b;
    private double s0;
    private double dt;
    
    public ModelInputDataGippsImpl(String modelName, Map<String,String> map) {
        super(modelName);
        this.v0 = Double.parseDouble(map.get("v0"));
        this.a  = Double.parseDouble(map.get("a"));
        this.b  = Double.parseDouble(map.get("b"));
        this.s0  = Double.parseDouble(map.get("s0"));
        this.dt  = Double.parseDouble(map.get("dt"));
        
        if(v0<0 || a<0 || b<0 || s0<0 || dt<0 ){
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit", modelName);
            System.exit(-1);
        }
    }

    public double getV0() {
        return v0;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getS0() {
        return s0;
    }

    public double getDt() {
        return dt;
    }
    
    
    
}
