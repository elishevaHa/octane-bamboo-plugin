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

package com.hp.octane.plugins.bamboo.rest;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.security.acegi.acls.BambooPermission;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import com.hp.octane.plugins.bamboo.octane.MqmProject;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import com.hp.octane.plugins.bamboo.ui.ConfigureOctaneAction;
import org.acegisecurity.acls.Permission;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/test")
@Scanned
public class OctaneRestResource {
    private static final Logger log = LoggerFactory.getLogger(OctaneRestResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


   /* @Path("test")
    @GET
    public Response test() throws IOException {
        return Response.ok().build();
    }*/

    @POST
    @Path("/testconnection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConfiguration(@Context HttpServletRequest request, OctaneConnection model) throws IOException {
        return tryToConnect(model);
    }

    private Response tryToConnect(OctaneConnection dto) {
        try {
            String location = dto.getLocation();
            String clientId = dto.getClientId();
            String clientSecret = dto.getClientSecret();
            String bambooUser = dto.getBambooUser();
            if (location == null || location.isEmpty()) {
                return Response.ok().entity("Location URL is required").build();
            }
            if (clientId == null || clientId.isEmpty()) {
                return Response.ok().entity("Client ID is required").build();
            }

            if (clientSecret == null || clientSecret.isEmpty()) {
                return Response.ok().entity("Client Secret is required").build();
            }

            if (bambooUser == null || bambooUser.isEmpty()) {
                return Response.ok().entity("Bamboo user is required").build();
            }
            if (!IsUserExist(bambooUser)) {
                return Response.ok().entity("Bamboo user does not exist").build();
            }

            if (!hasPermission(bambooUser)) {
                return Response.ok().entity("Bamboo user doesn't have enough permissions").build();
            }
            MqmProject mqmProject = Utils.parseUiLocation(location);
            if (mqmProject.hasError()) {
                return Response.ok().entity(mqmProject.getErrorMsg()).build();
            }
            OctaneConfiguration testedOctaneConfiguration = new OctaneConfiguration(UUID.randomUUID().toString(),
                    mqmProject.getLocation(),
                    mqmProject.getSharedSpace());
            testedOctaneConfiguration.setClient(clientId);
            if (ConfigureOctaneAction.PLAIN_PASSWORD.equals(clientSecret)) {
                testedOctaneConfiguration.setSecret(ConfigureOctaneAction.readApiSecretFromSettings());
            } else {
                testedOctaneConfiguration.setSecret(clientSecret);
            }
            OctaneResponse result;

            result = OctaneSDK.testOctaneConfiguration(testedOctaneConfiguration.getUrl(),
                    testedOctaneConfiguration.getSharedSpace(),
                    testedOctaneConfiguration.getClient(),
                    testedOctaneConfiguration.getSecret(),
                    BambooPluginServices.class);

            if (result.getStatus() == HttpStatus.SC_OK) {
                return Response.ok().entity("Success").build();
            } else if (result.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                return Response.ok().entity("You are unauthorized").build();
            } else if (result.getStatus() == HttpStatus.SC_FORBIDDEN) {
                return Response.ok().entity("Connection Forbidden").build();

            } else if (result.getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Response.ok().entity("URL not found").build();
            }
            return Response.ok().entity("Error validating octane config").build();

        } catch (SSLHandshakeException e) {
            log.error("Exception at tryToConnect", e);
            return Response.ok().entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Exception at tryToConnect", e);
            return Response.ok().entity("Error validating octane config").build();
        }
    }

    private boolean hasPermission(String userName) {
        PlanManager planManager = ComponentLocator.getComponent(PlanManager.class);
        List<Chain> plans = planManager.getAllPlans(Chain.class);
        if (plans.isEmpty()) {
            log.info("Server does not have any plan to run");
            return true;
        }
        boolean hasPermission;
        for (Chain chain : plans) {
            hasPermission = isUserHasPermission(BambooPermission.BUILD, userName, chain);
            if (hasPermission) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserHasPermission(Permission permissionType, String user, Chain chain) {
        BambooPermissionManager permissionManager = ComponentLocator.getComponent(BambooPermissionManager.class);
        return permissionManager.hasPermission(user, permissionType, chain);
    }

    private boolean IsUserExist(String userName) {
        BambooUserManager bambooUserManager = ComponentLocator.getComponent(com.atlassian.bamboo.user.BambooUserManager.class);
        BambooUser bambooUser = bambooUserManager.loadUserByUsername(userName);
        if (bambooUser != null) {
            return true;
        }
        return false;
    }
}