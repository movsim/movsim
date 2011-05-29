package org.movsim.output;

import java.util.List;

public interface SimObservables {

     SpatioTemporal getSpatioTemporal();
     
     FloatingCars getFloatingCars();
     
     List<LoopDetector> getLoopDetectors();
     
}
