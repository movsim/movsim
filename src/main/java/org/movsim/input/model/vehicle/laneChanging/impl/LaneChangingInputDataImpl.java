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
package org.movsim.input.model.vehicle.laneChanging.impl;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;
import org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData;
import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class LaneChangingInputDataImpl.
 */
public class LaneChangingInputDataImpl implements LaneChangingInputData {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LaneChangingInputDataImpl.class);

    private boolean isWithEuropeanRules;

    private double critSpeedEuroRules; // in SI (m/s)

    private LaneChangingMobilData lcMobilData;
    
    private boolean isInitializedMobilData = false;

    /**
     * Instantiates a new lane changing input data impl.
     *
     * @param elem the elem
     */
    public LaneChangingInputDataImpl(final Element elem) {
        final Map<String, String> map = XmlUtils.putAttributesInHash(elem);
        isWithEuropeanRules = Boolean.parseBoolean(map.get("eur_rules"));
        critSpeedEuroRules = Double.parseDouble(map.get("crit_speed_eur")); 

        lcMobilData = new LaneChangingMobilModelDataImpl();
	    
	  
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

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#isInitializedMobilData()
     */
    @Override
    public boolean isInitializedMobilData() {
        return isInitializedMobilData;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#isWithEuropeanRules()
     */
    @Override
    public boolean isWithEuropeanRules() {
        return isWithEuropeanRules;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#getCritSpeedEuroRules()
     */
    @Override
    public double getCritSpeedEuroRules() {
        return critSpeedEuroRules;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData#getLcMobilData()
     */
    @Override
    public LaneChangingMobilData getLcMobilData() {
        return lcMobilData;
    }
}
