package org.movsim.network;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.opendrive.jaxb.ObjectFactory;
import org.opendrive.jaxb.OpenDRIVE;
import org.opendrive.jaxb.OpenDRIVE.Road;

public class OpenDriveNetwork {

	
	public void marshall() {
		try{
		    JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
		    
		    Marshaller m = jaxbContext.createMarshaller();
		    
			org.opendrive.jaxb.ObjectFactory factory = new ObjectFactory();
			
			OpenDRIVE element = factory.createOpenDRIVE();

			m.setProperty("jaxb.formatted.output", Boolean.TRUE);
			
			m.marshal(element, System.out);
		} catch (JAXBException jbe) {
			// ...
		}
	}

	public void unMarshall(File xmlDocument) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			OpenDRIVE unmarshaled = (OpenDRIVE)unMarshaller.unmarshal(xmlDocument);
			List<Road> roads = unmarshaled.getRoad();

			System.out.println("number of roads: "+roads.size());
			for(Road road : roads){
			    System.out.println("id="+road.getId()+", name="+road.getName()+ ", length="+road.getLength());
			}
		} catch (JAXBException e) {
			System.out.println(e.toString());
		}

	}
}
