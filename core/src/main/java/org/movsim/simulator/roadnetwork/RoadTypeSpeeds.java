package org.movsim.simulator.roadnetwork;

import java.util.EnumMap;

import org.movsim.autogen.RoadTypeEnum;
import org.movsim.autogen.RoadTypeSpeedMappingType;
import org.movsim.autogen.RoadTypeSpeedMappingsType;
import org.movsim.simulator.MovsimConstants;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public enum RoadTypeSpeeds {

    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(RoadTypeSpeeds.class);

    private final EnumMap<RoadTypeEnum, Double> roadTypeSpeedMappings = new EnumMap<>(RoadTypeEnum.class);

    private RoadTypeSpeeds() {
        for (RoadTypeEnum roadTypeEnum : RoadTypeEnum.values()) {
            roadTypeSpeedMappings.put(roadTypeEnum, MovsimConstants.MAX_VEHICLE_SPEED);
        }
        // define useful speeds for non-highways
        roadTypeSpeedMappings.put(RoadTypeEnum.RURAL, 100 * Units.KMH_TO_MS);
    }

    public void init(RoadTypeSpeedMappingsType configuration) {
        for (RoadTypeSpeedMappingType roadTypeSpeedMapping : configuration.getRoadTypeSpeedMapping()) {
            RoadTypeEnum key = roadTypeSpeedMapping.getRoadType();
            roadTypeSpeedMappings.put(key, roadTypeSpeedMapping.getDefaultSpeedKmh() * Units.KMH_TO_MS);
            LOG.info("mapping of default speed: roadtype={} --> speed={} km/h", key, roadTypeSpeedMappings.get(key)
                    * Units.MS_TO_KMH);
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

}
