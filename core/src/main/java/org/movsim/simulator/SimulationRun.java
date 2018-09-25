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
package org.movsim.simulator;

import java.util.ArrayList;
import java.util.List;

import org.movsim.shutdown.ShutdownHooks;

import com.google.common.base.Preconditions;

public class SimulationRun {

    public interface CompletionCallback {
        /**
         * Callback to inform the application that the simulation has run to
         * completion.
         *
         * @param simulationTime
         */
        void simulationComplete(double simulationTime);
    }

    public interface UpdateStatusCallback {
        /**
         * Callback to allow the application to make updates to its state after
         * the vehicle positions etc have been updated, but before the repaint
         * is called.
         *
         * @param simulationTime the current logical time in the simulation
         */
        void updateStatus(double simulationTime);
    }

    protected double dt; // timestep, seconds

    protected double duration; // duration, seconds

    protected double simulationTime; // Simulation time, seconds (reset to 0.0
    // in each run)

    protected long iterationCount;

    protected long totalSimulationTime;

    protected final List<UpdateStatusCallback> updateStatusCallbacks = new ArrayList<>();

    protected CompletionCallback completionCallback;

    // simulation is an object that implements the SimulationTimeStep interface.
    protected final SimulationTimeStep simulation;

    /**
     * Constructor, sets the simulation object.
     *
     * @param simulation a simulation object that implements the SimulationTimeStep
     *                   interface
     */
    public SimulationRun(SimulationTimeStep simulation) {
        this.simulation = Preconditions.checkNotNull(simulation);
        initShutdownHook();
    }

    /**
     * Returns the simulation timestep object.
     *
     * @return the simulation timestep object
     */
    public final SimulationTimeStep simulation() {
        return simulation;
    }

    /**
     * Sets the simulation timestep.
     *
     * @param dt simulation timestep in seconds
     */
    public final void setTimeStep(double dt) {
        this.dt = dt;
    }

    /**
     * Returns the simulation timestep
     *
     * @return simulation timestep in seconds
     */
    public final double timeStep() {
        return dt;
    }

    /**
     * Sets the simulation duration.
     *
     * @param duration simulation duration in seconds
     */
    public final void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Returns the simulation duration.
     *
     * @return simulation duration in seconds
     */
    public final double duration() {
        return duration;
    }

    /**
     * Returns the number of iterations executed.
     *
     * @return number of iterations
     */
    public final long iterationCount() {
        return iterationCount;
    }

    /**
     * <p>
     * Returns the time for which the simulation has been running.
     * </p>
     *
     * <p>
     * This is the logical time in the simulation (that is the sum of the
     * deltaTs), not the amount of real time that has been required to do the
     * simulation calculations.
     * </p>
     *
     * @return the simulation time
     */
    public final double simulationTime() {
        return simulationTime;
    }

    /**
     * Returns the total execution time of the simulation. Useful for order of
     * magnitude benchmarking.
     *
     * @return total execution time of the simulation
     */
    public final long totalSimulationTime() {
        return totalSimulationTime;
    }

    /**
     * Resets the simulation instrumentation data.
     */
    public void reset() {
        simulationTime = 0.0;
        iterationCount = 0;
        totalSimulationTime = 0;
    }

    /**
     * Adds a update status callback.
     *
     * @param updateStatusCallback
     */
    public void addUpdateStatusCallback(UpdateStatusCallback updateStatusCallback) {
        updateStatusCallbacks.add(Preconditions.checkNotNull(updateStatusCallback));
    }

    /**
     * Sets the completion callback.
     *
     * @param completionCallback
     */
    public final void setCompletionCallback(CompletionCallback completionCallback) {
        Preconditions.checkState(this.completionCallback == null, "it's a mistake if this is set twice");
        this.completionCallback = completionCallback;
    }

    /**
     * Runs the simulation to completion and then calls the completion callback.
     */
    public void runToCompletion() {
        assert dt != 0.0;
        assert duration != 0.0;
        assert duration > 0.0;
        reset();
        final long timeBeforeSim_ms = System.currentTimeMillis();
        final double timeLimit = duration + dt / 2.0; // allow for rounding errors
        while (simulationTime <= timeLimit) {
            // perform the timeStep for the road network
            simulation.timeStep(dt, simulationTime, iterationCount);
            for (final UpdateStatusCallback updateStatusCallback : updateStatusCallbacks) {
                updateStatusCallback.updateStatus(simulationTime);
            }
            simulationTime += dt;
            ++iterationCount;

        }
        totalSimulationTime = System.currentTimeMillis() - timeBeforeSim_ms;
        if (completionCallback != null) {
            completionCallback.simulationComplete(simulationTime);
        }
        ShutdownHooks.INSTANCE.onShutDown();
    }

    private static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Unexpected end of simulator: perform ShutdownHooks");
                ShutdownHooks.INSTANCE.onShutDown();
            }
        });
    }
}
