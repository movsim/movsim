/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.input.model.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;

public class TrafficLightsInput {

    private List<TrafficLightData> trafficLightData;

    private int nDtSample;

    /** The with logging. */
    private boolean withLogging;

    /**
     * Instantiates a new traffic lights input impl.
     * 
     * @param elem
     *            the elem
     */
    public TrafficLightsInput(Element elem) {

        trafficLightData = new ArrayList<TrafficLightData>();

        this.nDtSample = Integer.parseInt(elem.getAttributeValue("n_dt"));
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));

        @SuppressWarnings("unchecked")
        final List<Element> trafficLightElems = elem.getChildren(XmlElementNames.RoadTrafficLight);
        for (final Element trafficLightElem : trafficLightElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(trafficLightElem);
            trafficLightData.add(new TrafficLightData(map));
        }

        Collections.sort(trafficLightData, new Comparator<TrafficLightData>() {
            @Override
            public int compare(TrafficLightData o1, TrafficLightData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.simulation.TrafficLightsInput#getTrafficLightData
     * ()
     */
    public List<TrafficLightData> getTrafficLightData() {
        return trafficLightData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.TrafficLightsInput#getnDtSample()
     */
    public int getnDtSample() {
        return nDtSample;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.TrafficLightsInput#isWithLogging()
     */
    public boolean isWithLogging() {
        return withLogging;
    }

}
