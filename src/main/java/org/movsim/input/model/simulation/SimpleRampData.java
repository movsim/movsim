package org.movsim.input.model.simulation;

import java.util.List;


public interface SimpleRampData {

    List<InflowDataPoint> getInflowTimeSeries();

    double getCenterPosition();

    double getRampLength();

    boolean withLogging();

}