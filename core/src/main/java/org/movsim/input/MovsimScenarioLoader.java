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
package org.movsim.input;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.movsim.MovsimCoreMain;
import org.movsim.core.autogen.MovsimScenario;
import org.movsim.xml.FileUnmarshaller;
import org.xml.sax.SAXException;

public class MovsimScenarioLoader {

    private static final Class<?> SCENARIO_FACTORY = org.movsim.core.autogen.MovsimScenario.class;

    private static final String SCENARIO_XML_SCHEMA = "/schema/MovsimScenario.xsd";

    private static final URL SCENARIO_XSD_URL = MovsimCoreMain.class.getResource(SCENARIO_XML_SCHEMA);

    public static MovsimScenario validateAndLoadScenarioInput(final File xmlFile) throws JAXBException, SAXException {
        return new FileUnmarshaller<MovsimScenario>().load(xmlFile, MovsimScenario.class, SCENARIO_FACTORY,
                SCENARIO_XSD_URL);
    }

}
