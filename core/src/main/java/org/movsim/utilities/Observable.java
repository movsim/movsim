/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.utilities;

// TODO: Auto-generated Javadoc
/**
 * The Interface Observable.
 */
public interface Observable {

    /**
     * Register observer.
     * 
     * @param observer
     *            the observer
     */
    void registerObserver(Observer observer);

    /**
     * Removes the observer.
     * 
     * @param observer
     *            the observer
     */
    void removeObserver(Observer observer);
}
