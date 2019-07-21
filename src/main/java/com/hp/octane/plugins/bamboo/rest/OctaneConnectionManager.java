package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.plugins.bamboo.octane.utils.JsonHelper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OctaneConnectionManager {
    private final PluginSettingsFactory settingsFactory;
    private static final String CONFIGURATIONS_LIST = "CONFIRURATIONS_LIST";

    public OctaneConnectionManager(PluginSettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
    }

    public List<OctaneConnection> getConnectionsList() {
        try {
            PluginSettings settings = this.settingsFactory.createGlobalSettings();
            OctaneConnectionCollection octaneConnectionCollection = new OctaneConnectionCollection();
            if (settings.get(CONFIGURATIONS_LIST) == null) {
                settings.put(CONFIGURATIONS_LIST, JsonHelper.serialize(octaneConnectionCollection));
            }
            String confStr = ((String) settings.get(CONFIGURATIONS_LIST));
            if (confStr == null || confStr.isEmpty()) {
                octaneConnectionCollection.setOctaneConnections(new LinkedList<>());
                confStr = JsonHelper.serialize(octaneConnectionCollection);
                settings.put(CONFIGURATIONS_LIST, confStr);
            } else {
                octaneConnectionCollection = JsonHelper.deserialize(confStr, OctaneConnectionCollection.class);
            }
            return octaneConnectionCollection.getOctaneConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setConnectionsList(List<OctaneConnection> configurations) {
        try {
            OctaneConnectionCollection octaneConnectionCollection = new OctaneConnectionCollection();
            octaneConnectionCollection.setOctaneConnections(configurations);
            String confStr = JsonHelper.serialize(octaneConnectionCollection);

            PluginSettings settings = this.settingsFactory.createGlobalSettings();
            settings.put(CONFIGURATIONS_LIST, confStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addConfiguration(OctaneConnection newConfiguration) {
        List<OctaneConnection> configurations = getConnectionsList();
        //cofigurations= cofigurations.stream().map(c->c.getId().equals(newConfiguration.getId())?newConfiguration:c).collect(Collectors.toList());;
        configurations.add(newConfiguration);
        setConnectionsList(configurations);
    }

    public void updateConfiguration(OctaneConnection octaneConnection) {
        List<OctaneConnection> configurations = getConnectionsList();
        for (int i = 0; configurations != null && i < configurations.size(); i++) {
            if (configurations.get(i).getId().equals(octaneConnection.getId())) {
                configurations.get(i).setLocation(octaneConnection.getLocation());
                configurations.get(i).setClientId(octaneConnection.getClientId());
                configurations.get(i).setClientSecret(octaneConnection.getClientSecret());
                configurations.get(i).setBambooUser(octaneConnection.getBambooUser());
                setConnectionsList(configurations);
                return;
            }
        }
    }

    public void deleteConfiguration(String id) {
        List<OctaneConnection> configurations = getConnectionsList();
        for (int i = 0; i < configurations.size(); i++) {
            if (configurations.get(i).getId().equals(id)) {
                configurations.remove(configurations.get(i));
                setConnectionsList(configurations);
                return;
            }
        }
    }
}
