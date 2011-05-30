package org.movsim.utilities;

public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
}
