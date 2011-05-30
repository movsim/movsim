package org.movsim.utilities.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.utilities.Observable;
import org.movsim.utilities.ObserverInTime;

public class ObservableImpl implements Observable {

    private List<ObserverInTime> observers = new ArrayList<ObserverInTime>();
    
    @Override
    public void registerObserver(ObserverInTime observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ObserverInTime observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(double time) {
        for (ObserverInTime o : observers ) {
            o.notifyObserver(time);
        }
    }

}
