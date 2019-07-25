package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.plugins.bamboo.octane.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OctaneConnectionManager {
    private final PluginSettingsFactory settingsFactory;
    public static final String CONFIGURATIONS_LIST = "CONFIRURATIONS_LIST";
    public static final String PLAIN_PASSWORD = "PLAIN_PASSWORD";
    private OctaneConnectionCollection octaneConnectionCollection;

    public OctaneConnectionManager(PluginSettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
        octaneConnectionCollection = new OctaneConnectionCollection();
    }

    public List<OctaneConnection> getConnectionsList() {
           /* PluginSettings settings = this.settingsFactory.createGlobalSettings();
            String confStr = ((String) settings.get(CONFIGURATIONS_LIST));
            octaneConnectionCollection = JsonHelper.deserialize(confStr, OctaneConnectionCollection.class);*/
        return octaneConnectionCollection.getOctaneConnections().stream().peek(c -> c.setClientSecret(PLAIN_PASSWORD)).collect(Collectors.toList());
    }

    public void updateSettings() {
        try {
            //   octaneConnectionCollection.setOctaneConnections(configurations);
            String confStr = JsonHelper.serialize(octaneConnectionCollection);

            PluginSettings settings = this.settingsFactory.createGlobalSettings();
            settings.put(CONFIGURATIONS_LIST, confStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OctaneConnection getConnectionById(String id) {
        return getConnectionsList().stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
    }

    public void addConfiguration(OctaneConnection newConfiguration) {
        octaneConnectionCollection.addConnection(newConfiguration);
        updateSettings();
    }

    public void updateConfiguration(OctaneConnection octaneConnection) {
        octaneConnectionCollection.updateConnection(octaneConnection);
        updateSettings();
        return;
    }

    public void deleteConfiguration(String id) {
        for (int i = 0; i < octaneConnectionCollection.getOctaneConnections().size(); i++) {
            if (octaneConnectionCollection.getOctaneConnections().get(i).getId().equals(id)) {
                octaneConnectionCollection.removeConnection(octaneConnectionCollection.getOctaneConnections().get(i));
                updateSettings();
                return;
            }
        }
    }
}
