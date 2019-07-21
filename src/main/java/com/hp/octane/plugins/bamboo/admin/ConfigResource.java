package com.hp.octane.plugins.bamboo.admin;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.hp.octane.integrations.OctaneConfiguration;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.plugins.bamboo.octane.BambooPluginServices;
import com.hp.octane.plugins.bamboo.octane.MqmProject;
import com.hp.octane.plugins.bamboo.octane.utils.Utils;
import com.hp.octane.plugins.bamboo.ui.ConfigureOctaneAction;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/")
@Scanned
public class ConfigResource {

    private UserManager userManager;

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

    public List<SpaceConfiguration> allConfiguration = new LinkedList<>(Arrays.asList(
            new SpaceConfiguration("1", "location1", "clientId1", "clientSecret1", "bambooUser1"),
            new SpaceConfiguration("2", "location2", "clientId2", "clientSecret2", "bambooUser2"),
            new SpaceConfiguration("3", "location3", "clientId3", "clientSecret3", "bambooUser3")));

    @GET
    @Path("/space-config/all")
    public Response SpaceConfiguration(@Context HttpServletRequest request) {
        if (!hasPermissions(request)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        return Response.ok(allConfiguration).build();
    }

    @PUT
    @Path("/space-config/self")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveSpaceConfiguration(@Context HttpServletRequest request, SpaceConfiguration model) {
        if (!hasPermissions(request)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (model.getId() == null || model.getId().isEmpty())
            model.setId(UUID.randomUUID().toString());
        addOrUpdateConfigurationById(model);
        return Response.ok().build();
    }



    @DELETE
    @Path("/space-config/self/{id}")
    public Response deleteConfigurationById(@Context HttpServletRequest request, @PathParam("id") String id) {
        if (!hasPermissions(request)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        for (SpaceConfiguration spaceConfiguration : allConfiguration) {
            if (spaceConfiguration.getId().equals(id)) {
                allConfiguration.remove(spaceConfiguration);
                return Response.ok().build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    public void addOrUpdateConfigurationById(SpaceConfiguration spaceConfiguration) {
        for (int i = 0; i < allConfiguration.size(); i++) {
            if (allConfiguration.get(i).getId().equals(spaceConfiguration.getId())) {
                allConfiguration.set(i, spaceConfiguration);
                return;
            }
        }
        allConfiguration.add(spaceConfiguration);
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SpaceConfiguration {
        @XmlElement
        private String id;
        @XmlElement
        private String location;
        @XmlElement
        private String clientId;
        @XmlElement
        private String clientSecret;
        @XmlElement
        private String bambooUser;

        public SpaceConfiguration() {
        }

        public SpaceConfiguration(String id, String location, String clientId, String clientSecret, String bambooUser) {
            this.id = id;
            this.location = location;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.bambooUser = bambooUser;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public void setClientSecret(int time) {
            this.clientSecret = clientSecret;
        }

        public String getBambooUser() {
            return bambooUser;
        }

        public void setBambooUser(int time) {
            this.bambooUser = bambooUser;
        }
    }
}
