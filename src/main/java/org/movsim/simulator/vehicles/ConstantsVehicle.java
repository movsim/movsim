/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator.vehicles;

/**
 * Default vehicle parameters.
 */
public interface ConstantsVehicle {
    /**
     * Default length of cars (meters).
     */
    static final double LENGTH_CAR_M = 5.0;
    /**
     * Default width of cars (meters).
     */
    static final double WIDTH_CAR_M = 3.0;
    /**
     * Default length of trucks (meters).
     */
    static final double LENGTH_TRUCK_M = 12.0;
    /**
     * Default width of trucks (meters).
     */
    static final double WIDTH_TRUCK_M = 3.5;

    /**
     * Default desired velocity for cars.
     */
    static final double V0_INIT_CAR_KMH = 120.0;
    /**
     * Default desired velocity for trucks.
     */
    static final double V0_INIT_TRUCK_KMH = 80.0;
    /**
     * Default maximum normal acceleration for cars.
     */
    static final double A_INIT_CAR_MS2 = 0.8; // m/s^2
    /**
     * Default maximum normal acceleration for trucks.
     */
    static final double A_INIT_TRUCK_MS2 = 0.4; // m/s^2
    /**
     * Default maximum normal braking for cars.
     */
    static final double B_INIT_CAR_MS2 = 1.25; // m/s^2
    /**
     * Default maximum normal braking for trucks.
     */
    static final double B_INIT_TRUCK_MS2 = 0.8; // m/s^2
    /**
     * Default safe time headway for cars (time interval to vehicle in front, seconds).
     */
    static final double T_INIT_CAR_S = 1.2; // seconds
    /**
     * Default safe time headway for trucks (time interval to vehicle in front, seconds).
     */
    static final double T_INIT_TRUCK_S = 1.7; // seconds
    /**
     * Default bumper to bumper separation in stationary traffic.
     */
    static final double S0_INIT_M = 2.0;
    /**
     * Default IDM gap parameter.
     */
    static final double S1_INIT_M = 10.0;
    //  IDM parameters used in "Microscopic Simulation of Congested Traffic"
    //   (Martin Treiber, Ansgar Hennecke, and Dirk Helbing)
    //          v0        T     a          b        s0  s1   l
    //  Cars   120km/h  1.2s  0.8m/s^2   1.25m/s^2  1m  10m  5m
    //  Trucks  80km/h  1.7s  0.4m/s^2   0.8 m/s^2  1m  10m  8m

    // IDM parameters used in "Congested Traffic States in Empirical Observations and Microscopic Simulations"
    // (Martin Treiber, Ansgar Hennecke, and Dirk Helbing, May 2007)
    //          v0        T     a          b        s0  s1   l
    //  Cars   120km/h  1.6s  0.73m/s^2  1.67m/s^2  2m  0m   5m

    // IDM parameters from "Calibrating Car-Following Models using Trajectory Data: Methodological Study"
    // Arne Kesting, Martin Treiber
    // DataSet 2 Fmix parameters
    //          v0        T     a           b         s0    s1   l
    //  Cars    --      1.43s 0.977m/s^2  0.994m/s^2  2.8m  --   -
    //

    // 33.33.. m/s = 120 km/h
    // 33.0 m/s = 118 km/h
    // 22.22.. m/s = 80 km/h
    // 22.2 m/s = 79.9 km/h
    // IDM parameters: v0, a, b, T, s0, s1
    /**
     * Default IDM for cars.
     */
    static final double[] IDM_CAR     = new double[] { V0_INIT_CAR_KMH/3.6, A_INIT_CAR_MS2, B_INIT_CAR_MS2, T_INIT_CAR_S, S0_INIT_M, S1_INIT_M };
    /**
     * Default IDM for trucks.
     */
    static final double[] IDM_TRUCK   = new double[] { V0_INIT_TRUCK_KMH/3.6,  A_INIT_TRUCK_MS2, B_INIT_TRUCK_MS2, T_INIT_TRUCK_S, S0_INIT_M, S1_INIT_M };
    /**
     * Default ACC coolness parameter.
     */
    static final double C_INIT = 0.99;
    //  ACC parameters used in "Enhanced Intelligent Driver Model to Access the Impact of Driving Strategies on Traffic Capacity"
    //   (Arne Kesting, Martin Treiber, and Dirk Helbing)
    //          v0        T     a          b        s0  s1   l  c
    //  Cars   120km/h  1.5s  1.4m/s^2   2.0m/s^2   2m  --   -  0.99
    //  Trucks  85km/h  2.0s  0.7m/s^2   2.0m/s^2   4m  --   -  0.99
    /**
     * Default ACC for cars.
     */
    static final double[] ACC_CAR     = new double[] { V0_INIT_CAR_KMH/3.6, A_INIT_CAR_MS2, B_INIT_CAR_MS2, T_INIT_CAR_S, S0_INIT_M, S1_INIT_M, C_INIT };
    /**
     * Default ACC for trucks.
     */
    static final double[] ACC_TRUCK   = new double[] { V0_INIT_TRUCK_KMH/3.6,  A_INIT_TRUCK_MS2, B_INIT_TRUCK_MS2, T_INIT_TRUCK_S, S0_INIT_M, S1_INIT_M, C_INIT };
}
