package org.movsim.simulator.roadnetwork;

import com.google.common.base.Preconditions;
import org.movsim.autogen.RoadTypeEnum;
import org.movsim.autogen.RoadTypeSpeedMappingType;
import org.movsim.autogen.RoadTypeSpeedMappingsType;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public enum RoadTypeSpeeds {

    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(RoadTypeSpeeds.class);

    private final Map<RoadTypeEnum, Double> roadTypeSpeedMappings = new EnumMap<>(RoadTypeEnum.class);

    RoadTypeSpeeds() {
        initWithDummyValues();
    }

    private void initWithDummyValues() {
        final double noLimitedFreeFlowSpeed = MovsimConstants.MAX_VEHICLE_SPEED;
        for (RoadTypeEnum roadTypeEnum : RoadTypeEnum.values()) {
            roadTypeSpeedMappings.put(roadTypeEnum, noLimitedFreeFlowSpeed);
        }
    }

    /**
     * Overwrites the dummy freeflow speed mappings with the values from the configuration.
     *
     * @param configuration
     */
    public void init(RoadTypeSpeedMappingsType configuration) {
        for (RoadTypeSpeedMappingType roadTypeSpeedMapping : configuration.getRoadTypeSpeedMapping()) {
            RoadTypeEnum key = roadTypeSpeedMapping.getRoadType();
            roadTypeSpeedMappings.put(key, roadTypeSpeedMapping.getDefaultSpeedKmh() * Units.KMH_TO_MS);
            LOG.info("mapping of default speed: roadtype={} --> speed={} km/h", key,
                    roadTypeSpeedMappings.get(key) * Units.MS_TO_KMH);
        }
    }

    public double getDefaultFreeFlowSpeed() {
        return roadTypeSpeedMappings.get(RoadTypeEnum.UNKNOWN);
    }

    public double getFreeFlowSpeed(RoadTypeEnum roadType) {
        Preconditions.checkArgument(roadTypeSpeedMappings.containsKey(roadType),
                "no speed mapping defined for roadType=" + roadType);
        return roadTypeSpeedMappings.get(roadType);
    }

    public double getFreeFlowSpeed(String xodrRoadType) {
        double freeFlowSpeed = getDefaultFreeFlowSpeed();
        try {
            RoadTypeEnum roadType = RoadTypeEnum.fromValue(xodrRoadType);
            freeFlowSpeed = roadTypeSpeedMappings.get(roadType);
        } catch (IllegalArgumentException e) {
            LOG.error("cannot map xodr road.type=" + xodrRoadType + " to a Movsim freeflow speed. Fall back to default="
                    + RoadTypeEnum.UNKNOWN);
        }
        return freeFlowSpeed;
    }

}
