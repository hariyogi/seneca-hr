package tech.harlabs.web.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import tech.harlabs.handler.SuperHandler;
import tech.harlabs.repo.jdbi.tenant.TenantsEnt;
import tech.harlabs.repo.jdbi.user.UsersEnt;
import tech.harlabs.web.security.SenecaUserSession;
import tech.harlabs.web.security.WebSessionHelper;

@Path("/super")
public class SuperController {

    private final SuperHandler superHandler;
    private final WebSessionHelper sessionHelper;

    public SuperController(SuperHandler superHandler, WebSessionHelper sessionHelper) {
        this.superHandler = superHandler;
        this.sessionHelper = sessionHelper;
    }

    @CheckedTemplate(basePath = "super", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    public static class Templates {
        public static native TemplateInstance dashboard(SenecaUserSession session, SuperHandler.SystemMetrics metrics, List<TenantsEnt> recentTenants);
        public static native TemplateInstance tenants(SenecaUserSession session, List<TenantsEnt> tenants, String statusFilter);
        public static native TemplateInstance users(SenecaUserSession session, List<UsersEnt> users, String query);
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance dashboard() {
        var session = sessionHelper.getSession();
        var metrics = superHandler.getSystemMetrics();
        var recentTenants = superHandler.listTenants("ALL", 10, 0);
        return Templates.dashboard(session, metrics, recentTenants);
    }

    @GET
    @Path("/tenants")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance tenants(@QueryParam("status") String status) {
        var session = sessionHelper.getSession();
        String filter = (status != null && !status.isBlank()) ? status.toUpperCase() : "ALL";
        var list = superHandler.listTenants(filter, 100, 0);
        return Templates.tenants(session, list, filter);
    }

    @POST
    @Path("/tenants")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTenant(
        @FormParam("code") String code,
        @FormParam("name") String name,
        @FormParam("legalName") String legalName,
        @FormParam("npwp") String npwp,
        @FormParam("timezone") String timezone
    ) {
        superHandler.createTenant(code, name, legalName, npwp, timezone, null);
        return Response.seeOther(URI.create("/super/tenants")).build();
    }

    @POST
    @Path("/tenants/{id}/status")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTenantStatus(@PathParam("id") UUID id, @FormParam("status") String status) {
        superHandler.updateTenantStatus(id, status);
        return Response.seeOther(URI.create("/super/tenants")).build();
    }

    @GET
    @Path("/users")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance users(@QueryParam("q") String query) {
        var session = sessionHelper.getSession();
        var list = superHandler.listUsers(query, 100, 0);
        return Templates.users(session, list, query);
    }

    @POST
    @Path("/users/{id}/toggle-status")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response toggleUserStatus(@PathParam("id") UUID id, @FormParam("isActive") boolean isActive) {
        superHandler.toggleUserActiveStatus(id, isActive);
        return Response.seeOther(URI.create("/super/users")).build();
    }
}
