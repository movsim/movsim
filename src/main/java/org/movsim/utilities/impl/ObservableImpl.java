package org.movsim.utilities.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.utilities.Observable;
import org.movsim.utilities.ObservableInTime;
import org.movsim.utilities.Observer;
import org.movsim.utilities.ObserverInTime;

public abstract class ObservableImpl implements ObservableInTime, Observable{

    private List<Observer> observers = new ArrayList<Observer>();
    private List<ObserverInTime> observersInTime = new ArrayList<ObserverInTime>();
    
    @Override
    public void registerObserver(ObserverInTime observer) {
        observersInTime.add(observer);
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ObserverInTime observer) {
        observersInTime.remove(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
   
    
    public void notifyObservers(double time) {
        for (final ObserverInTime o : observersInTime ) {
            o.notifyObserver(time);
        }
    }
    
    public void notifyObservers() {
        for (final Observer o : observers ) {
            o.notifyObserver();
        }
    }

}
