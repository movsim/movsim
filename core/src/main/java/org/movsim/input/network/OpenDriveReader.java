package org.movsim.input.network;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.xml.sax.SAXException;

public class OpenDriveReader {

    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, File xodrFile) throws JAXBException, SAXException {
        return OpenDriveHandler.loadRoadNetwork(roadNetwork, xodrFile);
    }

}
