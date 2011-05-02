package org.movsim.output;

public interface LoopDetectorObserver {

    void notify(double time, double flowAvg, double speedAvg, double densityAvg);
    
    
}
