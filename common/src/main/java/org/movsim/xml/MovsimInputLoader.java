/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.jdom.IllegalDataException;
import org.movsim.autogen.Movsim;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class MovsimInputLoader {

    /** The Constant logger. */
    private final static Logger LOG = LoggerFactory.getLogger(MovsimInputLoader.class);

    private static final Class<?> FACTORY = Movsim.class;

    private static final String SCENARIO_XML_SCHEMA = "/schema/MovsimScenario.xsd";

    private static URL getUrl() {
        return MovsimInputLoader.class.getResource(SCENARIO_XML_SCHEMA);
    }

    public static Movsim unmarshall(File xmlFile) {
        MovsimInputLoader loader = new MovsimInputLoader();
        return loader.unmarshallData(xmlFile);
    }
    
    /**
     * @throws IllegalStateException
     * @throws IllegalDataException
     */
    private Movsim unmarshallData(File xmlFile) {
        LOG.info("try to open file={}", xmlFile.getName());
        FileUnmarshaller<Movsim> fileUnmarshaller = new FileUnmarshaller<Movsim>();
        Movsim data = null;
        try {
            data = fileUnmarshaller.load(xmlFile, Movsim.class, FACTORY, getUrl());
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException(e.toString());
        }

        if (data == null) {
            LOG.error("input not valid. exit.");
            throw new IllegalDataException("xml input not valid");
        }

        return data;
    }
    
    /**
     * writes the movsim xsd to the current working directory.
     * 
     * @throws IOException
     */
    public static void writeXsdToFile() throws IOException {
        String filename = new File(SCENARIO_XML_SCHEMA).getName();
        FileUtils.writeStreamToFile(filename, getUrl().openStream());
        LOG.info("wrote file={}", filename);
    }

}
