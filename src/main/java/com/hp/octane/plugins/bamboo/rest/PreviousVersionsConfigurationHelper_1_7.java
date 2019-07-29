package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class PreviousVersionsConfigurationHelper_1_7 {
    private static final String PLUGIN_PREFIX = "com.hp.octane.plugins.bamboo.";
    private static final String OCTANE_URL = PLUGIN_PREFIX + "octaneUrl";
    private static final String ACCESS_KEY = PLUGIN_PREFIX + "accessKey";
    private static final String API_SECRET = PLUGIN_PREFIX + "apiSecret";
    private static final String IMPERSONATION_USER = PLUGIN_PREFIX + "userName";
    private static final String UUID = "uuid";


    public static OctaneConnection tryReadConfiguration(PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        boolean configExist = settings.get(OCTANE_URL) != null;
        if (configExist) {
            OctaneConnection octaneConnection = new OctaneConnection();
            if (settings.get(UUID) != null) {
                octaneConnection.setId(String.valueOf(settings.get(UUID)));
            }
            if (settings.get(OCTANE_URL) != null) {
                octaneConnection.setLocation(String.valueOf(settings.get(OCTANE_URL)));
            }
            if (settings.get(ACCESS_KEY) != null) {
                octaneConnection.setClientId(String.valueOf(settings.get(ACCESS_KEY)));
            }
            if (settings.get(API_SECRET) != null) {
                octaneConnection.setClientSecret(String.valueOf(settings.get(API_SECRET)));
            }
            if (settings.get(IMPERSONATION_USER) != null) {
                octaneConnection.setBambooUser(String.valueOf(settings.get(IMPERSONATION_USER)));
            }
            return octaneConnection;
        }
        return null;
    }

    public static void removePreviousVersion(PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        if (settings.get(UUID) != null) {
            settings.remove(UUID);
        }
        if (settings.get(OCTANE_URL) != null) {
            settings.remove(OCTANE_URL);
        }
        if (settings.get(ACCESS_KEY) != null) {
            settings.remove(ACCESS_KEY);
        }
        if (settings.get(API_SECRET) != null) {
            settings.remove(API_SECRET);
        }
        if (settings.get(IMPERSONATION_USER) != null) {
            settings.remove(IMPERSONATION_USER);
        }
    }


}
