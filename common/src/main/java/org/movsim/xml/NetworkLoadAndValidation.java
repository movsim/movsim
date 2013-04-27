package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.xml.sax.SAXException;

public final class NetworkLoadAndValidation {

    private static final Class<?> OPEN_DRIVE_FACTORY = org.movsim.network.autogen.opendrive.OpenDRIVE.class;

    private static final String OPEN_DRIVE_XML_SCHEMA = "/schema/OpenDRIVE_1.3.xsd";

    private static final URL OPEN_DRIVE_XSD_URL = NetworkLoadAndValidation.class.getResource(OPEN_DRIVE_XML_SCHEMA);

    private NetworkLoadAndValidation() {
    }

    public static OpenDRIVE validateAndLoadOpenDriveNetwork(final File xmlFile) throws JAXBException, SAXException {
        return new FileUnmarshaller<OpenDRIVE>().load(xmlFile, OpenDRIVE.class, OPEN_DRIVE_FACTORY, OPEN_DRIVE_XSD_URL);
    }

}
