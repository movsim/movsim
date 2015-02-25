package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.movsim.scenario.initial.autogen.MovsimInitialConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class MovsimInitialConditionsLoader {
    /** The Constant logger. */
    private final static Logger LOG = LoggerFactory.getLogger(MovsimInitialConditionsLoader.class);

    private static final Class<?> FACTORY = MovsimInitialConditions.class;

    private static final String XML_SCHEMA = "/schema/MovsimInitialConditions.xsd";

    private static final URL XSD_URL = MovsimInitialConditionsLoader.class.getResource(XML_SCHEMA);

    private MovsimInitialConditionsLoader() {}

    private static MovsimInitialConditions validateAndLoadInput(final File xmlFile) throws JAXBException, SAXException {
        return new FileUnmarshaller<MovsimInitialConditions>().load(xmlFile, MovsimInitialConditions.class, FACTORY, XSD_URL);
    }

    /**
     * @throws IllegalStateException
     */
    public static MovsimInitialConditions unmarshallData(File xmlFile) {
        MovsimInitialConditions data = null;
        try {
            LOG.info("try to open file={}", xmlFile.getName());
            data = validateAndLoadInput(xmlFile);
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException(e.toString());
        }
        if (data == null) {
            LOG.error("input not valid. exit.");
            System.exit(-1);
        }
        return data;
    }

}
