package tech.harlabs.web.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

public class WebSessionFilter {

    @Inject
    SessionTokenService tokenService;

    @Inject
    WebSessionHelper sessionHelper;

    @ServerRequestFilter(preMatching = true)
    public Response filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        if (path == null) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Static assets and bundler files bypass session checks
        if (path.startsWith("/static") || path.startsWith("/web") || path.startsWith("/_web-bundler")
            || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".svg")
            || path.endsWith(".png") || path.endsWith(".woff2") || path.endsWith(".ico")) {
            return null;
        }

        // Extract session cookie
        Cookie cookie = ctx.getCookies().get(WebSessionHelper.SESSION_COOKIE_NAME);
        Optional<SenecaUserSession> sessionOpt = Optional.empty();
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
            sessionOpt = tokenService.parseToken(cookie.getValue());
        }

        if (sessionOpt.isPresent()) {
            var session = sessionOpt.get();
            sessionHelper.setSession(session);
            ctx.setProperty(WebSessionHelper.SESSION_PROPERTY, session);

            // If already logged in and visiting /login or landing root /, redirect to appropriate dashboard
            if (path.equals("/login") || path.equals("/")) {
                if (session.isSuper()) {
                    return Response.seeOther(URI.create("/super/dashboard")).build();
                } else if (session.isOwner()) {
                    return Response.seeOther(URI.create("/owner/dashboard")).build();
                } else {
                    return Response.seeOther(URI.create("/owner/dashboard")).build();
                }
            }
        } else {
            // Root landing without login redirects to /login
            if (path.equals("/")) {
                return Response.seeOther(URI.create("/login")).build();
            }
        }

        // Guard /super/**
        if (path.startsWith("/super")) {
            if (sessionOpt.isEmpty()) {
                String target = "/login?redirect=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
                return Response.seeOther(URI.create(target)).build();
            }
            var session = sessionOpt.get();
            if (!session.isSuper()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Akses ditolak: Portal Super hanya dapat diakses oleh sysadmin.")
                    .build();
            }
        }

        // Guard /owner/**
        if (path.startsWith("/owner")) {
            if (sessionOpt.isEmpty()) {
                String target = "/login?redirect=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
                return Response.seeOther(URI.create(target)).build();
            }
            var session = sessionOpt.get();
            if (!session.isOwner() && !session.isSuper()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Akses ditolak: Portal Owner hanya dapat diakses oleh akun dengan role Owner pada tenant ini.")
                    .build();
            }
        }

        return null;
    }
}
