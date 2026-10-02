package tech.harlabs.web.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveResourceInfo;

@RequestScoped
public class WebSessionHelper {

    public static final String SESSION_COOKIE_NAME = "seneca_session";
    public static final String SESSION_PROPERTY = "seneca_session_data";

    private SenecaUserSession currentSession;

    public void setSession(SenecaUserSession session) {
        this.currentSession = session;
    }

    public SenecaUserSession getSession() {
        return this.currentSession;
    }

    public boolean isAuthenticated() {
        return this.currentSession != null;
    }
}
