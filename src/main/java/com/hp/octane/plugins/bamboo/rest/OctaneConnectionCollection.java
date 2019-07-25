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

    public void addConnection(OctaneConnection octaneConnection)
    {
        octaneConnections.add(octaneConnection);
    }
    public void removeConnection(OctaneConnection octaneConnection)
    {
        octaneConnections.remove(octaneConnection);
    }
    public void updateConnection(OctaneConnection octaneConnection)
    {
        for (int i = 0; octaneConnections != null && i < octaneConnections.size(); i++) {
            if (octaneConnections.get(i).getId().equals(octaneConnection.getId())) {
                octaneConnections.get(i).setLocation(octaneConnection.getLocation());
                octaneConnections.get(i).setClientId(octaneConnection.getClientId());
                octaneConnections.get(i).setClientSecret(octaneConnection.getClientSecret());
                octaneConnections.get(i).setBambooUser(octaneConnection.getBambooUser());
                return;
            }
        }
    }


}
