package org.movsim.simulator.roadnetwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SignalPoints implements Iterable<SignalPoint> {

    private final List<SignalPoint> signalPoints = new ArrayList<>();

    public void add(SignalPoint signalPoint) {
        signalPoints.add(signalPoint);
    }

    @Override
    public Iterator<SignalPoint> iterator() {
        return signalPoints.iterator();
    }

}
