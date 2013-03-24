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

import java.util.List;

public class ExponentialMovingAverage {

    private ExponentialMovingAverage() {
        throw new IllegalStateException();
    }

    public static double calcEMA(double time, List<XYDataPoint> timeSeries, double tau) {
        if (timeSeries.isEmpty()) {
            return 0;
        }
        double norm = 0;
        double result = 0;
        for (final XYDataPoint dp : timeSeries) {
            final double phi = weight(time, dp.getX(), tau);
            norm += phi;
            result += phi * dp.getY();
        }
        return result / norm;
    }

    // TODO javadoc
    private static double weight(double t1, double t2, double tau) {
        return Math.exp(-Math.abs((t1 - t2) / tau));
    }

    public static double calc(double xNew, double xEMA, double beta) {
        return (1 - beta) * xNew + beta * xEMA;
    }

}
