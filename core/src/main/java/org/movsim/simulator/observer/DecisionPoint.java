package org.movsim.simulator.observer;

import java.util.HashMap;
import org.movsim.autogen.AlternativeType;
import org.movsim.autogen.DecisionPointType;

public class DecisionPoint {

    private final String roadId;

    private final HashMap<String, Alternative> alternatives = new HashMap<>();

    public DecisionPoint(DecisionPointType configuration) {

        this.roadId = configuration.getRoadId();
        if (configuration.isSetAlternative()) {
            for (AlternativeType alternativeType : configuration.getAlternative()) {
                Alternative alternative = new Alternative(alternativeType.getRoute());
                alternatives.put(alternative.getRoute(), alternative);
            }
        }
    }

    public String getRoadId() {
        return roadId;
    }

    public HashMap<String, Alternative> getAlternatives() {
        return alternatives;
    }

}
