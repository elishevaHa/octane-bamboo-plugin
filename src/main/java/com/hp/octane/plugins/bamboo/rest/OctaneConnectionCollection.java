package com.hp.octane.plugins.bamboo.rest;

import java.util.LinkedList;
import java.util.List;

public class OctaneConnectionCollection {
    private List<OctaneConnection> octaneConnections;

    public OctaneConnectionCollection() {
        this.octaneConnections = new LinkedList<>();
    }

    public List<OctaneConnection> getOctaneConnections() {
        return octaneConnections;
    }

    public void setOctaneConnections(List<OctaneConnection> octaneConnections) {
        this.octaneConnections = octaneConnections;
    }

    public void addConnection(OctaneConnection octaneConnection) {
        octaneConnections.add(octaneConnection);
    }

    public boolean removeConnection(OctaneConnection octaneConnection) {
        return octaneConnections.remove(octaneConnection);
    }

    public void updateConnection(OctaneConnection octaneConnection) {
        getConnectionById(octaneConnection.getId())
                .setLocation(octaneConnection.getLocation())
                .setClientId(octaneConnection.getClientId())
                .setClientSecret(octaneConnection.getClientSecret())
                .setBambooUser(octaneConnection.getBambooUser());
    }

    public OctaneConnection getConnectionById(String id) {
        return octaneConnections.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }


}
