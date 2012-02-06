/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */
package org.movsim.utilities;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ObservableImpl.
 */
public abstract class ObservableImpl implements ObservableInTime, Observable {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ObservableImpl.class);

    private final List<Observer> observers = new ArrayList<Observer>();
    private final List<ObserverInTime> observersInTime = new ArrayList<ObserverInTime>();

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.ObservableInTime#registerObserver(org.movsim.utilities .ObserverInTime)
     */
    @Override
    public void registerObserver(ObserverInTime observer) {
        if (observersInTime.contains(observer)) {
            logger.error(" observer already registered, please fix this inconsistency. exit");
            System.exit(-1);
        }
        observersInTime.add(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.Observable#registerObserver(org.movsim.utilities .Observer)
     */
    @Override
    public void registerObserver(Observer observer) {
        if (observers.contains(observer)) {
            logger.error(" observer already registered, please fix this inconsistency. exit");
            System.exit(-1);
        }
        observers.add(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.ObservableInTime#removeObserver(org.movsim.utilities .ObserverInTime)
     */
    @Override
    public void removeObserver(ObserverInTime observer) {
        final int i = observersInTime.indexOf(observer);
        if (i >= 0) {
            observersInTime.remove(observer);
            logger.debug(" observer removed from observer list");
        } else {
            logger.warn(" try to remove observerInTime from observer list but observer is not contained in list");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.Observable#removeObserver(org.movsim.utilities.Observer )
     */
    @Override
    public void removeObserver(Observer observer) {
        final int i = observers.indexOf(observer);
        if (i >= 0) {
            observers.remove(observer);
            logger.debug(" observer removed from observer list");
        } else {
            logger.warn(" try to remove observer from observer list but observer is not contained in list");
        }
    }

    /**
     * Notify observers.
     * 
     * @param time
     *            the time
     */
    public void notifyObservers(double time) {
        for (final ObserverInTime o : observersInTime) {
            o.notifyObserver(time);
        }
        logger.debug(" n = {} observers notified at time = {}", observersInTime.size(), time);
    }

    /**
     * Notify observers.
     */
    public void notifyObservers() {
        for (final Observer o : observers) {
            o.notifyObserver();
        }
        // logger.debug(" n = {} observers notified", observers.size());
    }

    public int getObserversSize() {
        return observers.size();
    }

    public int getObserversInTimeSize() {
        return observersInTime.size();
    }
}
