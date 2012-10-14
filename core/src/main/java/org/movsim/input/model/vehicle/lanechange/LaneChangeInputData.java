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
package org.movsim.input.model.vehicle.lanechange;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.utilities.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaneChangeInputData {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LaneChangeInputData.class);

    private final boolean isWithEuropeanRules;
    private final double critSpeedEuroRules; // in SI (m/s)
    private final LaneChangeMobilData lcMobilData;
    private boolean isInitializedMobilData = false;

    public LaneChangeInputData(Element elem) {
        final Map<String, String> map = XmlUtils.putAttributesInHash(elem);
        isWithEuropeanRules = Boolean.parseBoolean(map.get("eur_rules"));
        critSpeedEuroRules = Double.parseDouble(map.get("crit_speed_eur"));

        lcMobilData = new LaneChangeMobilData();

        @SuppressWarnings("unchecked")
        final List<Element> lcModelElems = elem.getChildren();
        for (final Element lcModelElem : lcModelElems) {
            if (lcModelElem.getName().equalsIgnoreCase(XmlElementNames.VehicleLaneChangeModelMobil)) {
                final Map<String, String> mapModel = XmlUtils.putAttributesInHash(lcModelElem);
                lcMobilData.init(mapModel);
                isInitializedMobilData = true;
            } else {
                logger.error("lane-changing model with name {} not yet implemented!", lcModelElem.getName());
                System.exit(-1);
            }
        }
    }

    public boolean isInitializedMobilData() {
        return isInitializedMobilData;
    }

    public boolean isWithEuropeanRules() {
        return isWithEuropeanRules;
    }

    public double getCritSpeedEuroRules() {
        return critSpeedEuroRules;
    }

    public LaneChangeMobilData getLcMobilData() {
        return lcMobilData;
    }
}
