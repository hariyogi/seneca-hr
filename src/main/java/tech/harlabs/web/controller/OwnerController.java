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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import tech.harlabs.dto.MemberDetailDto;
import tech.harlabs.handler.OwnerHandler;
import tech.harlabs.repo.jdbi.adminrole.AdminRolesEnt;
import tech.harlabs.repo.jdbi.face.FaceEmbeddingsEnt;
import tech.harlabs.repo.jdbi.tenant.TenantsEnt;
import tech.harlabs.web.security.SenecaUserSession;
import tech.harlabs.web.security.WebSessionHelper;

@Path("/owner")
public class OwnerController {

    private final OwnerHandler ownerHandler;
    private final WebSessionHelper sessionHelper;

    public static final List<String> STANDARD_PERMISSIONS = List.of(
        "EMPLOYEE_VIEW",
        "EMPLOYEE_MANAGE",
        "ATTENDANCE_VIEW",
        "ATTENDANCE_MANAGE",
        "SCHEDULE_MANAGE",
        "LOCATION_MANAGE",
        "AUDIT_VIEW"
    );

    public OwnerController(OwnerHandler ownerHandler, WebSessionHelper sessionHelper) {
        this.ownerHandler = ownerHandler;
        this.sessionHelper = sessionHelper;
    }

    @CheckedTemplate(basePath = "owner", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    public static class Templates {
        public static native TemplateInstance dashboard(SenecaUserSession session, TenantsEnt tenant, List<TenantsEnt> ownedTenants, long memberCount, long adminRoleCount, long faceEmbeddingCount);
        public static native TemplateInstance members(SenecaUserSession session, TenantsEnt tenant, List<TenantsEnt> ownedTenants, List<MemberDetailDto> members, List<AdminRolesEnt> availableRoles);
        public static native TemplateInstance roles(SenecaUserSession session, TenantsEnt tenant, List<TenantsEnt> ownedTenants, List<AdminRolesEnt> roles, List<String> standardPermissions);
        public static native TemplateInstance faceEmbeddings(SenecaUserSession session, TenantsEnt tenant, List<TenantsEnt> ownedTenants, List<FaceEmbeddingsEnt> embeddings, long activeCount);
    }

    private TenantsEnt resolveCurrentTenant(SenecaUserSession session) {
        if (session.currentTenantId() != null) {
            var t = ownerHandler.getTenantById(session.currentTenantId());
            if (t.isPresent()) {
                return t.get();
            }
        }
        var list = ownerHandler.getOwnedTenants(session.userId());
        if (!list.isEmpty()) {
            return list.getFirst();
        }
        // Fallback default
        return TenantsEnt.create("DEFAULT", "Default Company", "PT Default", "00.000.000.0-000.000", "Asia/Jakarta");
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance dashboard() {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        var owned = ownerHandler.getOwnedTenants(session.userId());
        long memberCount = ownerHandler.listMembers(tenant.id()).size();
        long adminRoleCount = ownerHandler.listAdminRoles(tenant.id()).size();
        long faceCount = ownerHandler.countActiveFaceEmbeddings(tenant.id());

        return Templates.dashboard(session, tenant, owned, memberCount, adminRoleCount, faceCount);
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
        var session = sessionHelper.getSession();
        ownerHandler.createTenant(session.userId(), code, name, legalName, npwp, timezone);
        return Response.seeOther(URI.create("/owner/dashboard")).build();
    }

    @POST
    @Path("/tenants/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTenant(
        @PathParam("id") UUID id,
        @FormParam("name") String name,
        @FormParam("legalName") String legalName,
        @FormParam("npwp") String npwp,
        @FormParam("timezone") String timezone
    ) {
        ownerHandler.updateTenantDetails(id, name, legalName, npwp, timezone);
        return Response.seeOther(URI.create("/owner/dashboard")).build();
    }

    @GET
    @Path("/members")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance members() {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        var owned = ownerHandler.getOwnedTenants(session.userId());
        var memberList = ownerHandler.listMembers(tenant.id());
        var availableRoles = ownerHandler.listAdminRoles(tenant.id());

        return Templates.members(session, tenant, owned, memberList, availableRoles);
    }

    @POST
    @Path("/members")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addMember(
        @FormParam("email") String email,
        @FormParam("fullName") String fullName,
        @FormParam("phone") String phone,
        @FormParam("role") String role
    ) {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        ownerHandler.addMember(tenant.id(), email, fullName, phone, role, session.userId());
        return Response.seeOther(URI.create("/owner/members")).build();
    }

    @POST
    @Path("/members/{id}/role")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateMemberRole(@PathParam("id") UUID id, @FormParam("role") String role) {
        ownerHandler.updateMemberRole(id, role);
        return Response.seeOther(URI.create("/owner/members")).build();
    }

    @POST
    @Path("/members/{id}/delete")
    public Response deleteMember(@PathParam("id") UUID id) {
        ownerHandler.removeMember(id);
        return Response.seeOther(URI.create("/owner/members")).build();
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance roles() {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        var owned = ownerHandler.getOwnedTenants(session.userId());
        var roleList = ownerHandler.listAdminRoles(tenant.id());

        return Templates.roles(session, tenant, owned, roleList, STANDARD_PERMISSIONS);
    }

    @POST
    @Path("/roles")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createRole(
        @FormParam("name") String name,
        @FormParam("description") String description,
        @FormParam("permissions") List<String> permissions
    ) {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        ownerHandler.createAdminRole(tenant.id(), name, description, permissions, session.fullName());
        return Response.seeOther(URI.create("/owner/roles")).build();
    }

    @POST
    @Path("/roles/{id}/delete")
    public Response deleteRole(@PathParam("id") UUID id) {
        ownerHandler.deleteAdminRole(id);
        return Response.seeOther(URI.create("/owner/roles")).build();
    }

    @GET
    @Path("/face-embeddings")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance faceEmbeddings() {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);
        var owned = ownerHandler.getOwnedTenants(session.userId());
        var list = ownerHandler.listFaceEmbeddings(tenant.id());
        long activeCount = ownerHandler.countActiveFaceEmbeddings(tenant.id());

        return Templates.faceEmbeddings(session, tenant, owned, list, activeCount);
    }

    @POST
    @Path("/face-embeddings")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response enrollFaceEmbedding(
        @FormParam("userId") UUID userId,
        @FormParam("modelName") String modelName,
        @FormParam("modelVersion") String modelVersion
    ) {
        var session = sessionHelper.getSession();
        var tenant = resolveCurrentTenant(session);

        // Generate synthetic normalized 512-dimension vector for testing pgvector
        var random = new Random();
        var sb = new StringBuilder("[");
        for (int i = 0; i < 512; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "%.5f", (random.nextGaussian() / 10.0)));
        }
        sb.append("]");

        // Save via handler
        ownerHandler.saveFaceEmbedding(userId, tenant.id(), sb.toString(), modelName, modelVersion);

        return Response.seeOther(URI.create("/owner/face-embeddings")).build();
    }
}
