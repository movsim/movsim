package org.movsim.network;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.opendrive.jaxb.OpenDRIVE;
import org.xml.sax.SAXException;

public class NetworkMain {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File networkFile = new File("routing.xodr");
		
		OpenDriveNetwork network = new OpenDriveNetwork();
		
//		System.out.println("Marshall: ");
//		network.marshall();
		
		System.out.println("With validation: ");
        try {
            OpenDRIVE unmarshall = network.unmarshall(networkFile);
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		
        System.out.println("Unmarshall: ");
        network.unMarshall(networkFile);
	}

}
