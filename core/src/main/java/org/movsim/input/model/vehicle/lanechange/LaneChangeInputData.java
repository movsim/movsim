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
package org.movsim.input.model.vehicle.lanechange;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaneChangeInputData {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LaneChangeInputData.class);

    private final boolean isWithEuropeanRules;

    private final double critSpeedEuroRules; // in SI (m/s)

    private final LaneChangeMobilData lcMobilData;

    private boolean isInitializedMobilData = false;

    public LaneChangeInputData(final Element elem) {
        final Map<String, String> map = XmlUtils.putAttributesInHash(elem);
        isWithEuropeanRules = Boolean.parseBoolean(map.get("eur_rules"));
        critSpeedEuroRules = Double.parseDouble(map.get("crit_speed_eur"));

        lcMobilData = new LaneChangeMobilData();

        final List<Element> lcModelElems = elem.getChildren();
        for (final Element lcModelElem : lcModelElems) {
            if (lcModelElem.getName().equalsIgnoreCase(XmlElementNames.VehicleLaneChangeModelMobil)) {
                final Map<String, String> mapModel = XmlUtils.putAttributesInHash(lcModelElem);
                lcMobilData.init(mapModel);
                isInitializedMobilData = true;
            } else {
                logger.error("lane-changing model with name {} not yet implemented!", lcModelElem.getName());
                // logger.error("more than one lane-changing model is specified for a vehicle!");
                System.exit(-1);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#isInitializedMobilData()
     */
    public boolean isInitializedMobilData() {
        return isInitializedMobilData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#isWithEuropeanRules()
     */
    public boolean isWithEuropeanRules() {
        return isWithEuropeanRules;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#getCritSpeedEuroRules()
     */
    public double getCritSpeedEuroRules() {
        return critSpeedEuroRules;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#getLcMobilData()
     */
    public LaneChangeMobilData getLcMobilData() {
        return lcMobilData;
    }
}
