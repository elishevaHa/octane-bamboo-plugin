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


import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/")
@Scanned
public class OctaneConnectionRestResource {

    private final OctaneConnectionManager octaneConnectionManager;
    private static final Logger log = LoggerFactory.getLogger(OctaneConnectionRestResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String location = "";
    private String clientId = "";
    private String clientSecret = "";
    private String bambooUser = "";
    private String uuid = "";
    private UserManager userManager;

    public OctaneConnectionRestResource(PluginSettingsFactory settingsFactory) {
        octaneConnectionManager = new OctaneConnectionManager(settingsFactory);
    }

    private UserManager getUserManager() {
        if (userManager == null) {
            this.userManager = ComponentLocator.getComponent(UserManager.class);
        }
        return userManager;
    }

    private boolean hasPermissions(HttpServletRequest request) {
        UserProfile username = getUserManager().getRemoteUser(request);
        return (username != null && getUserManager().isSystemAdmin(username.getUserKey()));
    }


    @PUT
    @Path("/space-config/self")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSpaceConfiguration(@Context HttpServletRequest request, OctaneConnection model) {
        if (!hasPermissions(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        log.info("save configuration");
        Utils.cud("UPDATE", model.getLocation(), model.getId(), model.getClientId(), model.getClientSecret());
        octaneConnectionManager.updateConfiguration(model);
        return Response.ok().build();
    }

    @POST
    @Path("/space-config/self")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSpaceConfiguration(@Context HttpServletRequest request, OctaneConnection model) {
        if (!hasPermissions(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        log.info("save configuration");
        model.setId(UUID.randomUUID().toString());
        try {
            Utils.cud("CREATE", model.getLocation(), model.getId(), model.getClientId(), model.getClientSecret());
            octaneConnectionManager.addConfiguration(model);
        }catch (Exception e)
        {
            Response.status(404).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }


    @DELETE
    @Path("/space-config/self/{id}")
    public Response deleteConnection(@Context HttpServletRequest request, @PathParam("id") String id) {
        try {
            Utils.cud("DELETE", null, id, null, null);
            octaneConnectionManager.deleteConfiguration(id);
        } catch (Exception e) {
            Response.status(Response.Status.NOT_FOUND).entity("configuration not found").build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/space-config/all")
    public Response readAllData(@Context HttpServletRequest request) {
        return Response.ok(octaneConnectionManager.getConnectionsList()).build();
    }


    public String getBambooUser() {
        return bambooUser;
    }

    public void setBambooUser(String username) {
        bambooUser = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}