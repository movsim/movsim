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

import java.util.Random;

/**
 * The Class MyRandom.
 */
public class MyRandom {

    private static Random rand = new Random();

    private MyRandom() {
        // enforce singleton property with private constructor.
    }

    public static void initializeWithSeed(long randomSeed) {
        rand = new Random(randomSeed);
    }

    public static boolean isInitialized() {
        return rand != null;
    }

    /**
     * Next int.
     * 
     * @return the int
     */
    public static int nextInt() {
        return rand.nextInt();
    }

    public static int nextInt(int n) {
        return rand.nextInt(n);
    }

    /**
     * Next double.
     * 
     * @return the double
     */
    public static double nextDouble() {
        return rand.nextDouble();
    }

    /**
     * returns a realization of a uniformly distributed random variable in [-1, 1]
     * 
     * @return a uniformly distributed realization in [-1, 1]
     */
    public static double getUniformDistribution() {
        return 2 * MyRandom.nextDouble() - 1;
    }

    public static double getUniformlyDistributedRandomizedFactor(double randomizationStrength) {
        return 1 + randomizationStrength * getUniformDistribution();
    }

    public static double getGaussiansDistributedRandomizedFactor(double sigma, double nSigmaCutoff) {
        return 1 + Math.max(-nSigmaCutoff * sigma, Math.min(nSigmaCutoff, sigma * rand.nextGaussian()));
    }

}
