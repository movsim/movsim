/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadMapping;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Slope;
import org.movsim.simulator.roadnetwork.SpeedLimit;
import org.movsim.simulator.roadnetwork.TrafficLight;
import org.movsim.simulator.roadnetwork.TrafficSink;
import org.movsim.simulator.roadnetwork.TrafficSource;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.Vehicle.Type;
import org.movsim.utilities.ConversionUtilities;
import org.movsim.viewer.roadmapping.PaintRoadMapping;
import org.movsim.viewer.util.SwingHelper;

/**
 * <p>
 * TrafficCanvas class.
 * </p>
 * 
 * <p>
 * Handles:
 * <ul>
 * <li>Drawing the road network and the vehicles upon it.</li>
 * <li>Standard mouse events.</li>
 * <li>Mouse-over and clicks on vehicles (when simulation paused).</li>
 * <li>Key events.</li>
 * </ul>
 * </p>
 * <p>
 * The vehicles are redrawn in their new positions in the drawForeground() method, which is indirectly invoked from repaint(). The
 * drawForeground() method has a synchronization lock so that vehicles are not updated or removed while they are being drawn.
 * </p>
 * <p>
 * Actual road networks and traffic scenarios should be set up in a subclass.
 * </p>
 * 
 */
public class TrafficCanvas extends SimulationCanvasBase implements SimulationRunnable.UpdateDrawingCallback,
        SimulationRunnable.HandleExceptionCallback, SimulationRunnable.UpdateStatusCallback {

    static final long serialVersionUID = 1L;
    protected static final boolean DEBUG = false;

    protected final Simulator simulator;
    protected final RoadNetwork roadNetwork;

    /**
     * Callbacks from this TrafficCanvas to the application UI.
     * 
     */
    public interface StatusControlCallbacks {
        /**
         * Callback to get the UI to display a status message.
         * 
         * @param message
         *            the status message
         */
        public void showStatusMessage(String message);

        public void stateChanged();
    }

    protected StatusControlCallbacks statusControlCallbacks;

    protected static final int INITIAL_SLEEP_TIME = GraphicsConfigurationParameters.INITIAL_SLEEP_TIME;

    // pre-allocate vehicle drawing path
    private final GeneralPath vehiclePath = new GeneralPath();

    // pre-allocate clipping path for road mappings
    private final GeneralPath clipPath = new GeneralPath(Path2D.WIND_EVEN_ODD);

    protected double initialScale = GraphicsConfigurationParameters.INITIAL_SCALE;

    // colors
    protected Color roadEdgeColor = Color.black;
    protected Color roadLineColor = Color.white;
    protected Color roadInhomogeneityColor = Color.darkGray;
    protected Color sourceColor = Color.white;
    protected Color sinkColor = Color.black;
    protected Color sourceJunctionColor = Color.cyan;
    protected Color sinkJunctionColor = Color.blue;

    private double vmaxForColorSpectrum = GraphicsConfigurationParameters.VmaxForColorSpectrum;

    protected boolean drawRoadId = GraphicsConfigurationParameters.DRAW_ROADID;
    protected boolean drawSouces = GraphicsConfigurationParameters.DRAWSOURCES;
    protected boolean drawSinks = GraphicsConfigurationParameters.DRAWSINKS;
    protected boolean drawSpeedLimits = GraphicsConfigurationParameters.DRAWSPEEDLIMITS;
    protected boolean drawSlopes = GraphicsConfigurationParameters.SLOPES;

    // brake light handling
    protected Color brakeLightColor = Color.RED;

    /**
     * Vehicle color support only the first four are used by the button. commandCyclevehicleColors()
     */
    protected enum VehicleColorMode {
        VELOCITY_COLOR, LANE_CHANGE, ACCELERATION_COLOR, VEHICLE_COLOR, VEHICLE_LABEL_COLOR, HIGHLIGHT_VEHICLE, EXIT_COLOR
    }

    /** Color mode displayed on startup */
    protected VehicleColorMode vehicleColorMode = VehicleColorMode.VELOCITY_COLOR;

    protected VehicleColorMode vehicleColorModeSave;

    double[] velocities;

    private Color[] velocityColors;
    private Color[] accelerationColors;

    private final double[] accelerations = new double[] { -7.5, -0.1, 0.2 };

    /** vehicle mouse-over support */
    String popupString;

    String popupStringExitEndRoad;

    protected Vehicle vehiclePopup;

    protected long lastVehicleViewed = -1;

    protected long vehicleToHighlightId = -1;

    protected VehicleTipWindow vehicleTipWindow;

    public TrafficCanvas(SimulationRunnable simulationRunnable, Simulator simulator) {
        super(simulationRunnable);
        this.simulator = simulator;
        this.roadNetwork = simulator.getRoadNetwork();
        simulationRunnable.setUpdateDrawingCallback(this);
        simulationRunnable.setHandleExceptionCallback(this);
        setSleepTime(INITIAL_SLEEP_TIME);

        final TrafficCanvasMouseListener mouseListener = new TrafficCanvasMouseListener(this, roadNetwork);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addKeyListener(new TrafficCanvasKeyListener(this));
    }

    @Override
    protected void reset() {
        super.reset();
        simulator.reset();
    }

    /**
     * Sets the status callback functions.
     * 
     * @param statusControlCallbacks
     */
    public void setStatusControlCallbacks(StatusControlCallbacks statusCallbacks) {
        this.statusControlCallbacks = statusCallbacks;
    }

    /**
     * Sets the (locale dependent) message strings.
     * 
     * @param popupString
     *            popup window format string for vehicle that leaves road segment at a specific exit
     * @param popupStringExitEndRoad
     *            popup window format string for vehicle that leaves road segment at end
     */
    public void setMessageStrings(String popupString, String popupStringExitEndRoad) {
        this.popupString = popupString;
        this.popupStringExitEndRoad = popupStringExitEndRoad;
    }

    void setVelocityColors() {
        accelerationColors = new Color[] { Color.WHITE, Color.RED, Color.BLACK, Color.GREEN };
        assert velocities.length == velocityColors.length - 1;
        assert accelerations.length == accelerationColors.length - 1;
    }

    public double getVmaxForColorSpectrum() {
        return vmaxForColorSpectrum;
    }

    public void setVmaxForColorSpectrum(double vmaxForColorSpectrum) {
        this.vmaxForColorSpectrum = vmaxForColorSpectrum;
    }

    public boolean isDrawRoadId() {
        return drawRoadId;
    }

    public void setDrawRoadId(boolean drawRoadId) {
        this.drawRoadId = drawRoadId;
        repaint();
    }

    public void setDrawSources(boolean b) {
        this.drawSouces = b;
        repaint();
    }

    public void setDrawSinks(boolean b) {
        this.drawSinks = b;
        repaint();
    }

    public void setDrawSpeedLimits(boolean b) {
        this.drawSpeedLimits = b;
        repaint();
    }

    public void setDrawSlopes(boolean b) {
        this.drawSlopes = b;
        repaint();
    }

    /**
     * Returns the color of the vehicle. The color may depend on the vehicle's properties, such as its velocity.
     * 
     * @param vehicle
     * @param simulationTime
     */
    protected Color vehicleColor(Vehicle vehicle, double simulationTime) {
        Color color;
        final int count;

        switch (vehicleColorMode) {
        case ACCELERATION_COLOR:
            final double a = vehicle.physicalQuantities().getAcc();
            count = accelerations.length;
            for (int i = 0; i < count; ++i) {
                if (a < accelerations[i])
                    return accelerationColors[i];
            }
            return accelerationColors[accelerationColors.length - 1];
        case EXIT_COLOR:
            color = Color.BLACK;
            // if (vehicle.exitRoadSegmentId() != Vehicle.EXIT_AT_ROAD_END) {
            // switch (vehicle.exitRoadSegmentId() % 5) {
            // case 0:
            color = Color.WHITE;
            break;
        case HIGHLIGHT_VEHICLE:
            color = vehicle.getId() == vehicleToHighlightId ? Color.BLUE : Color.BLACK;
            break;
        case LANE_CHANGE:
            color = Color.BLACK;
            if (vehicle.inProcessOfLaneChange()) {
                color = Color.ORANGE;
            }
            break;
        case VEHICLE_COLOR:
            color = Color.BLACK;
            break;
        case VEHICLE_LABEL_COLOR:
            String label = vehicle.getLabel();
            // color = labelColors.get(label); //TODO put a color for each prototype in a HashMap
            color = Color.GREEN;
            break;
        default:
            final double v = vehicle.physicalQuantities().getSpeed() * 3.6;
            color = SwingHelper.getColorAccordingToSpectrum(0, getVmaxForColorSpectrum(), v);
        }
        return color;
    }

    /**
     * Callback to allow the application to make any further required drawing after the vehicles have been moved.
     */
    protected void drawAfterVehiclesMoved(Graphics2D g, double simulationTime, long iterationCount) {
    }

    /**
     * <p>
     * Draws the foreground: everything that moves each timestep. For the traffic simulation that means draw all the vehicles:<br />
     * For each roadSection, draw all the vehicles in the roadSection, positioning them using the roadMapping for that roadSection.
     * </p>
     * 
     * <p>
     * This method is synchronized with the <code>SimulationRunnable.run()</code> method, so that vehicles are not updated, added or removed
     * while they are being drawn.
     * </p>
     * <p>
     * tm The abstract method paintAfterVehiclesMoved is called after the vehicles have been moved, to allow any further required drawing on
     * the canvas.
     * </p>
     * 
     * @param g
     */
    @Override
    protected void drawForeground(Graphics2D g) {
        // moveVehicles occurs in the UI thread, so must synchronize with the
        // update of the road network in the calculation thread.

        final long timeBeforePaint_ms = System.currentTimeMillis();

        synchronized (simulationRunnable.dataLock) {

            drawTrafficLights(g);

            final double simulationTime = this.simulationTime();

            for (final RoadSegment roadSegment : roadNetwork) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                assert roadMapping != null;

                PaintRoadMapping.setClipPath(g, roadMapping, clipPath);
                for (final Vehicle vehicle : roadSegment) {
                    drawVehicle(g, simulationTime, roadMapping, vehicle);
                }
            }

            totalAnimationTime += System.currentTimeMillis() - timeBeforePaint_ms;

            drawAfterVehiclesMoved(g, simulationRunnable.simulationTime(), simulationRunnable.iterationCount());

        }
    }

    private void drawVehicle(Graphics2D g, double simulationTime, RoadMapping roadMapping, Vehicle vehicle) {
        // draw vehicle polygon at new position
        final RoadMapping.PolygonFloat polygon = roadMapping.mapFloat(vehicle, simulationTime);
        vehiclePath.reset();

        vehiclePath.moveTo(polygon.xPoints[0], polygon.yPoints[0]);
        vehiclePath.lineTo(polygon.xPoints[1], polygon.yPoints[1]);
        vehiclePath.lineTo(polygon.xPoints[2], polygon.yPoints[2]);
        vehiclePath.lineTo(polygon.xPoints[3], polygon.yPoints[3]);
        vehiclePath.closePath();
        g.setPaint(vehicleColor(vehicle, simulationTime));
        g.fill(vehiclePath);
        if (vehicle.isBrakeLightOn()) {
            // if the vehicle is decelerating then display the
            vehiclePath.reset();
            // points 2 & 3 are at the rear of vehicle
            vehiclePath.moveTo(polygon.xPoints[2], polygon.yPoints[2]);
            vehiclePath.lineTo(polygon.xPoints[3], polygon.yPoints[3]);
            vehiclePath.closePath();
            g.setPaint(brakeLightColor);
            g.draw(vehiclePath);
        }
    }

    /**
     * Draws the background: everything that does not move each timestep. The background consists of the road segments and the sources and
     * sinks, if they are visible.
     * 
     * @param g
     */
    @Override
    protected void drawBackground(Graphics2D g) {
        if (drawSouces) {
            drawSources(g);
        }
        if (drawSinks) {
            drawSinks(g);
        }
        drawRoadSegments(g);

        if (drawSpeedLimits) {
            drawSpeedLimits(g);
        }

        if (drawSlopes) {
            drawSlopes(g);
        }

        if (drawRoadId) {
            drawRoadSectionIds(g);
        }

    }

    /**
     * Draws each road segment in the road network.
     * 
     * @param g
     */
    private void drawRoadSegments(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            // System.out.println("draw roadSegment: " + roadSegment);
            assert roadMapping != null;
            drawRoadSegment(g, roadMapping);
            drawRoadSegmentLines(g, roadMapping); // in one step (parallel or sequential update)?!
        }
    }

    private void drawRoadSegment(Graphics2D g, RoadMapping roadMapping) {
        final BasicStroke roadStroke = new BasicStroke((float) roadMapping.roadWidth(), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER);
        g.setStroke(roadStroke);
        g.setColor(Color.GRAY);
        g.setColor(roadMapping.roadColor());
        PaintRoadMapping.paintRoadMapping(g, roadMapping);
    }

    /**
     * Draws the road lines and road edges.
     * 
     * @param g
     */
    private void drawRoadSegmentLines(Graphics2D g, RoadMapping roadMapping) {

        final float lineWidth = 1.0f; // a bit large, but ensures they are
                                      // visible
        final float lineLength = 5.0f;
        final float gapLength = 0.0f; // TODO rg modified for vasaloppet
        // TODO set dashPhase so road-line joins between road segments are
        // smooth

        // TODO
        // final float dashPhase = (float) (roadSection.getRoadLength() %
        // (lineLength + gapLength));
        final float dashPhase = (float) (roadMapping.roadLength() % (lineLength + gapLength));
        // final float dashPhase = (float)(roadSection.cumulativeRoadLength() %
        // (lineLength + gapLength));

        final Stroke lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                new float[] { lineLength, gapLength }, dashPhase);
        g.setStroke(lineStroke);
        g.setColor(roadLineColor);

        // draw the road lines
        final int laneCount = roadMapping.laneCount();
        for (int lane = 1; lane < laneCount; ++lane) {
            final double offset = roadMapping.laneInsideEdgeOffset(lane);
            if (lane == roadMapping.trafficLaneMin() || lane == roadMapping.trafficLaneMax()) {
                // use exit stroke pattern for on-ramps, off-ramps etc
                final Stroke exitStroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                        10.0f, new float[] { 5.0f, gapLength }, 5.0f); // TODO rg: modified for vasaloppet 6.0f
                g.setStroke(exitStroke);
            } else {
                g.setStroke(lineStroke);
            }
            PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);
        }

        // draw the road edges
        g.setStroke(new BasicStroke());
        g.setColor(roadEdgeColor);
        // inside edge
        double offset = roadMapping.laneInsideEdgeOffset(0);
        PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);
        // outside edge
        offset = roadMapping.laneInsideEdgeOffset(laneCount);
        PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);

    }

    private void drawTrafficLights(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            drawTrafficLightsOnRoad(g, roadSegment);
        }
    }

    private void drawTrafficLightsOnRoad(Graphics2D g, RoadSegment roadSegment) {
        if (roadSegment.trafficLights() == null) {
            return;
        }
        final RoadMapping roadMapping = roadSegment.roadMapping();
        assert roadMapping != null;

        final int offset = -(int) ((roadMapping.laneCount() / 2.0 + 1.5) * roadMapping.laneWidth());
        final int size = (int) (2 * roadMapping.laneWidth());
        final int radius = (int) (1.8 * roadMapping.laneWidth());
        for (final TrafficLight trafficLight : roadSegment.trafficLights()) {
            g.setColor(Color.DARK_GRAY);
            final RoadMapping.PosTheta posTheta = roadMapping.map(trafficLight.position(), offset);
            g.fillRect((int) posTheta.x - size / 2, (int) posTheta.y - size / 2, size, size);
            if (trafficLight.isGreen()) {
                g.setColor(Color.GREEN);
            } else if (trafficLight.isRed()) {
                g.setColor(Color.RED);
            } else if (trafficLight.isRedGreen()) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.YELLOW);
            }
            g.fillOval((int) posTheta.x - radius / 2, (int) posTheta.y - radius / 2, radius, radius);
        }
    }

    private void drawSpeedLimits(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            drawSpeedLimitsOnRoad(g, roadSegment);
        }
    }

    private void drawSlopes(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            drawSlopesOnRoad(g, roadSegment);
        }
    }

    private void drawSpeedLimitsOnRoad(Graphics2D g, RoadSegment roadSegment) {
        if (roadSegment.speedLimits() == null) {
            return;
        }

        final RoadMapping roadMapping = roadSegment.roadMapping();
        assert roadMapping != null;
        final double offset = -(roadMapping.laneCount() / 2.0 + 1.5) * roadMapping.laneWidth();
        final int redRadius2 = (int) (2.5 * roadMapping.laneWidth()) / 2;
        final int whiteRadius2 = (int) (2.0 * roadMapping.laneWidth()) / 2;
        final int fontHeight = whiteRadius2;
        final int yOffset = (int) (0.4 * fontHeight);
        final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
        final FontMetrics fontMetrics = getFontMetrics(font);

        for (final SpeedLimit speedLimit : roadSegment.speedLimits()) {

            g.setFont(font);
            final RoadMapping.PosTheta posTheta = roadMapping.map(speedLimit.getPosition(), offset);

            final double speedLimitValueKmh = speedLimit.getSpeedLimitKmh();
            if (speedLimitValueKmh < 150) {
                g.setColor(Color.RED);
                g.fillOval((int) posTheta.x - redRadius2, (int) posTheta.y - redRadius2, 2 * redRadius2, 2 * redRadius2);
                g.setColor(Color.WHITE);
                g.fillOval((int) posTheta.x - whiteRadius2, (int) posTheta.y - whiteRadius2, 2 * whiteRadius2,
                        2 * whiteRadius2);
                g.setColor(Color.BLACK);
                final String text = String.valueOf((int) (speedLimit.getSpeedLimitKmh()));
                final int textWidth = fontMetrics.stringWidth(text);
                g.drawString(text, (int) (posTheta.x - textWidth / 2.0), (int) (posTheta.y + yOffset));
            } else {
                // Draw a line between points (x1,y1) and (x2,y2)
                // draw speed limit clearing
                g.setColor(Color.BLACK);
                g.fillOval((int) posTheta.x - redRadius2, (int) posTheta.y - redRadius2, 2 * redRadius2, 2 * redRadius2);
                g.setColor(Color.WHITE);
                g.fillOval((int) posTheta.x - whiteRadius2, (int) posTheta.y - whiteRadius2, 2 * whiteRadius2,
                        2 * whiteRadius2);
                g.setColor(Color.BLACK);
                final int xOnCircle = (int) (whiteRadius2 * Math.cos(Math.toRadians(45.)));
                final int yOnCircle = (int) (whiteRadius2 * Math.sin(Math.toRadians(45.)));
                final Graphics2D g2 = g;
                final Line2D line = new Line2D.Double((int) posTheta.x - xOnCircle, (int) posTheta.y + yOnCircle,
                        (int) posTheta.x + xOnCircle, (int) posTheta.y - yOnCircle);
                g2.setStroke(new BasicStroke(2)); // thicker than just one pixel when calling g.drawLine
                g2.draw(line);
            }
        }
    }

    private void drawSlopesOnRoad(Graphics2D g, RoadSegment roadSegment) {
        if (roadSegment.slopes() == null) {
            return;
        }

        final RoadMapping roadMapping = roadSegment.roadMapping();
        assert roadMapping != null;
        final double laneWidth = 10; // ;
        final double offset = -(roadMapping.laneCount() / 2.0 + 1.5) * (roadMapping.laneWidth() + 1);
        final int redRadius2 = (int) (2.5 * laneWidth) / 2;
        final int whiteRadius2 = (int) (2.0 * laneWidth) / 2;
        final int fontHeight = whiteRadius2;
        final int yOffset = (int) (0.4 * fontHeight);
        final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
        final FontMetrics fontMetrics = getFontMetrics(font);

        for (final Slope slope : roadSegment.slopes()) {
            g.setFont(font);
            final RoadMapping.PosTheta posTheta = roadMapping.map(slope.getPosition(), offset);

            final double gradient = slope.getGradient() * 100;
            if (gradient != 0) {
                g.setColor(Color.BLACK);
                final String text = String.valueOf((int) (gradient)) + " %";
                final int textWidth = fontMetrics.stringWidth(text);
                g.drawString(text, (int) (posTheta.x - textWidth / 2.0), (int) (posTheta.y + yOffset));

            } else {
                // Draw a line between points (x1,y1) and (x2,y2)
                // draw speed limit clearing
                g.setColor(Color.BLACK);
                g.fillOval((int) posTheta.x - redRadius2, (int) posTheta.y - redRadius2, 2 * redRadius2, 2 * redRadius2);
                g.setColor(Color.WHITE);
                g.fillOval((int) posTheta.x - whiteRadius2, (int) posTheta.y - whiteRadius2, 2 * whiteRadius2,
                        2 * whiteRadius2);
                g.setColor(Color.BLACK);
                final int xOnCircle = (int) (whiteRadius2 * Math.cos(Math.toRadians(45.)));
                final int yOnCircle = (int) (whiteRadius2 * Math.sin(Math.toRadians(45.)));
                final Graphics2D g2 = g;
                final Line2D line = new Line2D.Double((int) posTheta.x - xOnCircle, (int) posTheta.y + yOnCircle,
                        (int) posTheta.x + xOnCircle, (int) posTheta.y - yOnCircle);
                g2.setStroke(new BasicStroke(2)); // thicker than just one pixel when calling g.drawLine
                g2.draw(line);
            }
        }
    }

    /**
     * Draws the ids for the road sections, sources and sinks.
     * 
     * @param g
     */
    private void drawRoadSectionIds(Graphics2D g) {

        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            assert roadMapping != null;
            String string;
            final int radius = (int) ((roadMapping.laneCount() + 2) * roadMapping.laneWidth());
            final RoadMapping.PosTheta posTheta = roadMapping.map(0.0);

            // draw the road segment's id
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.PLAIN, fontHeight); //$NON-NLS-1$
            g.setFont(font);
            g.setColor(Color.BLACK);
            g.drawString("R" + roadSegment.userId(), (int) (posTheta.x), (int) (posTheta.y)); //$NON-NLS-1$
        }
    }

    /**
     * Draws the sources.
     * 
     * @param g
     */
    private void drawSources(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            assert roadMapping != null;
            final int radius = (int) ((roadMapping.laneCount() + 2) * roadMapping.laneWidth());
            RoadMapping.PosTheta posTheta;

            // draw the road segment source, if there is one
            final TrafficSource trafficSource = roadSegment.getTrafficSource();
            if (trafficSource != null) {
                g.setColor(sourceColor);
                posTheta = roadMapping.startPos();
                g.fillOval((int) posTheta.x - radius / 2, (int) posTheta.y - radius / 2, radius, radius);

                g.setColor(Color.BLACK);
                // TODO this quantity reflects the desired inflow at the boundary but not the *actual* inflow. To be consistent
                // with the measured outflow the actual fed-in flow should be displayed.
                String inflowString = "inflow: "
                        + (int) (ConversionUtilities.INVS_TO_INVH * trafficSource.getTotalInflow(simulationTime()))
                        + " veh/h";
                g.drawString(inflowString, (int) (posTheta.x) + radius / 2, (int) (posTheta.y) + radius / 2);
            }
        }
    }

    /**
     * Draws the sinks.
     * 
     * @param g
     */
    private void drawSinks(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            assert roadMapping != null;
            final int radius = (int) ((roadMapping.laneCount() + 2) * roadMapping.laneWidth());
            RoadMapping.PosTheta posTheta;

            // draw the road segment sink, if there is one
            final TrafficSink sink = roadSegment.sink();
            if (sink != null) {
                g.setColor(sinkColor);
                posTheta = roadMapping.endPos();
                g.fillOval((int) posTheta.x - radius / 2, (int) posTheta.y - radius / 2, radius, radius);
                String outflowString = "outflow: " + (int) (ConversionUtilities.INVS_TO_INVH * sink.measuredOutflow())
                        + " veh/h";
                g.drawString(outflowString, (int) (posTheta.x) + radius / 2, (int) (posTheta.y) + radius / 2);
            }
        }
    }

    // ============================================================================================
    // SimulationRunnable callbacks
    // ============================================================================================

    /**
     * <p>
     * Implements SimulationRunnable.UpdateDrawingCallback.updateDrawing().
     * </p>
     * <p>
     * Calls repaint() which causes UI framework to asynchronously call update(g).
     * </p>
     */
    @Override
    public void updateDrawing(double simulationTime) {
        repaint();
    }

    /**
     * <p>
     * Implements SimulationRunnable.HandleExceptionCallback.handleException().
     * </p>
     * <p>
     * Called back from the TrafficRunnable thread, in the synchronization block, if an exception occurs.
     * </p>
     */
    @Override
    public void handleException(Exception e) {
        // if (e instanceof Vehicle.VehicleException) {
        // // if (e.getClass() == Vehicle.VehicleException.class) {
        // // something went wrong with the integration
        // final Vehicle.VehicleException v = (Vehicle.VehicleException) e;
        // final Vehicle vehicle = v.vehicle;
        // vehicleToHighlightId = vehicle.getId();
        // if (vehicleColorMode != VehicleColorMode.HIGHLIGHT_VEHICLE) {
        // vehicleColorModeSave = vehicleColorMode;
        // vehicleColorMode = VehicleColorMode.HIGHLIGHT_VEHICLE;
        // }
        // repaint();
        // if (DEBUG) {
        //                System.out.println("VehicleException id:" + vehicle.getId()); //$NON-NLS-1$
        //                System.out.println("  pos:" + vehicle.getPosition()); //$NON-NLS-1$
        //                System.out.println("  vel:" + vehicle.getSpeed()); //$NON-NLS-1$
        //                System.out.println("  roadSectionId:"); //$NON-NLS-1$
        // }
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.web.appletroad.control.SimulationRun.UpdateStatusCallback#updateStatus(double)
     */
    @Override
    public void updateStatus(double simulationTime) {
        // overridden in TrafficCanvasScenario
    }
}
