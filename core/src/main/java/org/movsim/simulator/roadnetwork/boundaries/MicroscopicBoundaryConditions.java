package org.movsim.simulator.roadnetwork.boundaries;

import com.google.common.base.Preconditions;
import org.movsim.scenario.boundary.autogen.BoundaryConditionsType;
import org.movsim.scenario.boundary.autogen.MovsimMicroscopicBoundaryConditions;
import org.movsim.scenario.boundary.autogen.RoadMicroscopicBoundaryConditionsType;
import org.movsim.xml.InputLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MicroscopicBoundaryConditions {

    private static final Logger LOG = LoggerFactory.getLogger(MicroscopicBoundaryConditions.class);

    private final Map<String, RoadMicroscopicBoundaryConditionsType> roadToBoundaryConditions;

    private final File file;

    private final String timeFormat;

    public MicroscopicBoundaryConditions(File file) {
        this.file = Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists(), "micro boundary conditions file " + file + " not found");
        roadToBoundaryConditions = new HashMap<>();

        MovsimMicroscopicBoundaryConditions input = InputLoader.unmarshallMicroBoundaryConditions(file);
        LOG.info("loaded initial conditions from file={}", file);

        this.timeFormat = input.getTimeFormat();
        LOG.debug("for input file={} use time format={}", file, timeFormat);
        fillMapping(input);
    }

    /**
     * @throws IllegalStateException
     */
    private void fillMapping(MovsimMicroscopicBoundaryConditions input) {
        for (RoadMicroscopicBoundaryConditionsType roadConditions : input.getRoadMicroscopicBoundaryConditions()) {
            String roadId = roadConditions.getId();
            if (roadToBoundaryConditions.containsKey(roadId)) {
                throw new IllegalStateException("roadId=" + roadId + "already used in input file=" + file);
            }
            roadToBoundaryConditions.put(roadId, roadConditions);
        }
        LOG.info("unmarshalled initial conditions for {} roads", roadToBoundaryConditions.size());
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    @CheckForNull
    public BoundaryConditionsType getBoundaryConditions(String roadUserId) {
        return roadToBoundaryConditions.get(roadUserId).getBoundaryConditions();
    }

}
