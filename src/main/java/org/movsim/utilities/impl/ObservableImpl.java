package org.movsim.utilities.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.utilities.Observable;
import org.movsim.utilities.ObservableInTime;
import org.movsim.utilities.Observer;
import org.movsim.utilities.ObserverInTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObservableImpl implements ObservableInTime, Observable{

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ObservableImpl.class);
    
    private List<Observer> observers = new ArrayList<Observer>();
    private List<ObserverInTime> observersInTime = new ArrayList<ObserverInTime>();
    
    @Override
    public void registerObserver(ObserverInTime observer) {
        if( observersInTime.contains(observer) ){
            logger.error(" observer already registered, please fix this inconsistency. exit");
            System.exit(-1);
        }
        observersInTime.add(observer);
    }
    
    @Override
    public void registerObserver(Observer observer) {
        if( observers.contains(observer) ){
            logger.error(" observer already registered, please fix this inconsistency. exit");
            System.exit(-1);
        }
        observers.add(observer);
    }

    @Override
    public void removeObserver(ObserverInTime observer) {
        observersInTime.remove(observer);
        final int i = observersInTime.indexOf(observer);
        if (i >= 0) {
            observersInTime.remove(observer);
            logger.debug(" observer removed from observer list");
        }
        else{
            logger.warn(" try to remove observer from observer list but observer is not contained in list");
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        final int i = observers.indexOf(observer);
        if (i >= 0) {
            observers.remove(observer);
            logger.debug(" observer removed from observer list");
        }
        else{
            logger.warn(" try to remove observer from observer list but observer is not contained in list");
        }
    }
   
    
    public void notifyObservers(double time) {
        for (final ObserverInTime o : observersInTime ) {
            o.notifyObserver(time);
        }
        //logger.debug(" n = {} observers notified at time = {}", observersInTime.size(), time);
    }
    
    public void notifyObservers() {
        for (final Observer o : observers ) {
            o.notifyObserver();
        }
        //logger.debug(" n = {} observers notified", observers.size());
    }

}
