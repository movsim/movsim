package org.movsim.input.model.output;

import java.util.List;

import org.movsim.input.model.output.impl.TravelTimeRouteInput;

public interface TravelTimesInput {

    List<TravelTimeRouteInput> getRoutes();
}
