package tech.harlabs.web.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;
import tech.harlabs.handler.AuthHandler;
import tech.harlabs.web.security.WebSessionHelper;

@Path("/")
public class AuthController {

    private final AuthHandler authHandler;
    private final WebSessionHelper sessionHelper;

    public AuthController(AuthHandler authHandler, WebSessionHelper sessionHelper) {
        this.authHandler = authHandler;
        this.sessionHelper = sessionHelper;
    }

    @CheckedTemplate(basePath = "auth", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    public static class Templates {
        public static native TemplateInstance login(String error, String redirect);
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage(@QueryParam("error") String errorParam, @QueryParam("redirect") String redirect) {
        String error = null;
        if ("forbidden".equalsIgnoreCase(errorParam)) {
            error = "Akses ditolak: Anda tidak memiliki izin untuk halaman tersebut.";
        } else if ("forbidden_owner".equalsIgnoreCase(errorParam)) {
            error = "Akses ditolak: Anda bukan Owner dari tenant ini.";
        }
        return Templates.login(error, redirect);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response processLogin(
        @FormParam("email") String email,
        @FormParam("password") String password,
        @FormParam("redirect") String redirect
    ) {
        var result = authHandler.authenticate(email, password, null);
        if (!result.success()) {
            return Response.ok(Templates.login(result.errorMessage(), redirect)).build();
        }

        String target = (redirect != null && !redirect.isBlank()) ? redirect : result.redirectUrl();
        return Response.seeOther(URI.create(target))
            .cookie(result.sessionCookie())
            .build();
    }

    @GET
    @Path("/logout")
    public Response logout() {
        return Response.seeOther(URI.create("/login"))
            .cookie(authHandler.createLogoutCookie())
            .build();
    }

    @POST
    @Path("/switch-tenant")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response switchTenant(@FormParam("tenantId") UUID tenantId) {
        var currentSession = sessionHelper.getSession();
        if (currentSession == null || tenantId == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        var newCookie = authHandler.switchTenant(currentSession, tenantId);
        if (newCookie != null) {
            return Response.seeOther(URI.create("/owner/dashboard"))
                .cookie(newCookie)
                .build();
        }
        return Response.seeOther(URI.create("/owner/dashboard")).build();
    }
}
