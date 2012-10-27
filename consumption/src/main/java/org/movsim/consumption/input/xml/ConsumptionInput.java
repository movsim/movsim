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
package org.movsim.consumption.input.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.consumption.input.xml.model.ConsumptionModelInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionInput.class);

    private Map<String, ConsumptionModelInput> consumptionModelInput;

    @SuppressWarnings("unchecked")
    public ConsumptionInput(Element elem) {

        // fuel consumption element is optional
        if (elem == null) {
            return;
        }

        consumptionModelInput = new HashMap<String, ConsumptionModelInput>();

        final List<Element> fuelvehicleElements = elem.getChildren(XmlElementNames.ConsumptionModel);

        for (final Element fuelModelElem : fuelvehicleElements) {
            final ConsumptionModelInput consModel = new ConsumptionModelInput(fuelModelElem);
            consumptionModelInput.put(consModel.getLabel(), consModel);
        }
    }

    public Map<String, ConsumptionModelInput> getConsumptionModelInput() {
        return consumptionModelInput;
    }
}
