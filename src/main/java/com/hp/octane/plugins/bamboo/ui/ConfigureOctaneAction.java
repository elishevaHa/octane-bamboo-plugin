/*
 *     Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.octane.plugins.bamboo.ui;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import org.acegisecurity.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ConfigureOctaneAction extends BambooActionSupport {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureOctaneAction.class);

    private static PluginSettingsFactory settingsFactory;

    public static final String PLAIN_PASSWORD = "_plain_password_";
    private String octaneUrl;
    private String accessKey;
    private String apiSecret;
    private String userName;
    private String uuid;

    public ConfigureOctaneAction(PluginSettingsFactory settingsFactory, BambooPermissionManager bambooPermissionManager) {
        this.setBambooPermissionManager(bambooPermissionManager);
        this.settingsFactory = settingsFactory;
        readData();
    }

    public String doEdit() {
        logger.info("edit configuration");
        if (!this.hasAdminPermission()) {
            logger.error("Access Denied, no admin permissions.");
            throw new AccessDeniedException(null);
        }
        return INPUT;
    }

    public String doSave() {
        logger.info("save configuration");
        if (!this.hasAdminPermission()) {
            logger.error("Access Denied, no admin permissions.");
            throw new AccessDeniedException(null);
        }

        PluginSettings settings = settingsFactory.createGlobalSettings();
        settings.put(OctaneConfigurationKeys.OCTANE_URL, octaneUrl);
        settings.put(OctaneConfigurationKeys.ACCESS_KEY, accessKey);
        if (!PLAIN_PASSWORD.equals(apiSecret)) {
            settings.put(OctaneConfigurationKeys.API_SECRET, apiSecret);
        }
        settings.put(OctaneConfigurationKeys.IMPERSONATION_USER, userName);
        addActionMessage("Configuration updated successfully");
        //todo should be changed for multi shared space
        Utils.cud(octaneUrl, uuid, accessKey, apiSecret);
        return SUCCESS;
    }

    public static String readApiSecretFromSettings() {
        PluginSettings settings = settingsFactory.createGlobalSettings();
        return String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        userName = username;
    }

    public String getOctaneUrl() {
        return octaneUrl;
    }

    public void setOctaneUrl(String octaneUrl) {
        this.octaneUrl = octaneUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getApiSecret() {
        return PLAIN_PASSWORD;//always return plain_password  constant in order to not expose the real password to the client
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private void readData() {
        PluginSettings settings = settingsFactory.createGlobalSettings();
        if (settings.get(OctaneConfigurationKeys.UUID) != null) {
            uuid = String.valueOf(settings.get(OctaneConfigurationKeys.UUID));
        } else {
            // generate new UUID
            uuid = UUID.randomUUID().toString();
            settings.put(OctaneConfigurationKeys.UUID, uuid);
        }
        if (settings.get(OctaneConfigurationKeys.OCTANE_URL) != null) {
            octaneUrl = String.valueOf(settings.get(OctaneConfigurationKeys.OCTANE_URL));
        } else {
            octaneUrl = "";
        }
        if (settings.get(OctaneConfigurationKeys.ACCESS_KEY) != null) {
            accessKey = String.valueOf(settings.get(OctaneConfigurationKeys.ACCESS_KEY));
        } else {
            accessKey = "";
        }
        if (settings.get(OctaneConfigurationKeys.API_SECRET) != null) {
            apiSecret = String.valueOf(settings.get(OctaneConfigurationKeys.API_SECRET));
        } else {
            apiSecret = "";
        }
        if (settings.get(OctaneConfigurationKeys.IMPERSONATION_USER) != null) {
            userName = String.valueOf(settings.get(OctaneConfigurationKeys.IMPERSONATION_USER));
        } else {
            userName = "";
        }
    }
}
