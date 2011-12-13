package org.movsim.input.model.simulation;

import org.jdom.Element;

public class TrafficSinkData {

    private boolean withLogging = false;

    public TrafficSinkData(Element elem) {
        if (elem != null) {
            withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
        }
    }

    public boolean withLogging() {
        return withLogging;
    }

}
