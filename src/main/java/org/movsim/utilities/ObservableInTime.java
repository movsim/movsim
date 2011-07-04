/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.utilities;

// TODO: Auto-generated Javadoc
/**
 * The Interface ObservableInTime.
 */
public interface ObservableInTime {

    /**
     * Register observer.
     * 
     * @param observer
     *            the observer
     */
    void registerObserver(ObserverInTime observer);

    /**
     * Removes the observer.
     * 
     * @param observer
     *            the observer
     */
    void removeObserver(ObserverInTime observer);
}
