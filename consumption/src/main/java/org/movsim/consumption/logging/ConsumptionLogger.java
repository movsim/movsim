/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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
package org.movsim.consumption.logging;

import java.io.File;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.consumption.ConsumptionMain;

public class ConsumptionLogger {

    private static final String LOG4J_PROPERTIES = "log4j.properties";

    private static final String LOG4J_PATH = "/config/";

    private ConsumptionLogger() {
        throw new IllegalStateException();
    }

    public static void initializeLogger() {
        // Log Levels: DEBUG < INFO < WARN < ERROR;
        final File file = new File(LOG4J_PROPERTIES);
        if (file.exists() && file.isFile()) {
            PropertyConfigurator.configure(LOG4J_PROPERTIES);
        } else {
            final URL log4jConfig = ConsumptionMain.class.getResource(LOG4J_PATH + LOG4J_PROPERTIES);
            PropertyConfigurator.configure(log4jConfig);
        }
    }


}
