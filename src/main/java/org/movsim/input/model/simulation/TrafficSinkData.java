package org.movsim.input.model.simulation;

import org.jdom.Element;

public class TrafficSinkData {

    /** The with logging. */
    private final boolean withLogging;

    private final int sinkId;

    public TrafficSinkData(Element elem) {
        sinkId = Integer.parseInt(elem.getAttributeValue("id"));
        withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
    }

    public boolean withLogging() {
        return withLogging;
    }

    public int getSinkId() {
        return sinkId;
    }

}
