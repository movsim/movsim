package org.movsim.utilities;



public interface Observable {
    void registerObserver(ObserverInTime observer);
    void removeObserver(ObserverInTime observer);
}
