package org.movsim.input.model.vehicle.laneChanging.impl;

import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.vehicle.laneChanging.LaneChangingInputData;
import org.movsim.input.model.vehicle.laneChanging.LaneChangingMobilData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaneChangingInputDataImpl implements LaneChangingInputData {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LaneChangingInputDataImpl.class);

    private boolean isWithEuropeanRules;

    private double critSpeedEuroRules; // in SI (m/s)

    private LaneChangingMobilData lcMobilData;
    
    private boolean isInitializedMobilData = false;

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

    @Override
    public boolean isInitializedMobilData() {
        return isInitializedMobilData;
    }

    @Override
    public boolean isWithEuropeanRules() {
        return isWithEuropeanRules;
    }

    @Override
    public double getCritSpeedEuroRules() {
        return critSpeedEuroRules;
    }

    @Override
    public LaneChangingMobilData getLcMobilData() {
        return lcMobilData;
    }
}
