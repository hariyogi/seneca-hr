package tech.harlabs.web.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

public class WebSessionFilter {

    @Inject
    SessionTokenService tokenService;

    @Inject
    WebSessionHelper sessionHelper;

    /**
     * Memeriksa apakah path merupakan aset statis atau bundle frontend.
     */
    public static boolean isStaticAsset(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/static") || path.startsWith("/web") || path.startsWith("/_web-bundler")
            || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".svg")
            || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")
            || path.endsWith(".woff2") || path.endsWith(".woff") || path.endsWith(".ttf")
            || path.endsWith(".ico") || path.endsWith(".map");
    }

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
        if (isStaticAsset(path)) {
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

    /**
     * Menyematkan header keamanan W3C/OWASP dan mematikan cache (no-cache, no-store) pada seluruh respons dinamis
     * untuk mencegah Back-Forward Cache (bfcache) dan history traversal menampilkan data sensitif setelah logout.
     */
    @ServerResponseFilter
    public void responseFilter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path == null) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        var headers = responseContext.getHeaders();

        // 1. Header Keamanan Standar OWASP
        headers.putSingle("X-Content-Type-Options", "nosniff");
        headers.putSingle("X-Frame-Options", "DENY");
        headers.putSingle("Referrer-Policy", "strict-origin-when-cross-origin");

        // 2. Izinkan caching untuk aset statis agar performa tetap optimal
        if (isStaticAsset(path)) {
            return;
        }

        // 3. Kebijakan Anti-Caching Ketat untuk seluruh halaman HTML & rute dinamis/terproteksi
        headers.putSingle("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        headers.putSingle("Pragma", "no-cache");
        headers.putSingle("Expires", "0");
    }
}

