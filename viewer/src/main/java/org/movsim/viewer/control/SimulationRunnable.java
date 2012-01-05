/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */

package org.movsim.viewer.control;

import org.movsim.viewer.graphics.GraphicsConfigurationParameters;

/**
 * <p>
 * Class to encapsulate a simulation thread. Includes the necessary synchronization and callbacks to coordinate with an application UI
 * thread.
 * </p>
 * 
 * <p>
 * This class is generic and can be used for any type of simulation: it can be used with any simulation object that implements the
 * SimulationTimeStep interface.
 * </p>
 * 
 */
public class SimulationRunnable extends SimulationRun implements Runnable {
    /**
     * Callbacks from the simulation thread to the application UI thread.
     */
    public interface UpdateDrawingCallback {
        /**
         * Callback to get the application to do a draw after the vehicles have had their positions updated.
         * 
         * @param simulationTime
         *            the current logical time in the simulation
         */
        public void updateDrawing(double simulationTime);
    }

    public interface UpdateStatusPanelCallback {
        public void updateStatusPanel(double simulationTime);
    }

    public interface HandleExceptionCallback {
        /**
         * Callback to allow the application to handle an exception in the main run() loop.
         * 
         * @param e
         */
        public void handleException(Exception e);
    }

    public interface UpdateStatusCallback {
        /**
         * Callback to allow the application to make updates to its state after the vehicle positions etc have been updated, but before the
         * repaint is called.
         * 
         * @param simulationTime
         *            the current logical time in the simulation
         */
        public void updateStatus(double simulationTime);
    }

    public UpdateStatusCallback updateStatusCallback;

    private UpdateDrawingCallback updateDrawingCallback;
    private HandleExceptionCallback handleExceptionCallback;
    private UpdateStatusPanelCallback updateStatusPanelCallback;

    // not in applet
    // private class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    // public DefaultExceptionHandler() {
    // }
    //
    // @Override
    // public void uncaughtException(final Thread t, final Throwable e) {
    // if (callbacks != null) {
    // callbacks.handleException((Exception) e);
    // }
    // e.printStackTrace();
    // }
    // }
    //
    // private final DefaultExceptionHandler defaultExceptionHandler;

    // Times
    private static int sleepTime_ms;
    // private int sleepTimeUsed;
    private static final int DEFAULT_SLEEP_TIME_MS = GraphicsConfigurationParameters.DEFAULT_SLEEP_TIME_MS;
    private boolean pausedWhenRunning;
    private long lastUpdateTime_ms;
    private double actualTimewarp = 0;
    private double smoothedTimewarp = 0;
    private final double betaTimewarp = Math.exp(-1. / 50); // moving exponential average scale

    // Thread and thread synchronization.
    private Thread thread;
    private final String threadName = "SimulationRunnable-Thread";
    /**
     * Lock to synchronize the simulation thread with the UI thread.
     */
    public final Object dataLock = new Object();

    private static SimulationRunnable instance = null;

    /**
     * Constructor, sets the simulation object and default sleep time.
     * 
     * @param movsimSimulatorCore
     *            a simulation object that implements the SimulationTimeStep interface
     */
    private SimulationRunnable() {
        super();
        setSleepTime(DEFAULT_SLEEP_TIME_MS);
        // defaultExceptionHandler = new DefaultExceptionHandler();
        // Cannot call Thread.setDefaultUncaughtExceptionHandler() in applet
        // since this causes a java.security.AccessControlException
    }

    public static synchronized SimulationRunnable getInstance() {
        if (instance == null) {
            instance = new SimulationRunnable();
        }
        return instance;
    }

    /**
     * Sets the update drawing callback.
     * 
     * @param updateDrawingCallback
     */
    public void setUpdateDrawingCallback(UpdateDrawingCallback updateDrawingCallback) {
        assert this.updateDrawingCallback == null; // it's a mistake if this is set twice
        this.updateDrawingCallback = updateDrawingCallback;
    }

    /**
     * Sets the handle exception callback.
     * 
     * @param handleExceptionCallback
     */
    public void setHandleExceptionCallback(HandleExceptionCallback handleExceptionCallback) {
        assert this.handleExceptionCallback == null; // it's a mistake if this is set twice
        this.handleExceptionCallback = handleExceptionCallback;
    }

    /**
     * @param statusPanel
     */
    public void setUpdateStatusPanelCallback(UpdateStatusPanelCallback updateStatusPanelCallback) {
        this.updateStatusPanelCallback = updateStatusPanelCallback;
    }

    /**
     * Set the thread sleep time. This controls the animation speed.
     * 
     * @param sleepTime_ms
     *            sleep time in milliseconds
     */
    public void setSleepTime(int sleepTime_ms) {
        SimulationRunnable.sleepTime_ms = sleepTime_ms;
        // sleepTimeUsed = sleepTime_ms;
    }

    /**
     * Returns the sleep time.
     * 
     * @return sleep time in milliseconds
     */
    public static int sleepTime() {
        return sleepTime_ms;
    }

    public double getTimewarp() {
        return actualTimewarp;
    }

    public String getSmoothedTimewarp() {
        return String.format("%.1f", Math.min(1000, smoothedTimewarp));
    }

    /**
     * Returns true if the thread is stopped.
     * 
     * @return true if the tread is stopped
     */
    public boolean isStopped() {
        return pausedWhenRunning == false && thread == null ? true : false;
    }

    /**
     * Returns true if the thread is paused.
     * 
     * @return true if the thread is paused
     */
    public boolean isPaused() {
        return pausedWhenRunning;
    }

    /**
     * Stops the simulation thread.
     */
    public void stop() {
        pausedWhenRunning = false;
        if (thread != null) {
            final Thread waitFor = thread;
            thread = null;
            try {
                waitFor.join(5);
            } catch (final InterruptedException e) {
                // just ignore exception
            }
        }
    }

    /**
     * Starts the simulation thread.
     */
    public void start() {
        reset();
        if (thread == null) {
            thread = new Thread(this, threadName);
            // thread.setUncaughtExceptionHandler(defaultExceptionHandler);
            thread.start();
        }
    }

    /**
     * Pauses the simulation thread. Differs from stop in that the running state is saved so that it can be restored on resume.
     */
    public void pause() {
        if (thread != null) {
            stop();
            pausedWhenRunning = true;
        }
    }

    /**
     * Resumes the simulation thread. Change back to the running state we had before pause was called.
     */
    public void resume() {
        if (pausedWhenRunning) {
            pausedWhenRunning = false;
            if (thread == null) {
                thread = new Thread(this);
                thread.setName(threadName);
                // thread.setUncaughtExceptionHandler(defaultExceptionHandler);
                thread.start();
            }
        }
    }

    /**
     * <p>
     * Main thread loop. During the loop <code>update()<code> is called for the simulation object.
     * Typically the simulation object is an iterable collection of elements, each with their
     * own update method.
     * </p>
     * 
     * <p>
     * This method must be synchronized (using <code>dataLock</code>) (normally with the <code>drawForeground</code> method), so that
     * elements are not updated, added or removed from the simulation while they are being drawn.
     * </p>
     * 
     * </p>
     * 
     */
    @Override
    public void run() {
        assert updateDrawingCallback != null;
        assert simulator != null;

        lastUpdateTime_ms = System.currentTimeMillis();

        while (Thread.currentThread() == thread) {

            if (simulator.isSimulationRunFinished()) {
                stop();
                break;
            }

            try {
                // Thread.sleep(sleepTimeUsed);
                Thread.sleep(sleepTime_ms);
            } catch (final InterruptedException e) {
            }

            synchronized (dataLock) {
                try {
                    simulator.updateTimestep();

                } catch (final Exception e) {
                    if (handleExceptionCallback != null) {
                        handleExceptionCallback.handleException(e);
                    }
                    e.printStackTrace();
                }
            }

            final double simulationTime = simulationTime();
            updateStatusCallback.updateStatus(simulationTime);
            // updateDrawing calls back to the UI framework which then asynchronously redraws the view
            updateDrawingCallback.updateDrawing(simulationTime);
            updateStatusPanelCallback.updateStatusPanel(simulationTime);

            calculateTimewarp();
        }
    }

    private void calculateTimewarp() {
        final long timeAfterSim_ms = System.currentTimeMillis();
        actualTimewarp = simulator.timestep() / (0.001 * (timeAfterSim_ms - lastUpdateTime_ms));
        lastUpdateTime_ms = timeAfterSim_ms;

        smoothedTimewarp = (smoothedTimewarp == 0) ? actualTimewarp : betaTimewarp * smoothedTimewarp
                + (1 - betaTimewarp) * actualTimewarp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.web.appletroad.control.MovSimViewerRunnable#getDataLock()
     */
    public Object getDataLock() {
        return dataLock;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.web.appletroad.control.MovSimViewerRunnable#setUpdateStatusCallback(org.movsim.web.appletroad.control.SimulationRunnable
     * .UpdateStatusCallback)
     */
    public void setUpdateStatusCallback(UpdateStatusCallback updateStatusCallback) {
        this.updateStatusCallback = updateStatusCallback;
    }
}
