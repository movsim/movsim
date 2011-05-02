package org.movsim.output;


public interface LoopDetectorObservable {
    
    // add observer
    void registerObserver(LoopDetectorObserver observer);
    
    // delete observer
    void removeObserver(LoopDetectorObserver observer);
    
    // update observers: public method needed???
    // void notifyObservers();
}
