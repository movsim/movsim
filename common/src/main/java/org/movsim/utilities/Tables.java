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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Tables. Various static table-related methods like interpolation, extrapolation
 */
public class Tables {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Tables.class);

    private static final double TINY_VALUE = 1.e-10;

    private Tables() {
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
            logger.error("intp: index i = " + i + " (ir=" + ir + ") out of range\n");
            System.exit(-1);
        }
        return intp_value;
    }

    public static double intpextp(double[] x_vals, double[] y_vals, double x) {
        final int nx = x_vals.length;
        final int ny = y_vals.length;
        final int n = Math.min(nx, ny);
        if (nx != ny) {
            logger.debug("Warning: arrays of not equal length = {}, {} ", nx, ny);
        }
        if (nx == 0 || ny == 0) {
            // logger.debug("cannot interpolate from arrays with zero length(s) = {}, {}. return 0 ",
            // nx, ny);
            return 0;
        }

        int i = 0;
        double intp_value = 0;

        while ((x_vals[i] <= x) && (i < n - 1)) {
            i++;
        }

        if (i == 0) { // extrapolation to "left" side
            intp_value = y_vals[0];
        } else if ((i == n - 1) && (x > x_vals[i])) {
            // extrapol. to "right" side
            intp_value = y_vals[n - 1];
        } else if (Math.abs(x_vals[i] - x_vals[i - 1]) < TINY_VALUE) { // treatment
            // of jumps
            intp_value = y_vals[i];
        } else {
            // linear interpolation
            intp_value = y_vals[i - 1] + (y_vals[i] - y_vals[i - 1]) * (x - x_vals[i - 1])
                    / (x_vals[i] - x_vals[i - 1]);
        }
        // logger.debug(" return = {}", intp_value);
        return (intp_value);
    }

    // Version using only the index range imin ... imax and extrapolating
    // constant values otherwise; reverse=true means that the array x
    // has x values in decreasing order. NOT TIME OPTIMIZED
    public static double intpextp(double[] x, double[] y, double pos, boolean reverse) {
        return intpextp(x, y, pos, 0, x.length - 1, reverse);
    }

    public static double intpextp(double[] x, double[] y, double pos, int imin, int imax, boolean reverse) {

        final double tinyValue = 0.000001;
        double intp_value;
        int i = imin;
        if (reverse) {
            while ((x[i] >= pos) && (i < imax)) {
                i++;
            }
        } else {
            while ((x[i] <= pos) && (i < imax)) {
                i++;
            }
        }
        if (i == imin) {
            intp_value = y[imin]; // left extrapolation
        } else if (i == imax) {
            intp_value = y[imax]; // right extrapolation
        } else if (Math.abs(x[i] - x[i - 1]) < tinyValue) {
            intp_value = y[i]; // same x values
        } else {
            intp_value = y[i - 1] + (y[i] - y[i - 1]) * (pos - x[i - 1]) / (x[i] - x[i - 1]); // interpolation
        }
        return (intp_value);
    }

    // TODO check implementation !!!
    // extrapolate left-hand side values for use in speedlimit
    public static double stepExtrapolation(double[] x_vals, double[] y_vals, double x) {
        final int nx = x_vals.length;
        final int ny = y_vals.length;
        final int n = Math.min(nx, ny);
        if (nx != ny) {
            logger.debug("Warning: arrays of not equal length = {}, {} ", nx, ny);
        }
        if (nx == 0 || ny == 0) {
            logger.debug("cannot interpolate from arrays with zero length(s) = {}, {} ", nx, ny);
            return 0;
        }
        int i = 0;
        double intp_value;

        while ((x_vals[i] <= x) && (i < n - 1)) {
            i++;
        }

        if (i == 0) { // extrapolation to "left" side
            intp_value = y_vals[0];
        } else if ((i == n - 1) && (x > x_vals[i])) {
            // extrapol. to "right" side
            intp_value = y_vals[n - 1];
        } else {
            // simple constant extrapolation from "left" side
            intp_value = y_vals[i - 1];
        }
        // else { // linear interpolation
        // intp_value = y_vals[i - 1] + (y_vals[i] - y_vals[i - 1]) * (x -
        // x_vals[i - 1])
        // / (x_vals[i] - x_vals[i - 1]);
        // }

        return (intp_value);

    }

}
