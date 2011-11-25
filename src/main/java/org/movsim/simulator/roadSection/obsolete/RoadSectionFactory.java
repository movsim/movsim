package org.movsim.simulator.roadSection.obsolete;

import org.movsim.input.InputData;
import org.movsim.input.model.RoadInput;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoadSectionFactory {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RoadSectionFactory.class);

    public static RoadSection create(InputData inputData, final RoadInput roadInput,
            final VehicleGenerator vehGenerator) {
        final long roadId = roadInput.getId();
        if (roadId % 10 == 0) {
            logger.info("create MAINROAD for road id={}", roadId);
            return new RoadSectionImpl(inputData, roadInput, vehGenerator);
        } else if (roadId > 0) {
            logger.info("create ONRAMP for road id={}", roadId);
            // merging from onramp only to most-right lane (shoulder lane)
            return new OnrampMobilImpl(roadInput, vehGenerator, inputData.getProjectMetaData().getProjectName());
        }
        // quick hack for considering offramp (identified by negative id)
        else if (roadId < 0) {
            logger.info("create OFFRAMP for road id={}", roadId);
            return new OfframpImpl(roadInput);
        } else {
            logger.info("create MAINROAD for road id={}", roadId);
            return new RoadSectionImpl(inputData, roadInput, vehGenerator);
        }
    }
}
