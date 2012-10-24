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
package org.movsim.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileNameUtils {
    
    private static Logger logger = LoggerFactory.getLogger(FileNameUtils.class);
    
    /**
     * Validate simulation file name.
     * 
     * @param filename
     *            the filename
     * @return true, if successful
     */
    public static boolean validateFileName(String filename, String ending) {
        final int i = filename.lastIndexOf(ending);
        if (i < 0) {
            System.out
                    .println("Please provide simulation file with ending \""+ending+"\" as argument with option -f, exit. ");
            return false;
        }
        return true;
    }
}
