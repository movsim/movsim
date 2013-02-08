package org.movsim.network;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road;
import org.xml.sax.SAXException;

public class OpenDriveNetwork {

    public static OpenDRIVE loadNetwork(final File xmlFile) throws JAXBException, SAXException {
	NetworkLoadAndValidation xmlValidator = new NetworkLoadAndValidation();
	OpenDRIVE openDrive = xmlValidator.validateAndLoadOpenDriveNetwork(xmlFile);
	System.out.println("network loaded from file=" + xmlFile.getName());
	return openDrive;
    }
    
    public void unMarshall(File xmlDocument) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
            Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
            OpenDRIVE unmarshaled = (OpenDRIVE) unMarshaller.unmarshal(xmlDocument);
            if (unmarshaled.isSetRoad()) {
                List<Road> roads = unmarshaled.getRoad();
                System.out.println("number of roads: " + roads.size());
                for (Road road : roads) {
                    System.out.println("id=" + road.getId() + ", name=" + road.getName() + ", length=" + road.getLength());
                }
            }
        } catch (JAXBException e) {
            System.out.println(e.toString());
        }

    }

  
}
