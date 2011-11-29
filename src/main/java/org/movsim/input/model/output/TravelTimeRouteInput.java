package org.movsim.input.model.output;

import org.jdom.Element;

public class TravelTimeRouteInput {

    public long getStartId() {
        return startId;
    }

    public long getEndId() {
        return endId;
    }

    public double getStartPosition() {
        return startPosition;
    }

    public double getEndPosition() {
        return endPosition;
    }

    private final long startId;
    private final long endId;

    private final double startPosition;
    private final double endPosition;

    public TravelTimeRouteInput(Element elem) {
        this.startId = Long.parseLong(elem.getAttributeValue("start_id"));
        this.endId = Long.parseLong(elem.getAttributeValue("end_id"));

        this.startPosition = Double.parseDouble(elem.getAttributeValue("start_pos"));
        this.endPosition = Double.parseDouble(elem.getAttributeValue("end_pos"));

    }

}
