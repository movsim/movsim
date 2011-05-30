package org.movsim.utilities;


public interface ObservableInTime {
    void registerObserver(ObserverInTime observer);
    void removeObserver(ObserverInTime observer);
}
