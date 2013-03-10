package org.movsim.input.network.opendrive;

import javax.xml.bind.JAXBException;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.xml.sax.SAXException;

public class OpenDriveReader {

    final static boolean useJaxb = true; // true still for testing!

    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, String fullXodrFileName) throws JAXBException,
            SAXException {
        if (useJaxb) {
            return OpenDriveHandlerJaxb.loadRoadNetwork(roadNetwork, fullXodrFileName);
        }
        return OpenDriveReaderSax.loadRoadNetwork(roadNetwork, fullXodrFileName);
    }

}
