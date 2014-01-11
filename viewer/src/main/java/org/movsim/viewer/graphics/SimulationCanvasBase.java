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

package org.movsim.viewer.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import org.movsim.simulator.SimulationRunnable;

/**
 * <p>
 * Simulation canvas abstract base class. Contains the SimulationRunnable which runs the simulation in a separate thread.
 * </p>
 * 
 * <p>
 * This base class handles:
 * <ul>
 * <li>Synchronization between the simulation and UI threads.</li>
 * <li>Starting, stopping, pausing and resuming of the simulation.</li>
 * <li>Zooming and panning.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * That is the base class handles the "policy free" aspects of the view. Other aspects of the view (colors, setting up the simulation,
 * drawing the foreground and background) are deferred to a subclass.
 * </p>
 * 
 * 
 */
public abstract class SimulationCanvasBase extends Canvas {

    private static final long serialVersionUID = 3351170249194920665L;

    private static final double FORCE_REPAINT_BACKGROUND_INTERVAL = 60.0; // seconds;

    protected final SimulationRunnable simulationRunnable;
    protected long totalAnimationTime;

    // drawing support
    private Image backgroundBuffer;
    private Image foregroundBuffer;
    private int bufferHeight;
    private int bufferWidth;
    protected boolean backgroundChanged;
    // default background color
    protected Color backgroundColor;
    // scale factor pixels/m, smaller value means a smaller looking view
    double scale;
    int xOffset = 0;
    int yOffset = 0;

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    protected AffineTransform transform = new AffineTransform();

    protected boolean zoomingAllowed = true;

    protected double measuredTime = 0;

    /**
     * Abstract function to allow the view to draw the simulation background, normally this is everything that does not move.
     * 
     * @param g
     */
    protected abstract void drawBackground(Graphics2D g);

    /**
     * Abstract function to allow the view to draw the simulation foreground, normally this is everything that moves.
     */
    protected abstract void drawForeground(Graphics2D g);

    /**
     * Constructor, set the simulation.
     * 
     * @param simulationRunnable
     */
    public SimulationCanvasBase(SimulationRunnable simulationRunnable) {
        assert simulationRunnable != null;
        this.simulationRunnable = simulationRunnable;

    }

    public void reset() {
        resetScaleAndOffset();
        simulationRunnable.reset();
    }

    public abstract void resetScaleAndOffset();

    protected void setTransform() {
        transform.setToIdentity();
        transform.scale(scale, scale);
        transform.translate(xOffset, yOffset);
    }

    @Override
    public void setSize(int newWidth, int newHeight) {
        if (!isDisplayable()) {
            return;
        }
        super.setSize(Math.max(newWidth, 10), Math.max(newHeight, 10));
        final int width = getWidth();
        final int height = getHeight();
        setTransform();
        if (backgroundBuffer == null || width > bufferWidth || height > bufferHeight) {
            backgroundBuffer = createImage(width, height);
            assert backgroundBuffer != null; // assert preconditions for createImage have been met
            foregroundBuffer = createImage(width, height);
            assert foregroundBuffer != null;
            bufferWidth = width;
            bufferHeight = height;
        }
    }

    public void setScale(double scale) {
        final int width = getWidth();
        final int height = getHeight();
        xOffset -= 0.5 * width * (1.0 / this.scale - 1.0 / scale);
        yOffset -= 0.5 * height * (1.0 / this.scale - 1.0 / scale);
        this.scale = scale;
        setTransform();
    }

    public double scale() {
        return scale;
    }

    @Deprecated
    protected void setScales() {
        final int width = Math.max(getSize().width, 10);
        final int height = Math.max(getSize().height, 10);

        if (backgroundBuffer == null || width != bufferWidth || height != bufferHeight) {
            backgroundBuffer = createImage(width, height);
            foregroundBuffer = createImage(width, height);
            bufferWidth = width;
            bufferHeight = height;
        }
    }

    protected void clearOffsets() {
        xOffset = 0;
        yOffset = 0;
        setTransform();
    }

    public void forceRepaintBackground() {
        backgroundChanged = true;
        repaint();
    }

    /**
     * Application-triggered painting. <code>update()</code> is asynchronously triggered by a previous call to <code>repaint()</code>.
     * 
     * @param g
     */
    @Override
    public void update(Graphics g) {
        final Graphics2D bufferGraphics = (Graphics2D) backgroundBuffer.getGraphics();
        if (backgroundChanged) {
            // clear the background before affine transforms
            clearBackground(bufferGraphics);
        }
        bufferGraphics.setTransform(transform);

        if (backgroundChanged) {
            // if the background has been changed, then its content needs to be repainted
            drawBackground(bufferGraphics);
            backgroundChanged = false;
        }

        // update background (for outflow) every e.g. 60 seconds of simulation
        measuredTime += simulationRunnable.timeStep();
        if (measuredTime > FORCE_REPAINT_BACKGROUND_INTERVAL) {
            forceRepaintBackground();
            measuredTime = 0;
        }

        drawForegroundAndBlit(g);
    }

    /**
     * System-triggered painting. <code>paint()</code> is called if any part of the window becomes invalid; for example it has been
     * obscured, or it has been resized.
     * 
     * @param g
     */
    @Override
    public void paint(Graphics g) {

        if (backgroundBuffer == null)
            return;
        final Graphics2D backgroundGraphics = (Graphics2D) backgroundBuffer.getGraphics();
        clearBackground(backgroundGraphics); // clear the background before transforms
        backgroundGraphics.setTransform(transform);
        drawBackground(backgroundGraphics); // draw the background to the buffer
        drawForegroundAndBlit(g);

    }

    /**
     * Draw the foreground and blit it to the screen.
     * 
     * @param g
     */
    private void drawForegroundAndBlit(Graphics g) {
        // copy the background buffer to the foregroundBuffer buffer
        final Graphics2D foregroundGraphics = (Graphics2D) foregroundBuffer.getGraphics();
        foregroundGraphics.drawImage(backgroundBuffer, 0, 0, null);
        // draw the foreground to the foreground buffer
        foregroundGraphics.setTransform(transform);
        drawForeground(foregroundGraphics);
        // copy the foreground buffer to the screen
        g.drawImage(foregroundBuffer, 0, 0, null);
    }

    /**
     * Clear the view background. Default implementation just fills the background with the background color. May be overridden by a
     * subclass if required.
     * 
     * @param g
     */
    protected void clearBackground(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ============================================================================================
    // SimulationRunnable related functions
    // ============================================================================================

    /**
     * Set the thread sleep time. This controls the animation speed.
     * 
     * @param sleepTime_ms
     *            sleep time in milliseconds
     */
    public final void setSleepTime(int sleepTime_ms) {
        simulationRunnable.setSleepTime(sleepTime_ms);
    }

    /**
     * Returns the thread sleep time
     * 
     * @return the sleep time in milliseconds
     */
    public final int sleepTime() {
        return simulationRunnable.sleepTime();
    }

    /**
     * <p>
     * Returns the time for which the simulation has been running.
     * </p>
     * 
     * <p>
     * This is the logical time in the simulation (that is the sum of the timesteps), not the amount of real time that has been required to
     * do the simulation calculations.
     * </p>
     * 
     * @return the simulation time
     */
    public final double simulationTime() {
        return simulationRunnable.simulationTime();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Function to be overridden for subclass state change handling.
     */
    void stateChanged() {
    }

    /**
     * Start the simulation thread. Called from <code>start()</code> method of main applet class.
     */
    public void start() {
        totalAnimationTime = 0;
        simulationRunnable.start();
        stateChanged();
    }

    /**
     * Returns true if the animation is stopped.
     * 
     * @return true if the animation is stopped
     */
    public final boolean isStopped() {
        return simulationRunnable.isStopped();
    }

    /**
     * Stop the simulation thread.
     */
    public final void stop() {
        simulationRunnable.stop();
        stateChanged();
    }

    /**
     * Returns true if the simulation is paused.
     * 
     * @return true if the simulation is paused
     */
    public final boolean isPaused() {
        return simulationRunnable.isPaused();
    }

    /**
     * Pause the simulation.
     */
    public void pause() {
        simulationRunnable.pause();
        stateChanged();
    }

    /**
     * Resume the simulation after a pause.
     */
    public final void resume() {
        simulationRunnable.resume();
        stateChanged();
    }
}
