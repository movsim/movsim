package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.jdom.IllegalDataException;
import org.movsim.scenario.initial.autogen.MovsimInitialConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class MovsimInitialConditionsLoader {
    private static final Logger LOG = LoggerFactory.getLogger(MovsimInitialConditionsLoader.class);

    private static final Class<?> FACTORY = MovsimInitialConditions.class;
    private static final String XML_SCHEMA = "/schema/MovsimInitialConditions.xsd";

    public static MovsimInitialConditions unmarshall(File file) {
        MovsimInitialConditionsLoader loader = new MovsimInitialConditionsLoader();
        return loader.unmarshallData(file);
    }

    private URL getUrl() {
        return MovsimInitialConditionsLoader.class.getResource(XML_SCHEMA);
    }

    /**
     * @throws IllegalStateException
     * @throws IllegalDataException
     */
    private MovsimInitialConditions unmarshallData(File xmlFile) {
        LOG.info("try to open file={}", xmlFile.getName());
        FileUnmarshaller<MovsimInitialConditions> fileUnmarshaller = new FileUnmarshaller<MovsimInitialConditions>();
        MovsimInitialConditions data = null;
        try {
            data = fileUnmarshaller.load(xmlFile, MovsimInitialConditions.class, FACTORY, getUrl());
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
