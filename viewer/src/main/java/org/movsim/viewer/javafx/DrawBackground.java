package org.movsim.viewer.javafx;

import org.jfree.fx.FXGraphics2D;
import org.movsim.roadmappings.PosTheta;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSink;
import org.movsim.simulator.roadnetwork.controller.FlowConservingBottleneck;
import org.movsim.simulator.roadnetwork.controller.GradientProfile;
import org.movsim.simulator.roadnetwork.controller.SpeedLimit;
import org.movsim.simulator.roadnetwork.regulator.NotifyObject;
import org.movsim.simulator.roadnetwork.regulator.Regulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class DrawBackground {
    private static final Logger LOG = LoggerFactory.getLogger(DrawBackground.class);

    private GraphicSettings settings;
    private RoadNetwork roadNetwork;
    private SimulationRunnable simulationRunnable;
    private Simulator simulator;

    private BufferedImage backgroundPicture = null;

    public DrawBackground(GraphicSettings settings, RoadNetwork roadNetwork, SimulationRunnable simulationRunnable, Simulator simulator) {
        this.settings = settings;
        this.roadNetwork = roadNetwork;
        this.simulationRunnable = simulationRunnable;
        this.simulator = simulator;
    }

    public void update(FXGraphics2D fxGraphics2D) {
        clearBackground(fxGraphics2D); // TODO clearBackground not working
        drawBackground(fxGraphics2D);
    }

    private void clearBackground(FXGraphics2D g) {
        g.setColor(settings.getBackgroundColor());
        g.fillRect(0, 0, 1000, 800);
    }

    /**
     * Draws the background: everything that does not move each timestep. The background consists of the road segments and the sources and
     * sinks, if they are visible.
     *
     * @param g
     */
    private void drawBackground(FXGraphics2D g) {

        if (backgroundPicture != null)
            g.drawImage(backgroundPicture, 0, -(int) (backgroundPicture.getHeight()),
                    (int) (backgroundPicture.getWidth() * 1.01), 0, 0, 0, backgroundPicture.getWidth(),
                    backgroundPicture.getHeight(), null);

        drawRoadSegmentsAndLines(g);

        if (settings.isDrawSources()) {
            drawSources(g);
        }

        if (settings.isDrawSinks()) {
            drawSinks(g);
        }

        if (settings.isDrawSpeedLimits()) {
            drawSpeedLimits(g);
        }

        if (settings.isDrawSlopes()) {
            drawSlopes(g);
        }

        if (settings.isDrawFlowConservingBottlenecks()) {
            drawFlowConservingBottlenecks(g);
        }

        if (settings.isDrawRoadId()) {
            drawRoadSectionIds(g);
        }

        if (settings.isDrawNotifyObjects()) {
            drawNotifyObjects(g);
        }
    }
    private void drawRoadSegmentsAndLines(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            if (roadMapping.isPeer()) {
                LOG.debug("skip painting peer element={}", roadMapping);
                continue;
            }
            drawRoadSegment(g, roadMapping);
            drawRoadSegmentLines(g, roadMapping);
        }
    }

    private void drawRoadSegment(FXGraphics2D g, RoadMapping roadMapping) {
        BasicStroke roadStroke = new BasicStroke((float) roadMapping.roadWidth(), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER);
        g.setStroke(roadStroke);
        g.setColor(new Color(roadMapping.roadColor()));
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping);
    }

    private void drawRoadSegmentLines(FXGraphics2D g, RoadMapping roadMapping) {
        final float dashPhase = (float) (roadMapping.roadLength() % (settings.getLineLength() + settings.getGapLength()));

        final Stroke lineStroke = new BasicStroke(settings.getLineWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                new float[]{settings.getLineLength(), settings.getGapLength()}, dashPhase);
        g.setStroke(lineStroke);
        g.setColor(settings.getRoadLineColor());

        // draw the road lines: left lane is positive
        int maxRightLane = -roadMapping.getLaneGeometries().getRight().getLaneCount();
        int maxLeftLane = roadMapping.getLaneGeometries().getLeft().getLaneCount();
        for (int lane = maxRightLane + 1; lane < maxLeftLane; lane++) {
            final double offset = lane * roadMapping.getLaneGeometries().getLaneWidth();
            LOG.debug("draw road lines: lane={}, offset={}", lane, offset);
            // FIXME after reimpl
            // if (lane == roadMapping.trafficLaneMin() || lane == roadMapping.trafficLaneMax()) {
            // // use exit stroke pattern for on-ramps, off-ramps etc
            // final Stroke exitStroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
            // 10.0f, new float[] { 5.0f, gapLengthExit }, 5.0f);
            // g.setStroke(exitStroke);
            // } else {
            g.setStroke(lineStroke);
            // }
            PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
        }

        // draw the road edges
        g.setStroke(new BasicStroke());
        g.setColor(settings.getRoadEdgeColor());
        double offset = roadMapping.getLaneGeometries().getLeft().getLaneCount()
                * roadMapping.getLaneGeometries().getLaneWidth();
        LOG.debug("draw road left outer edge: offset={}", offset);
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
        // edge of most outer edge
        offset = roadMapping.getMaxOffsetRight();
        LOG.debug("draw road most outer edge: offset={}", offset);
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
    }
    private void drawSpeedLimits(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            assert roadSegment.speedLimits() != null;
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.BOLD, fontHeight);
            final RoadMapping roadMapping = roadSegment.roadMapping();
            for (SpeedLimit speedLimit : roadSegment.speedLimits()) {
                g.setFont(font);
                final double position = speedLimit.position();
                final double offset = roadMapping.isPeer()
                        ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                        : roadMapping.getMaxOffsetRight();
                final PosTheta posTheta = roadMapping.map(position, offset);
                final String text = String.valueOf((int) (speedLimit.getSpeedLimitKmh())) + "km/h";
                g.setFont(font);
                java.awt.Color color = speedLimit.getSpeedLimit() < MovsimConstants.MAX_VEHICLE_SPEED ? java.awt.Color.RED
                        : java.awt.Color.DARK_GRAY;
                g.setColor(color);
                PaintRoadMappingFx.drawTextRotated(text, posTheta, font, g);
                PaintRoadMappingFx.drawLine(g, roadMapping, position, 1, color);
            }
        }
    }

    private void drawSlopes(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            for (GradientProfile gradientProfile : roadSegment.gradientProfiles()) {
                for (Map.Entry<Double, Double> gradientEntry : gradientProfile.gradientEntries()) {
                    final double position = gradientEntry.getKey();
                    final PosTheta posTheta = roadMapping.map(position, offset);
                    final double gradient = gradientEntry.getValue() * 100;
                    final String text = String.valueOf((int) (gradient)) + "%";
                    PaintRoadMappingFx.drawTextRotated(text, posTheta, font, g);
                    PaintRoadMappingFx.drawLine(g, roadMapping, position, 1, java.awt.Color.DARK_GRAY);
                }
            }
        }
    }

    private void drawFlowConservingBottlenecks(FXGraphics2D g) {
        final int fontHeight = 12;
        final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
        final java.awt.Color color = java.awt.Color.ORANGE;
        final java.awt.Color prevColor = g.getColor();
        g.setColor(color);
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            for (FlowConservingBottleneck bottleneck : roadSegment.flowConservingBottlenecks()) {
                final double posStart = bottleneck.position();
                PosTheta posTheta = roadMapping.map(posStart, offset);
                PaintRoadMappingFx.drawTextRotated(" bneck start", posTheta, font, g);
                PaintRoadMappingFx.drawLine(g, roadMapping, posStart, 2, color);

                final double posEnd = bottleneck.endPosition();
                posTheta = roadMapping.map(posEnd, offset);
                PaintRoadMappingFx.drawTextRotated(" bneck end", posTheta, font, g);
                PaintRoadMappingFx.drawLine(g, roadMapping, bottleneck.endPosition(), 2, color);
            }

        }
        g.setColor(prevColor);
    }

    private void drawSources(FXGraphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            AbstractTrafficSource trafficSource = roadSegment.trafficSource();
            if (trafficSource != null) {
                PaintRoadMappingFx.drawLine(g, roadSegment.roadMapping(), 0, 4, settings.getSourceColor());
            }
        }
    }

    private void drawSinks(FXGraphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            TrafficSink sink = roadSegment.sink();
            if (sink != null) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                PaintRoadMappingFx.drawLine(g, roadMapping, roadMapping.roadLength(), 4, settings.getSinkColor());
            }
        }
    }

    private void drawNotifyObjects(FXGraphics2D g) {
        for (Regulator regulator : simulator.getRegulators()) {
            for (NotifyObject notifyObject : regulator.getNotifyObjects()) {
                RoadMapping roadMapping = notifyObject.getRoadSegment().roadMapping();
                PaintRoadMappingFx.drawLine(g, roadMapping, notifyObject.getPosition(), 2, java.awt.Color.DARK_GRAY);
            }
        }
    }

    private void drawRoadSectionIds(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double position = roadMapping.isPeer() ? roadMapping.roadLength() : 0.0;
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            final PosTheta posTheta = roadMapping.map(position, offset);
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.PLAIN, fontHeight);
            g.setFont(font);
            g.setColor(java.awt.Color.BLACK);
            PaintRoadMappingFx.drawTextRotated(roadSegment.userId(), posTheta, font, g);
        }
    }

    public BufferedImage getBackgroundPicture() {
        return backgroundPicture;
    }

    public void setBackgroundPicture(BufferedImage backgroundPicture) {
        this.backgroundPicture = backgroundPicture;
    }
}
