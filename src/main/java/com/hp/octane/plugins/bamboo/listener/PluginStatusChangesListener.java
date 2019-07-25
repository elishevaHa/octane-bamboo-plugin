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
package com.hp.octane.plugins.bamboo.listener;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import com.hp.octane.plugins.bamboo.octane.MqmProject;
import com.hp.octane.plugins.bamboo.octane.utils.JsonHelper;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import com.hp.octane.plugins.bamboo.rest.OctaneConnection;
import com.hp.octane.plugins.bamboo.rest.OctaneConnectionCollection;
import com.hp.octane.plugins.bamboo.rest.OctaneConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

public class PluginStatusChangesListener implements InitializingBean, DisposableBean {
	private static final Logger logger = LoggerFactory.getLogger(PluginStatusChangesListener.class);

	private final PluginSettingsFactory settingsFactory;

	public PluginStatusChangesListener(PluginSettingsFactory settingsFactory) {
		this.settingsFactory = settingsFactory;
	}

	@Override
	public void destroy() throws Exception {
		logger.info("Destroying plugin - removing SDK clients");
		List<OctaneClient> clients = OctaneSDK.getClients();
		for (OctaneClient client : clients) {
			OctaneSDK.removeClient(client);
		}
	}

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Init ALM Octane plugin - creating SDK clients");
        PluginSettings settings = settingsFactory.createGlobalSettings();
        OctaneConnectionCollection octaneConnectionCollection = new OctaneConnectionCollection();
        if (settings.get(OctaneConnectionManager.CONFIGURATIONS_LIST) == null) {
            settings.put(OctaneConnectionManager.CONFIGURATIONS_LIST, JsonHelper.serialize(octaneConnectionCollection));
        }
        String confStr = ((String) settings.get(OctaneConnectionManager.CONFIGURATIONS_LIST));
        octaneConnectionCollection = JsonHelper.deserialize(confStr, OctaneConnectionCollection.class);


        for (OctaneConnection c : octaneConnectionCollection.getOctaneConnections()) {
            try {
                MqmProject project = Utils.parseUiLocation(c.getLocation());
                OctaneConfiguration octaneConfiguration = new OctaneConfiguration(c.getId(),
                        project.getLocation(),
                        project.getSharedSpace());
                octaneConfiguration.setClient(c.getClientId());
                octaneConfiguration.setSecret(c.getClientSecret());
                OctaneSDK.addClient(octaneConfiguration, BambooPluginServices.class);
            } catch (Exception e) {
                logger.info("Exception cannot add client to sdk " + e.getMessage());
            }
        }
    }
}
