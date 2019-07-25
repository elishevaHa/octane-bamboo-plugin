package com.hp.octane.plugins.bamboo.rest;

import java.util.LinkedList;
import java.util.List;

public class OctaneConnectionCollection {
    private List<OctaneConnection> octaneConnections;

    public List<OctaneConnection> getOctaneConnections() {
        if (octaneConnections == null) {
            octaneConnections = new LinkedList<>();
        }
        return octaneConnections;
    }

    public void setOctaneConnections(List<OctaneConnection> octaneConnections) {
        this.octaneConnections = octaneConnections;
    }

}
