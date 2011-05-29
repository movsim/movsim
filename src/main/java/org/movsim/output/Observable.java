package org.movsim.output;


public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
}
