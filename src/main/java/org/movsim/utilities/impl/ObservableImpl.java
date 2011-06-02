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
        if( observers.contains(observer) ){
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
     // ake: this applies only if *same* observer is registered multiple times, this is not consistent behavior; we now check this case when registering
//        int i = observersInTime.indexOf(observer);

//        if (i >= 0) {
//            observersInTime.remove(observer);
//        }
    }

    @Override
    public void removeObserver(Observer observer) {
        int i = observers.indexOf(observer);
        if (i >= 0) {
            observers.remove(observer);
        }
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
