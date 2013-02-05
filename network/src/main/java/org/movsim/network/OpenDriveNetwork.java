package org.movsim.network;

import java.io.File;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.opendrive.jaxb.ObjectFactory;
import org.opendrive.jaxb.OpenDRIVE;
import org.opendrive.jaxb.OpenDRIVE.Road;
import org.xml.sax.SAXException;

public class OpenDriveNetwork {

    private static final File xsdFile = new File("src/main/resources/schema", "OpenDRIVE_1.3.xsd");

// public void marshall() {
// try{
// JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
// Marshaller m = jaxbContext.createMarshaller();
// org.opendrive.jaxb.ObjectFactory factory = new ObjectFactory();
// OpenDRIVE element = factory.createOpenDRIVE();
// m.setProperty("jaxb.formatted.output", Boolean.TRUE);
// m.marshal(element, System.out);
// } catch (JAXBException jbe) {
// // ...
// }
// }

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

    public OpenDRIVE unmarshall(File xmlDocument) throws JAXBException, SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(xsdFile);
        JAXBContext jaxbContext = JAXBContext.newInstance(OpenDRIVE.class);
        //JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new MyValidationEventHandler());
        return (OpenDRIVE) unmarshaller.unmarshal(xmlDocument);
    }
}
