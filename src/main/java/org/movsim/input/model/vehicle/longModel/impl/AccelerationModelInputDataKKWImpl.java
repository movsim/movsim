/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.longModel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AccelerationModelInputDataKKWImpl.
 */
public class AccelerationModelInputDataKKWImpl extends AccelerationModelInputDataImpl implements
        AccelerationModelInputDataKKW {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelInputDataKKWImpl.class);

    /** The v0. */
    private double v0;
    private final double v0Default;

    /**
     * The k. Multiplikator fuer sync-Abstand D=lveh+k*v*tau
     */
    private double k;
    private final double kDefault;

    /**
     * The pb0. "Troedelwahrsch." for standing vehicles
     */
    private double pb0;
    private final double pb0Default;

    /**
     * The pb1. "Troedelwahrsch." for moving vehicles
     */
    private double pb1;
    private final double pb1Default;

    /**
     * The pa1. "Beschl.=Anti-Troedelwahrsch." falls v<vp
     */
    private double pa1;
    private final double pa1Default;

    /**
     * The pa2. "Beschl.=Anti-Troedelwahrsch." falls v>=vp
     */
    private double pa2;
    private final double pa2Default;

    /**
     * The vp. Geschw., ab der weniger "anti-getroedelt" wird
     */
    private double vp;
    private final double vpDefault;

    /**
     * Instantiates a new model input data kca impl.
     * 
     * @param modelName
     *            the model name
     * @param map
     *            the map
     */
    public AccelerationModelInputDataKKWImpl(Map<String, String> map) {
        super(ModelName.KKW);
        v0Default = v0 = Double.parseDouble(map.get("v0"));
        kDefault = k = Double.parseDouble(map.get("k"));
        pb0Default = pb0 = Double.parseDouble(map.get("pb0"));
        pb1Default = pb1 = Double.parseDouble(map.get("pb1"));
        pa1Default = pa1 = Double.parseDouble(map.get("pa1"));
        pa2Default = pa2 = Double.parseDouble(map.get("pa2"));
        vpDefault = vp = Double.parseDouble(map.get("vp"));
        checkParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl #checkParameters()
     */
    @Override
    protected void checkParameters() {
        if (v0 < 0 || k < 0 || pb0 < 0 || pb1 < 0 || pa1 < 0 || pa2 < 0 || vp < 0) {
            logger.error(" negative parameter values for {} not defined in input. please choose positive values. exit",
                    getModelName().name());
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.AccelerationModelInputDataImpl #resetParametersToDefault()
     */
    @Override
    public void resetParametersToDefault() {
        v0 = v0Default;
        k = kDefault;
        pb0 = pb0Default;
        pb1 = pb1Default;
        pa1 = pa1Default;
        pa2 = pa2Default;
        vp = vpDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getV0()
     */
    @Override
    public double getV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getK()
     */
    @Override
    public double getK() {
        return k;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPb0()
     */
    @Override
    public double getPb0() {
        return pb0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPb1()
     */
    @Override
    public double getPb1() {
        return pb1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPa1()
     */
    @Override
    public double getPa1() {
        return pa1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getPa2()
     */
    @Override
    public double getPa2() {
        return pa2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCA#getVp()
     */
    @Override
    public double getVp() {
        return vp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getV0Default()
     */
    @Override
    public double getV0Default() {
        return v0Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getkDefault()
     */
    @Override
    public double getkDefault() {
        return kDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getPb0Default()
     */
    @Override
    public double getPb0Default() {
        return pb0Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getPb1Default()
     */
    @Override
    public double getPb1Default() {
        return pb1Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getPa1Default()
     */
    @Override
    public double getPa1Default() {
        return pa1Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getPa2Default()
     */
    @Override
    public double getPa2Default() {
        return pa2Default;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #getVpDefault()
     */
    @Override
    public double getVpDefault() {
        return vpDefault;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setV0(double)
     */
    @Override
    public void setV0(double v0) {
        this.v0 = v0;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setK(double)
     */
    @Override
    public void setK(double k) {
        this.k = k;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setPb0(double)
     */
    @Override
    public void setPb0(double pb0) {
        this.pb0 = pb0;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setPb1(double)
     */
    @Override
    public void setPb1(double pb1) {
        this.pb1 = pb1;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setPa1(double)
     */
    @Override
    public void setPa1(double pa1) {
        this.pa1 = pa1;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setPa2(double)
     */
    @Override
    public void setPa2(double pa2) {
        this.pa2 = pa2;
        parametersUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKKW #setVp(double)
     */
    @Override
    public void setVp(double vp) {
        this.vp = vp;
        parametersUpdated();
    }

}
