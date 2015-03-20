/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Tables. Various static table-related methods like interpolation, extrapolation
 */
public final class Tables {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Tables.class);

    private Tables() {
       throw new IllegalStateException("do not instanciate");
    }

    public static double intp(double[] tab, double x, double xmin, double xmax) {
        return intp(tab, tab.length, x, xmin, xmax);
    }

    public static double intp(double[] tab, int n, double x, double xmin, double xmax) {
        double intp_value = tab[0];
        final double ir = n * (x - xmin) / (xmax - xmin);
        final int i = (int) ir;
        final double rest = ir - i;
        if ((i >= 0) && (i < n - 1)) {
            intp_value = (1 - rest) * tab[i] + rest * tab[i + 1];
        } else if (i == n - 1) {
            intp_value = tab[n - 1];
        } else if (i == n) {
            intp_value = xmax;
        } else {
            LOG.error("intp: index i = " + i + " (ir=" + ir + ") out of range\n");
            System.exit(-1);
        }
        return intp_value;
    }

}
