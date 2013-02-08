package org.movsim.network;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.xml.sax.SAXException;

public class NetworkMain {

	/**
	 * @param args
	 */
    // TODO create tests for loading/validating xodr files
	public static void main(String[] args) {
		File networkFile = new File("routing.xodr");
		OpenDriveNetwork network = new OpenDriveNetwork();
		System.out.println("With validation: ");
        try {
            OpenDRIVE unmarshall = network.loadNetwork(networkFile);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
		
        System.out.println("Unmarshall: ");
        network.unMarshall(networkFile);
	}

}
