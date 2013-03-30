package org.movsim.input.network.opendrive;

import javax.xml.bind.JAXBException;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.xml.sax.SAXException;

public class OpenDriveReader {

    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, String fullXodrFileName) throws JAXBException,
            SAXException {
        return OpenDriveHandlerJaxb.loadRoadNetwork(roadNetwork, fullXodrFileName);
    }

}
