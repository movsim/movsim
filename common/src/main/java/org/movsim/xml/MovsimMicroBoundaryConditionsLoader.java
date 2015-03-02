package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.jdom.IllegalDataException;
import org.movsim.scenario.boundary.autogen.MovsimMicroscopicBoundaryConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class MovsimMicroBoundaryConditionsLoader {
    private static final Logger LOG = LoggerFactory.getLogger(MovsimMicroBoundaryConditionsLoader.class);

    private static final Class<?> FACTORY = MovsimMicroscopicBoundaryConditions.class;
    private static final String XML_SCHEMA = "/schema/MovsimMicroscopicBoundaryConditions.xsd";

    public static MovsimMicroscopicBoundaryConditions unmarshall(File file) {
        MovsimMicroBoundaryConditionsLoader loader = new MovsimMicroBoundaryConditionsLoader();
        return loader.unmarshallData(file);
    }

    private URL getUrl() {
        return MovsimMicroBoundaryConditionsLoader.class.getResource(XML_SCHEMA);
    }

    /**
     * @throws IllegalStateException
     * @throws IllegalDataException
     */
    private MovsimMicroscopicBoundaryConditions unmarshallData(File xmlFile) {
        LOG.info("try to open file={}", xmlFile.getName());
        FileUnmarshaller<MovsimMicroscopicBoundaryConditions> fileUnmarshaller = new FileUnmarshaller<MovsimMicroscopicBoundaryConditions>();
        MovsimMicroscopicBoundaryConditions data = null;
        try {
            data = fileUnmarshaller.load(xmlFile, MovsimMicroscopicBoundaryConditions.class, FACTORY, getUrl());
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException(e.toString());
        }

        if (data == null) {
            LOG.error("input not valid. exit.");
            throw new IllegalDataException("xml input not valid");
        }

        return data;
    }

}
