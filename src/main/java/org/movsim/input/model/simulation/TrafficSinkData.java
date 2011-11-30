package org.movsim.input.model.simulation;

import org.jdom.Element;

public class TrafficSinkData {

    /** The with logging. */
    private boolean withLogging = false;

    private int sinkId = 0;

    public TrafficSinkData(Element elem) {
        if (elem != null) {
            sinkId = Integer.parseInt(elem.getAttributeValue("id"));
            withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
        }
    }

    public boolean withLogging() {
        return withLogging;
    }

    public int getSinkId() {
        return sinkId;
    }

}
