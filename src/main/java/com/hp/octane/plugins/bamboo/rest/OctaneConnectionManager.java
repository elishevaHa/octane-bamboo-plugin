package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import com.hp.octane.plugins.bamboo.octane.MqmProject;
import com.hp.octane.plugins.bamboo.octane.utils.JsonHelper;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class OctaneConnectionManager {

    private static final Logger logger = LogManager.getLogger(OctaneConnectionManager.class);
    private PluginSettingsFactory settingsFactory;
    private static final String PLUGIN_PREFIX = "com.hp.octane.plugins.bamboo.";
    public static final String CONFIGURATIONS_LIST = PLUGIN_PREFIX + "CONFIGURATIONS_LIST";
    public static final String PLAIN_PASSWORD = "___PLAIN_PASSWORD____";
    private OctaneConnectionCollection octaneConnectionCollection;
    private static OctaneConnectionManager instance = new OctaneConnectionManager();

    public static OctaneConnectionManager getInstance() {
        return instance;
    }

    private OctaneConnectionManager() {
    }

    public void init(PluginSettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
        initSdkClients();
    }

    public OctaneConnectionCollection getOctaneConnections() {
        return octaneConnectionCollection;
    }

    private void saveSettings() {
        try {
            String confStr = JsonHelper.serialize(octaneConnectionCollection);
            PluginSettings settings = this.settingsFactory.createGlobalSettings();
            settings.put(CONFIGURATIONS_LIST, confStr);
        } catch (IOException e) {
            logger.error("Failed to saveSettings : " + e.getMessage());
        }
    }

    public OctaneConnection getConnectionById(String id) {
        return octaneConnectionCollection.getConnectionById(id);
    }

    public void addConfiguration(OctaneConnection newConfiguration) {
        addSdkClient(newConfiguration);
        octaneConnectionCollection.addConnection(newConfiguration);
        saveSettings();
    }

    public void updateConfiguration(OctaneConnection octaneConnection) {
        updateClientInSDK(octaneConnection.getLocation(), octaneConnection.getId(), octaneConnection.getClientId(), octaneConnection.getClientSecret());
        octaneConnectionCollection.updateConnection(octaneConnection);
        saveSettings();
        return;
    }

    public boolean deleteConfiguration(String id) {
        removeClientFromSDK(id);
        boolean removed = octaneConnectionCollection.removeConnection(getConnectionById(id));
        saveSettings();

        return removed;
    }

    public void replacePlainPasswordIfRequired(OctaneConnection octaneConnection) {
        if (octaneConnection.getClientSecret().equals(OctaneConnectionManager.PLAIN_PASSWORD)) {
            octaneConnection.setClientSecret(getConnectionById(octaneConnection.getId()).getClientSecret());
        }
    }

    private void addSdkClient(OctaneConnection configuration) {
        MqmProject project = Utils.parseUiLocation(configuration.getLocation());
        OctaneConfiguration octaneConfiguration = new OctaneConfiguration(configuration.getId(),
                project.getLocation(),
                project.getSharedSpace());
        octaneConfiguration.setClient(configuration.getClientId());
        octaneConfiguration.setSecret(configuration.getClientSecret());
        OctaneSDK.addClient(octaneConfiguration, BambooPluginServices.class);
    }

    private void updateClientInSDK(String octaneUrl, String uuid, String accessKey, String apiSecret) {
        List<OctaneClient> clients = OctaneSDK.getClients();
        MqmProject project = Utils.parseUiLocation(octaneUrl);
        OctaneClient currentClient = clients.stream().filter(c -> c.getInstanceId().equals(uuid)).findFirst().orElse(null);
        if (currentClient == null) {
            throw new RuntimeException("Configuration not found ");
        }
        OctaneConfiguration config = currentClient.getConfigurationService().getCurrentConfiguration();
        config.setSharedSpace(project.getSharedSpace());
        config.setUrl(project.getLocation());
        config.setClient(accessKey);
        config.setSecret(apiSecret);
    }

    private void removeClientFromSDK(String uuid) {
        OctaneClient currentClient = OctaneSDK.getClients().stream()
                .filter(c -> c.getInstanceId().equals(uuid))
                .findFirst().orElse(null);
        if (currentClient == null) {
            throw new RuntimeException("Configuration not found ");
        }
        OctaneSDK.removeClient(currentClient);
    }

    private void initSdkClients() {
        logger.info("");
        logger.info("");
        logger.info("***********************************************************************************");
        logger.info("****************************Enabling plugin - init SDK Clients*********************");
        logger.info("***********************************************************************************");

        try {
            PluginSettings settings = settingsFactory.createGlobalSettings();
            if (settings.get(OctaneConnectionManager.CONFIGURATIONS_LIST) == null) {
                //TODO - upgrade from previous version
                octaneConnectionCollection = new OctaneConnectionCollection();
            } else {
                String confStr = ((String) settings.get(OctaneConnectionManager.CONFIGURATIONS_LIST));
                octaneConnectionCollection = JsonHelper.deserialize(confStr, OctaneConnectionCollection.class);
            }

            for (OctaneConnection c : octaneConnectionCollection.getOctaneConnections()) {
                try {
                    addSdkClient(c);
                } catch (Exception e) {
                    logger.info(String.format("Failed to add client '%s' to sdk : %s", c.getId(), e.getMessage()));
                }
            }
        } catch (IOException e) {
            logger.error("Failed to initSdkClients : " + e.getMessage(), e);
        }
    }

    public void removeClients() {
        logger.info("Disabling plugin - removing SDK clients");
        OctaneSDK.getClients().forEach(c -> OctaneSDK.removeClient(c));
    }

}
