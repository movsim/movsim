/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.movsim.autogen.Movsim;
import org.xml.sax.SAXException;

// TODO rename
public final class MovsimInputLoader {

    private static final Class<?> SCENARIO_FACTORY = Movsim.class;

    private static final String SCENARIO_XML_SCHEMA = "/schema/MovsimScenario.xsd";

    private static final URL SCENARIO_XSD_URL = MovsimInputLoader.class.getResource(SCENARIO_XML_SCHEMA);

    private MovsimInputLoader() {
    }

    public static Movsim validateAndLoadScenarioInput(final File xmlFile) throws JAXBException, SAXException {
        return new FileUnmarshaller<Movsim>().load(xmlFile, Movsim.class, SCENARIO_FACTORY,
                SCENARIO_XSD_URL);
    }

    public static Movsim getInputData(File xmlFile) {
        // testwise jaxb unmarshalling
        Movsim inputData = null;
        try {
            System.out.println("try to open file = " + xmlFile.getName());
            inputData = MovsimInputLoader.validateAndLoadScenarioInput(xmlFile);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (SAXException e) {
            throw new IllegalArgumentException(e.toString());
        }
        if (inputData == null) {
            System.out.println("input not valid. exit.");
            System.exit(-1);
        }
        return inputData;
    }
}
