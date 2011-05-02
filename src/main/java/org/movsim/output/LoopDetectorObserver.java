package org.movsim.output;

public interface LoopDetectorObserver {

    // notify or update method
    void update(double time, double flowAvg, double speedAvg, double densityAvg);
    
    
}
