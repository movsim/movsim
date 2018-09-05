package org.movsim.input.network;

import org.movsim.simulator.roadnetwork.RoadNetwork;

import java.io.File;

public final class OpenDriveReader {

    private OpenDriveReader() {
        // do not invoke
    }

    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, File xodrFile) {
        return OpenDriveHandler.loadRoadNetwork(roadNetwork, xodrFile);
    }

}
