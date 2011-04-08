package org.movsim.input.model.simulation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.simulation.InflowDataPoint;
import org.movsim.input.model.simulation.SimpleRampData;

public class SimpleRampDataImpl implements SimpleRampData{

    private List<InflowDataPoint> inflowTimeSeries;
    private double centerPosition;
    private double rampLength;
    private boolean withLogging;

    @SuppressWarnings("unchecked")
    public SimpleRampDataImpl(Element elem){
        this.centerPosition = Double.parseDouble(elem.getAttributeValue("x_center"));
        this.rampLength = Double.parseDouble(elem.getAttributeValue("length"));
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("with_logging"));
        
        final List<Element> inflowElems = elem.getChildren("INFLOW");
        parseAndSortInflowElements(inflowElems);
        
    }

    private void parseAndSortInflowElements(List<Element> inflowElems) {
        inflowTimeSeries = new ArrayList<InflowDataPoint>();
        for (Element inflowElem : inflowElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(inflowElem);
            inflowTimeSeries.add(new InflowDataPointImpl(map));
        }
        Collections.sort(inflowTimeSeries, new Comparator<InflowDataPoint>() {
            public int compare(InflowDataPoint o1, InflowDataPoint o2) {
                Double pos1 = new Double((o1).getTime());
                Double pos2 = new Double((o2).getTime());
                return pos1.compareTo(pos2); // sort with increasing t 
            }
        });
    }

    public List<InflowDataPoint> getInflowTimeSeries() {
        return inflowTimeSeries;
    }

    public double getCenterPosition() {
        return centerPosition;
    }
    public double getRampLength() {
        return rampLength;
    }

    public boolean withLogging(){
        return withLogging;
    }
    


}
