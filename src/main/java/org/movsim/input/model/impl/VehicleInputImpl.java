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
package org.movsim.input.model.impl;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.vehicle.behavior.MemoryInputData;
import org.movsim.input.model.vehicle.behavior.NoiseInputData;
import org.movsim.input.model.vehicle.behavior.impl.MemoryInputDataImpl;
import org.movsim.input.model.vehicle.behavior.impl.NoiseInputDataImpl;
import org.movsim.input.model.vehicle.longModel.ModelInputData;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataACCImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataGippsImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataIDMImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataKCAImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNSMImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataNewellImpl;
import org.movsim.input.model.vehicle.longModel.impl.ModelInputDataOVM_VDIFFImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VehicleInputImpl implements VehicleInput {
    final static Logger logger = LoggerFactory.getLogger(VehicleInputImpl.class);

    private final String label;// cannot be changed while simulating

    private final double length;// cannot be changed while simulating

    private double maxDeceleration; // in m/s^2, positive (default: Infinity)
    
    private final double reactionTime; // cannot be changed while simulating

    private ModelInputData modelInputData;

    private MemoryInputData memoryInputData = null;
    
    private NoiseInputData noiseInputData = null;

    @SuppressWarnings("unchecked")
    public VehicleInputImpl(Element elem) {
        this.label = elem.getAttributeValue("label");
        this.length = Double.parseDouble(elem.getAttributeValue("length"));
        this.maxDeceleration = Double.parseDouble(elem.getAttributeValue("b_max"));
        this.reactionTime = Double.parseDouble(elem.getAttributeValue("reaction_time"));

        List<Element> longModelElems = elem.getChild("LONGITUDINAL_MODEL").getChildren();
        if (longModelElems.size() != 1) {
            logger.error("specify only one long model ! exit ");
            System.exit(-1);
        } else {
            modelInputData = modelInputDataFactory(longModelElems.get(0));
        }

        final Element memoryElem = elem.getChild("MEMORY");
        if (memoryElem != null) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(memoryElem);
            memoryInputData = new MemoryInputDataImpl(map);
        }

        final Element noiseElem = elem.getChild("NOISE");
        if (noiseElem != null) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(noiseElem);
            noiseInputData = new NoiseInputDataImpl(map);
        }
    }

   

    private ModelInputData modelInputDataFactory(Element elem) {
        String modelName = elem.getName();
        Map<String, String> map = XmlUtils.putAttributesInHash(elem);
        if (modelName.equalsIgnoreCase("IDM")) {
            return new ModelInputDataIDMImpl("IDM", map);
        } else if (modelName.equalsIgnoreCase("ACC")) {
            return new ModelInputDataACCImpl("ACC", map);
        } else if (modelName.equalsIgnoreCase("OVM_VDIFF")) {
            return new ModelInputDataOVM_VDIFFImpl("OVM_VDIFF", map);
        } else if (modelName.equalsIgnoreCase("GIPPS")) {
            return new ModelInputDataGippsImpl("GIPPS", map);
        } else if (modelName.equalsIgnoreCase("NEWELL")) {
            return new ModelInputDataNewellImpl("NEWELL", map);
        } else if (modelName.equalsIgnoreCase("NSM")) {
            return new ModelInputDataNSMImpl("NSM", map);
        } else if (modelName.equalsIgnoreCase("KCA")) {
            return new ModelInputDataKCAImpl("KCA", map);
        } else {
            logger.error("model with name {} not yet implemented. exit.", modelName);
            System.exit(-1);
        }
        return null; // not reached, instead exit
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getLabel()
     */
    public String getLabel() {
        return label;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getLength()
     */
    public double getLength() {
        return length;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getMaxDeceleration()
     */
    public double getMaxDeceleration() {
        return maxDeceleration;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getModelInputData()
     */
    public ModelInputData getModelInputData() {
        return modelInputData;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#isWithMemory()
     */
    public boolean isWithMemory() {
        return (memoryInputData != null);
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getMemoryInputData()
     */
    public MemoryInputData getMemoryInputData() {
        return memoryInputData;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#isWithNoise()
     */
    public boolean isWithNoise() {
        return (noiseInputData != null);
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.VehicleInput#getNoiseInputData()
     */
    public NoiseInputData getNoiseInputData() {
        return noiseInputData;
    }
    
    public double getReactionTime(){
        return reactionTime;
    }

}
