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
package org.movsim.consumption;

import java.util.Locale;

import org.movsim.consumption.input.ConsumptionCommandLine;
import org.movsim.consumption.input.ConsumptionMetadata;
import org.movsim.consumption.input.xml.ConsumptionInputData;
import org.movsim.consumption.input.xml.XmlReader;
import org.movsim.consumption.logging.ConsumptionLogger;


public class ConsumptionMain {

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);
        
        System.out.println("Movsim Consumption Model. (c) Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden");
        
        ConsumptionLogger.initializeLogger();
        
        ConsumptionCommandLine.parse(ConsumptionMetadata.getInstance(), args);

        ConsumptionInputData inputData = new ConsumptionInputData();
        XmlReader.parse(ConsumptionMetadata.getInstance(), inputData);
        
        
        
    }
    
}
