package org.movsim.output.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.output.Observable;
import org.movsim.output.Observer;

public class ObservableImpl implements Observable {

    private List<Observer> observers = new ArrayList<Observer>();
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(double time) {
        for (Observer o : observers ) {
            o.notifyObserver(time);
        }
    }

}
