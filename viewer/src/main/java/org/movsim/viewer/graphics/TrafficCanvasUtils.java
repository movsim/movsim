package org.movsim.viewer.graphics;

import org.movsim.roadmappings.PosTheta;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.roadmappings.RoadMapping.PolygonFloat;
import org.movsim.viewer.roadmapping.PaintRoadMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;

public final class TrafficCanvasUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficCanvasUtils.class);

    private TrafficCanvasUtils() {
        throw new IllegalStateException("do not invoke");
    }

    static void drawRoadSegment(Graphics2D g, RoadMapping roadMapping) {
        BasicStroke roadStroke = new BasicStroke((float) roadMapping.roadWidth(), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER);
        g.setStroke(roadStroke);
        g.setColor(new Color(roadMapping.roadColor()));
        PaintRoadMapping.paintRoadMapping(g, roadMapping);
    }

    static Rectangle2D getRectangle(PosTheta posTheta, double widthHeight) {
        Rectangle2D rect = new Rectangle2D.Double(posTheta.getScreenX(), posTheta.getScreenY(), widthHeight,
                widthHeight);
        LOG.debug("rectangle={}", rect);
        return rect;
    }

    static void drawTextRotated(String text, PosTheta posTheta, Font font, Graphics2D g) {
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text); //$NON-NLS-1$
        AffineTransform at = AffineTransform
                .getTranslateInstance((int) posTheta.getScreenX(), (int) posTheta.getScreenY());
        at.rotate(-posTheta.getTheta());
        Shape glyph = gv.getOutline();
        Shape transformedGlyph = at.createTransformedShape(glyph);
        g.fill(transformedGlyph);
    }

    static void drawLine(Graphics2D g, RoadMapping roadMapping, double position, int strokeWidth, Color color) {
        Color prevColor = g.getColor();
        final double lateralExtend = roadMapping.getLaneCountInDirection() * roadMapping.laneWidth();
        final PosTheta posTheta = roadMapping.map(position, 0/* offset */);
        final PolygonFloat line = roadMapping.mapLine(posTheta, roadMapping.isPeer() ? +lateralExtend : -lateralExtend);
        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Line2D.Float(line.getXPoint(0), line.getYPoint(0), line.getXPoint(1), line.getYPoint(1)));
        g.setColor(prevColor);
    }

    static void fillPath(PolygonFloat polygon, GeneralPath path) {
        path.reset();
        path.moveTo(polygon.getXPoint(0), polygon.getYPoint(0));
        path.lineTo(polygon.getXPoint(1), polygon.getYPoint(1));
        path.lineTo(polygon.getXPoint(2), polygon.getYPoint(2));
        path.lineTo(polygon.getXPoint(3), polygon.getYPoint(3));
        path.closePath();
    }

    static Point2D getTransformed(Point point, AffineTransform transform) throws NoninvertibleTransformException {
        final Point2D transformedPoint = new Point2D.Float();
        // convert from mouse coordinates to vehicle coordinates
        transform.inverseTransform(new Point2D.Float(point.x, point.y), transformedPoint);
        return transformedPoint;
    }

}
