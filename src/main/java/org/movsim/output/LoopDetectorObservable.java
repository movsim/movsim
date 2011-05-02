package org.movsim.output;


public interface LoopDetectorObservable {
    void registerObserver(LoopDetectorObserver observer);
    void removeObserver(LoopDetectorObserver observer);
    void notifyObservers();
}
