package org.movsim.input.model.output;

import java.util.ArrayList;

public class RouteInput {
    private final ArrayList<String> roadIds;
    private String name;

    public RouteInput() {
        roadIds = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String attributeValue) {
        name = attributeValue;
    }

    public void add(String roadId) {
        roadIds.add(roadId);
    }

    public ArrayList<String> getRoadIds() {
        return roadIds;
    }
}
