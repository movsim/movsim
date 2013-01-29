package org.movsim.network;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.opendrive.jaxb.ObjectFactory;
import org.opendrive.jaxb.OpenDRIVEElement;

public class OpenDriveNetwork {

	
	public void marshall() {
		try{
		    JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
		    
		    Marshaller m = jaxbContext.createMarshaller();
		    
		    
			org.opendrive.jaxb.ObjectFactory factory = new ObjectFactory();
			
			OpenDRIVEElement element = factory.createOpenDRIVEElement();

			m.setProperty("jaxb.formatted.output", Boolean.TRUE);
			
			m.marshal(element, System.out);
		} catch (JAXBException jbe) {
			// ...
		}
	}

	public void unMarshall(File xmlDocument) {
		try {

//			ClassLoader cl = org.opendrive.jaxb.ObjectFactory.class
//					.getClassLoader();

//			JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb", cl);
			JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			
			//System.out.println(unMarshaller.getSchema());
			//JAXBElement<OpenDRIVEElement> unmarshaled = (JAXBElement<OpenDRIVEElement>)unMarshaller.unmarshal(xmlDocument);
//			System.out.println(unmarshaled);
//			OpenDRIVE openDriveNetwork = openDriveJAXBElement.getValue();
		} catch (JAXBException e) {
			System.out.println(e.toString());
		}

	}
}
