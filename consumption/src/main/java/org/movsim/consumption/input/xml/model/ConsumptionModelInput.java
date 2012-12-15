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
package org.movsim.consumption.input.xml.model;

import java.util.Map;

import org.jdom.Element;
import org.movsim.consumption.input.xml.XmlElementNames;
import org.movsim.utilities.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionModelInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionModelInput.class);

    private final ConsumptionCarModelInput carData;

    private final ConsumptionEngineModelInput engineData;

    private final RotationModelInput rotationModelInput;

    private final String label;
    
    private final String type;
    
    private final boolean output;

    public ConsumptionModelInput(Element elem) {

        this.label = elem.getAttributeValue("label");
        this.type = elem.getAttributeValue("type");
        this.output = Boolean.parseBoolean(elem.getAttributeValue("output"));

        final Map<String, String> carDataMap = XmlUtils.putAttributesInHash(elem
                .getChild(XmlElementNames.ConsumptionCarData));
        carData = new ConsumptionCarModelInput(carDataMap);

        engineData = new ConsumptionEngineModelInput(elem.getChild(XmlElementNames.ConsumptionEngineData));
        
        rotationModelInput = new RotationModelInput(elem.getChild(XmlElementNames.ConsumptionRotatiionModel));

    }

    public ConsumptionCarModelInput getCarData() {
        return carData;
    }

    public ConsumptionEngineModelInput getEngineData() {
        return engineData;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public boolean isOutput() {
        return output;
    }

    public RotationModelInput getRotationModelInput() {
        return rotationModelInput;
    }

}
