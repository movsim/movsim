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
package org.movsim.input.model.vehicle.longModel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKCA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataKCAImpl.
 */
public class AccelerationModelInputDataKCAImpl extends AccelerationModelInputDataImpl implements AccelerationModelInputDataKCA {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataKCAImpl.class);

    /** The v0. */
    private final double v0;
    
    /** The k. */
    private final double k; // Multiplikator fuer sync-Abstand D=lveh+k*v*tau
    
    /** The pb0. */
    private final double pb0; // "Troedelwahrsch." for standing vehicles
    
    /** The pb1. */
    private final double pb1; // "Troedelwahrsch." for moving vehicles
    
    /** The pa1. */
    private final double pa1; // "Beschl.=Anti-Troedelwahrsch." falls v<vp
    
    /** The pa2. */
    private final double pa2; // "Beschl.=Anti-Troedelwahrsch." falls v>=vp
    
    /** The vp. */
    private final double vp; // Geschw., ab der weniger "anti-getroedelt" wird

    /**
     * Instantiates a new model input data kca impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataKCAImpl(String modelName, Map<String, String> map) {
        super(modelName);
        this.v0 = Double.parseDouble(map.get("v0"));
        this.k = Double.parseDouble(map.get("k"));
        this.pb0 = Double.parseDouble(map.get("pb0"));
        this.pb1 = Double.parseDouble(map.get("pb1"));
        this.pa1 = Double.parseDouble(map.get("pa1"));
        this.pa2 = Double.parseDouble(map.get("pa2"));
        this.vp = Double.parseDouble(map.get("vp"));

        if (v0 < 0 || k < 0 || pb0 < 0 || pb1 < 0 || pa1 < 0 || pa2 < 0 || vp < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    modelName);
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getK()
     */
    @Override
    public double getK() {
        return k;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPb0()
     */
    @Override
    public double getPb0() {
        return pb0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPb1()
     */
    @Override
    public double getPb1() {
        return pb1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPa1()
     */
    @Override
    public double getPa1() {
        return pa1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPa2()
     */
    @Override
    public double getPa2() {
        return pa2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getVp()
     */
    @Override
    public double getVp() {
        return vp;
    }

}
