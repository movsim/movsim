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
package org.movsim.consumption.model;

final class MomentsHelper {

    private MomentsHelper() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    static double getMoment(double power, double frequency) {
        return power / (2 * Math.PI * frequency);
    }

    /**
     * power = 2*pi*f*M
     * 
     * @param moment
     * @param frequency
     * @return physical power
     */
    static double getPower(double moment, double frequency) {
        return 2 * Math.PI * frequency * moment;
    }

    /** model for loss moment */
    static double getLossPower(double frequency) {
        return getPower(getModelLossMoment(frequency), frequency);
    }

    /** heuristic parameters, assume constant coefficient for *all* gears */
    static double getModelLossMoment(double frequency) {
        final double a = 0.003;
        final double b = 0.03;
        final double c = 12;
        return a * frequency * frequency + b * frequency + c;
    }
}
