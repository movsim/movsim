/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.model.vehicle.behavior.MemoryInputData;
import org.movsim.input.model.vehicle.behavior.NoiseInputData;
import org.movsim.input.model.vehicle.lanechange.LaneChangeInputData;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataACC;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataCCS;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataGipps;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataIDM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKKW;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKrauss;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNSM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNewell;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataOVM_FVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.movsim.utilities.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(VehicleInput.class);

    /** The label. cannot be changed while simulating */
    private final String label;

    /** The length. cannot be changed while simulating */
    private final double length;
    
    private final double width;

    /** The max deceleration. in m/s^2, positive (default: Infinity) */
    private final double maxDeceleration;

    /** The reaction time. cannot be changed while simulating */
    private final double reactionTime;

    /** Label for the fuel consumption model. Default is "none" */
    private final String fuelConsumptionLabel;

    /** The model input data. */
    private LongitudinalModelInputData modelInputData;

    private final LaneChangeInputData laneChangeInputData;

    /** The memory input data. */
    private MemoryInputData memoryInputData = null;

    /** The noise input data. */
    private NoiseInputData noiseInputData = null;

    /**
     * Instantiates a new vehicle input.
     * 
     * @param elem
     *            the elem
     */
    @SuppressWarnings("unchecked")
    public VehicleInput(Element elem) {
        this.label = elem.getAttributeValue("label");
        this.length = Double.parseDouble(elem.getAttributeValue("length"));
        this.width = Double.parseDouble(elem.getAttributeValue("width"));
        this.maxDeceleration = Double.parseDouble(elem.getAttributeValue("b_max"));
        this.reactionTime = Double.parseDouble(elem.getAttributeValue("reaction_time"));
        this.fuelConsumptionLabel = elem.getAttributeValue("consumption");

        final List<Element> longModelElems = elem.getChild(XmlElementNames.VehicleLongitudinalModel).getChildren();
        for (final Element longModelElem : longModelElems) {
            if (longModelElem.getName().equalsIgnoreCase(XmlElementNames.VehicleMemory)) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(longModelElem);
                memoryInputData = new MemoryInputData(map);
            } else if (modelInputData == null) {
                modelInputData = modelInputDataFactory(longModelElems.get(0));
            } else {
                logger.error("more than one acceleration model is specified for a vehicle!");
                System.exit(-1);
            }
        }

        final Element lcModelElem = elem.getChild(XmlElementNames.VehicleLaneChangeModel);
        laneChangeInputData = new LaneChangeInputData(lcModelElem);

        final Element noiseElem = elem.getChild(XmlElementNames.VehicleNoise);
        if (noiseElem != null) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(noiseElem);
            noiseInputData = new NoiseInputData(map);
        }
    }

    /**
     * Model input data factory.
     * 
     * @param elem
     *            the elem
     * @return the model input data
     */
    private static LongitudinalModelInputData modelInputDataFactory(Element elem) {
        final String modelName = elem.getName();
        final Map<String, String> map = XmlUtils.putAttributesInHash(elem);
        if (modelName.equals(LongitudinalModelBase.ModelName.IDM.name())) {
            return new LongitudinalModelInputDataIDM(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.ACC.name())) {
            return new LongitudinalModelInputDataACC(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.OVM_FVDM.name())) {
            return new LongitudinalModelInputDataOVM_FVDM(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.GIPPS.name())) {
            return new LongitudinalModelInputDataGipps(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.KRAUSS.name())) {
            return new LongitudinalModelInputDataKrauss(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.NEWELL.name())) {
            return new LongitudinalModelInputDataNewell(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.NSM.name())) {
            return new LongitudinalModelInputDataNSM(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.KKW.name())) {
            return new LongitudinalModelInputDataKKW(map);
        } else if (modelName.equals(LongitudinalModelBase.ModelName.CCS.name())) {
            return new LongitudinalModelInputDataCCS(map);
        } else {
            logger.error("model with name {} not yet implemented. exit.", modelName);
            System.exit(-1);
        }
        return null; // not reached, instead exit
    }

    public String getLabel() {
        return label;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getMaxDeceleration() {
        return maxDeceleration;
    }

    public LongitudinalModelInputData getAccelerationModelInputData() {
        return modelInputData;
    }

    public LaneChangeInputData getLaneChangeInputData() {
        return laneChangeInputData;
    }

    public boolean isWithMemory() {
        return (memoryInputData != null);
    }

    public MemoryInputData getMemoryInputData() {
        return memoryInputData;
    }

    public boolean isWithNoise() {
        return (noiseInputData != null);
    }

    public NoiseInputData getNoiseInputData() {
        return noiseInputData;
    }

    public double getReactionTime() {
        return reactionTime;
    }

    public String getFuelConsumptionLabel() {
        return fuelConsumptionLabel;
    }
}
