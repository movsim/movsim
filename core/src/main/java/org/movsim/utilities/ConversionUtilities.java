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
package org.movsim.utilities;

public interface ConversionUtilities {

    /** convert 1/s to 1/h */
    final double INVS_TO_INVH = 3600.;

    /** convert m/s to km/h */
    final double MS_TO_KMH = 3.6;

    /** convert km/h to m/s */
    final double KMH_TO_MS = 1. / MS_TO_KMH;

    /** convert (e.g. traffic densities) from 1/m to 1/km */
    final double INVM_TO_INVKM = 1000.;
    
    /** convert (e.g. traffic densities) from 1/km to 1/m */
    final double INVKM_TO_INVM = 1./INVM_TO_INVKM;

}
