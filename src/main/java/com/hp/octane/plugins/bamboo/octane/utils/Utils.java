package com.hp.octane.plugins.bamboo.octane.utils;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.sal.api.component.ComponentLocator;
import com.hp.octane.integrations.OctaneClient;
import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import com.hp.octane.plugins.bamboo.octane.MqmProject;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final String PARAM_SHARED_SPACE = "p";

    public static MqmProject parseUiLocation(String uiLocation) {
        try {
            URL url = new URL(uiLocation);
            String location;
            int contextPos = uiLocation.indexOf("/ui");
            if (contextPos < 0) {
                return new MqmProject("Application context not found in URL");
            } else {
                location = uiLocation.substring(0, contextPos);
            }
            List<NameValuePair> params = URLEncodedUtils.parse(url.toURI(), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName().equals(PARAM_SHARED_SPACE)) {
                    String[] sharedSpaceAndWorkspace = param.getValue().split("/");
                    if (sharedSpaceAndWorkspace.length < 1 || StringUtils.isEmpty(sharedSpaceAndWorkspace[0])) {
                        return new MqmProject("Unexpected shared space parameter value");
                    }
                    return new MqmProject(location, sharedSpaceAndWorkspace[0]);
                }
            }
            return new MqmProject("Missing shared space parameter");
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
            return new MqmProject("Invalid URL");
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            return new MqmProject("Invalid URL");
        }
    }

    public static void cud(String octaneUrl, String uuid, String accessKey, String apiSecret) {
        List<OctaneClient> clients = OctaneSDK.getClients();
        MqmProject project = Utils.parseUiLocation(octaneUrl);
        if (clients.isEmpty()) { //clean config->add a new one
            OctaneConfiguration octaneConfiguration = new OctaneConfiguration(uuid,
                    project.getLocation(),
                    project.getSharedSpace());
            octaneConfiguration.setClient(accessKey);
            octaneConfiguration.setSecret(apiSecret);
            OctaneSDK.addClient(octaneConfiguration, BambooPluginServices.class);
        } else { //update existing conf
            OctaneClient client = clients.get(0);
            OctaneConfiguration config = client.getConfigurationService().getCurrentConfiguration();

            config.setSharedSpace(project.getSharedSpace());
            config.setUrl(project.getLocation());
            config.setClient(accessKey);
            config.setSecret(apiSecret);
        }
    }

    public static boolean registerArtifactDefinition(@NotNull Job job, String name, String pattern) {
        if (job == null || StringUtils.isEmpty(name) || StringUtils.isEmpty(pattern)) {
            return false;
        }
        try {
            ArtifactDefinitionManager artifactDefinitionManager = ComponentLocator.getComponent(ArtifactDefinitionManager.class);
            if (artifactDefinitionManager.findArtifactDefinition(job, name) == null) {
                ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "", pattern);
                artifactDefinition.setProducerJob(job);
                artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to registerArtifactDefinition : " + e.getMessage(), e);//Enable XSRF protection
        }
        return false;
    }
}
