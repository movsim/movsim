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

package org.movsim.input.file.opendrive;

import org.movsim.input.file.xml.XMLReaderBase;
import org.movsim.simulator.roadnetwork.RoadNetwork;

/**
 * Reads an OpenDRIVE format file and uses it to create a road network, see: http://www.opendrive.org/docs/OpenDRIVEFormatSpecRev1.3D.pdf
 */
public class OpenDriveReader extends XMLReaderBase {
    /**
     * Reads an OpenDrive format file, creating a road network.
     * 
     * @param roadNetwork
     * @param filename
     * @return true if the road network file exists and was successfully parsed, false otherwise.
     */
    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, String filename) {
        final OpenDriveHandler handler = new OpenDriveHandler(roadNetwork);
        return parse(filename, handler);
    }
}
