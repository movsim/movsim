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

import org.movsim.input.model.vehicle.longModel.ModelInputDataOVM_VDIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelInputDataOVM_VDIFFImpl extends ModelInputDataImpl implements ModelInputDataOVM_VDIFF{

    final static Logger logger = LoggerFactory.getLogger(ModelInputDataOVM_VDIFFImpl.class);
    
    private double s0;
    private double v0;
    private double tau;
    private double lenInteraction;
    private double beta;
    private double lambda;
    private int variant;

    public ModelInputDataOVM_VDIFFImpl(String modelName, Map<String, String> map) {
        super(modelName);
        this.s0 = Double.parseDouble(map.get("s0"));
        this.v0 = Double.parseDouble(map.get("v0"));
        this.tau = Double.parseDouble(map.get("tau"));
        this.lenInteraction = Double.parseDouble(map.get("l_int"));
        this.beta = Double.parseDouble(map.get("beta"));
        this.lambda = Double.parseDouble(map.get("lambda"));
        this.variant = Integer.parseInt(map.get("variant"));
        
        if(s0<0 ||  v0<0 || tau<0 || lenInteraction<0 || beta<0 || lambda<0 || variant < 0){
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit", modelName);
            System.exit(-1);
        }
        
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getS0()
     */
    public double getS0() {
        return s0;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getV0()
     */
    public double getV0() {
        return v0;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getTau()
     */
    public double getTau() {
        return tau;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getLenInteraction()
     */
    public double getLenInteraction() {
        return lenInteraction;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getBeta()
     */
    public double getBeta() {
        return beta;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getLambda()
     */
    public double getLambda(){
        return lambda;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFF#getVariant()
     */
    public int getVariant(){
        return variant;
    }
    
}
