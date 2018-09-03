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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.movsim.roadmappings.PosTheta;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.roadmappings.RoadMapping.PolygonFloat;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSink;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.roadnetwork.controller.VariableMessageSignDiversion;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.viewer.graphics.TrafficCanvas.VehicleColorMode;
import org.movsim.viewer.util.SwingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class TrafficCanvasMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficCanvasMouseListener.class);

    private final TrafficCanvas trafficCanvas;
    private final TrafficCanvasController controller;
    private final RoadNetwork roadNetwork;
    private boolean inDrag;
    private int startDragX;
    private int startDragY;
    private int xOffsetSave;
    private int yOffsetSave;
    private final boolean draggingAllowed = true;

    /**
     * @param trafficCanvas
     */
    public TrafficCanvasMouseListener(TrafficCanvas trafficCanvas, TrafficCanvasController controller,
            RoadNetwork roadNetwork) {
        this.trafficCanvas = Preconditions.checkNotNull(trafficCanvas);
        this.controller = Preconditions.checkNotNull(controller);
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        LOG.debug("mouseClicked at screen={}", mouseEvent.getPoint()); //$NON-NLS-1$
        if (trafficCanvas.lastVehicleViewed != -1) {
            LOG.info("vehicle id set"); //$NON-NLS-1$
            trafficCanvas.vehicleToHighlightId = trafficCanvas.lastVehicleViewed;
            trafficCanvas.vehicleColorMode = VehicleColorMode.HIGHLIGHT_VEHICLE;
            trafficCanvas.repaint();
        }

        try {
            final Point eventPoint = mouseEvent.getPoint();
            // convert from mouse coordinates to canvas coordinates
            Point2D transformedPoint = TrafficCanvasUtils.getTransformed(eventPoint, trafficCanvas.transform);
            LOG.debug("mouse clicked at transformed point={}", transformedPoint);
            for (final RoadSegment roadSegment : roadNetwork) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                // TODO check if angles are correctly mapped
                checkForVariableMessageSigns(transformedPoint, roadSegment, roadMapping);
                checkForTrafficLights(transformedPoint, roadSegment, roadMapping);
            }
        } catch (NoninvertibleTransformException e1) {
            LOG.error("Error occurred", e1);
        }
    }

    private void checkForTrafficLights(Point2D transformedPoint, RoadSegment roadSegment, RoadMapping roadMapping) {
        for (final TrafficLight trafficLight : roadSegment.trafficLights()) {
            final double widthHeight = roadMapping.roadWidth(); // hack: just proxy for strokeWidth
            final PosTheta posTheta = roadMapping.map(trafficLight.position(), 0);
            final Rectangle2D trafficLightRect = TrafficCanvasUtils.getRectangle(posTheta, widthHeight);
            if (trafficLightRect.contains(transformedPoint)) {
                LOG.info("mouse clicked: traffic light triggers next phase");
                trafficLight.triggerNextPhase();
                trafficCanvas.repaint();
            }
        }
    }

    private void checkForVariableMessageSigns(Point2D transformedPoint, RoadSegment roadSegment, RoadMapping roadMapping) {
        for (VariableMessageSignDiversion vmsDiversion : roadSegment.variableMessageSignDiversions()) {
            PosTheta posTheta = roadMapping.startPos();
            GeneralPath path = new GeneralPath();
            path.moveTo(posTheta.getScreenX(), posTheta.getScreenY());
            posTheta = roadMapping.map(roadMapping.roadLength(), 0);
            path.lineTo(posTheta.getScreenX(), posTheta.getScreenY());
            posTheta = roadMapping.map(roadMapping.roadLength(), roadMapping.getMaxOffsetRight());
            path.lineTo(posTheta.getScreenX(), posTheta.getScreenY());
            posTheta = roadMapping.map(0, roadMapping.getMaxOffsetRight());
            path.lineTo(posTheta.getScreenX(), posTheta.getScreenY());
            path.closePath();
            if (path.contains(transformedPoint)) {
                LOG.info("mouse clicked: toggle status of variable message sign");
                vmsDiversion.toogleActiveStatus();
                trafficCanvas.repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (!draggingAllowed) {
            return;
        }
        final Point point = mouseEvent.getPoint();
        startDragX = point.x;
        startDragY = point.y;
        xOffsetSave = trafficCanvas.xOffset;
        yOffsetSave = trafficCanvas.yOffset;
        inDrag = true;
        trafficCanvas.backgroundChanged = false;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        inDrag = false;
        trafficCanvas.backgroundChanged = false;
        if (trafficCanvas.mouseOverTipWindow != null) {
            trafficCanvas.mouseOverTipWindow.setVisible(false);
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        LOG.debug("SimCanvas mouseEntered");
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        // do nothing
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        final Point p = mouseEvent.getPoint();
        if (inDrag) {
            final int xOffsetNew = xOffsetSave + (int) ((p.x - startDragX) / trafficCanvas.scale);
            final int yOffsetNew = yOffsetSave + (int) ((p.y - startDragY) / trafficCanvas.scale);
            trafficCanvas.backgroundChanged = false;
            if (xOffsetNew != trafficCanvas.xOffset || yOffsetNew != trafficCanvas.yOffset) {
                trafficCanvas.xOffset = xOffsetNew;
                trafficCanvas.yOffset = yOffsetNew;
                trafficCanvas.setTransform();
                trafficCanvas.forceRepaintBackground();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if (trafficCanvas.isStopped() || trafficCanvas.isPaused()) {
            if (trafficCanvas.mouseOverTipWindow == null) {
                trafficCanvas.mouseOverTipWindow = new MouseOverTipWindow(trafficCanvas,
                        SwingHelper.getFrame(trafficCanvas));
            }

            try {
                Point2D transformedPoint = TrafficCanvasUtils.getTransformed(mouseEvent.getPoint(),
                        trafficCanvas.transform);
                final GeneralPath path = new GeneralPath();
                // iterate over all vehicles in all road segments, to see if the
                // mouse is over a vehicle
                for (RoadSegment roadSegment : roadNetwork) {
                    RoadMapping roadMapping = roadSegment.roadMapping();

                    AbstractTrafficSource source = roadSegment.trafficSource();
                    if (source != null) {
                        PolygonFloat polygon = roadMapping.mapFloat(roadMapping.startPos(), 5, roadMapping.roadWidth());
                        TrafficCanvasUtils.fillPath(polygon, path);
                        if (path.contains(transformedPoint)) {
                            // the mouse is over a source
                            LOG.debug("mouse over source");
                            trafficCanvas.showSourceMouseOverInfo(mouseEvent.getPoint(), source);
                        }
                    }

                    TrafficSink sink = roadSegment.sink();
                    if (sink != null) {
                        double length = 5;
                        PolygonFloat polygon = roadMapping.mapFloat(roadMapping.endPos(), length,
                                roadMapping.roadWidth());
                        TrafficCanvasUtils.fillPath(polygon, path);
                        if (path.contains(transformedPoint)) {
                            // the mouse is over a source
                            LOG.debug("mouse over sink");
                            trafficCanvas.showSinkMouseOverInfo(mouseEvent.getPoint(), sink);
                        }
                    }

                    for (Vehicle vehicle : roadSegment) {
                        // TODO quick hack here, no correction for offsets
                        final RoadMapping.PolygonFloat polygon = roadMapping.mapFloat(vehicle);
                        TrafficCanvasUtils.fillPath(polygon, path);
                        if (path.contains(transformedPoint)) {
                            // the mouse is over a vehicle
                            LOG.debug("mouse over vehicle={}", vehicle);
                            trafficCanvas.showVehicleMouseOverInfo(mouseEvent.getPoint(), vehicle);
                            break;
                        }
                    }
                }
            } catch (NoninvertibleTransformException e) {
                LOG.error("error occurred", e.getMessage());
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        final int notches = event.getWheelRotation();
        if (notches < 0) {
            LOG.debug("Mouse wheel moved UP {} notch(es)", -notches);
            controller.commandZoomIn();
        } else {
            LOG.debug("Mouse wheel moved DOWN {} notch(es)", notches);
            controller.commandZoomOut();
        }
    }
}
