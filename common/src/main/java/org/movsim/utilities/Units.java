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

public interface Units {

    /** converts 1/s to 1/h, factor is 3600 */
    final double INVS_TO_INVH = 3600.;

    /** converts 1/h to 1/s, factor is 1/3600 */
    final double INVH_TO_INVS = 1 / INVS_TO_INVH;

    /** converts m/s to km/h, factor is 3.6 */
    final double MS_TO_KMH = 3.6;

    /** converts km/h to m/s, factor is 1/3.6 */
    final double KMH_TO_MS = 1. / MS_TO_KMH;

    /** converts from 1/m to 1/km, factor is 1000 */
    final double INVM_TO_INVKM = 1000.;

    /** converts from 1/km to 1/m, factor is 1/1000 */
    final double INVKM_TO_INVM = 1. / INVM_TO_INVKM;

    /** converts meter to kilometer. Factor is 1000. */
    final double KM_TO_M = 1000;

    /** converts kilometer to meter. Factor is 1/1000. */
    final double M_TO_KM = 1. / KM_TO_M;

    /** converts hours to seconds. Factor is 3600. */
    final double H_TO_S = 3600;

    /** converts seconds to hours. Factor is 1/3600. */
    final double S_TO_H = 1 / H_TO_S;

}
