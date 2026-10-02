package tech.harlabs.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tech.harlabs.repo.jdbi.adminrole.AdminRolesRepos;
import tech.harlabs.repo.jdbi.tenant.TenantsEnt;
import tech.harlabs.repo.jdbi.tenant.TenantsRepos;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersEnt;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersRepos;
import tech.harlabs.repo.jdbi.user.UsersEnt;
import tech.harlabs.repo.jdbi.user.UsersRepos;
import tech.harlabs.web.security.PasswordUtil;
import tech.harlabs.web.security.SenecaUserSession;
import tech.harlabs.web.security.SessionTokenService;
import tech.harlabs.web.security.WebSessionHelper;

@ApplicationScoped
public class AuthHandler {

    private final UsersRepos usersRepos;
    private final TenantsRepos tenantsRepos;
    private final TenantMembersRepos tenantMembersRepos;
    private final AdminRolesRepos adminRolesRepos;
    private final SessionTokenService tokenService;

    public AuthHandler(
        UsersRepos usersRepos,
        TenantsRepos tenantsRepos,
        TenantMembersRepos tenantMembersRepos,
        AdminRolesRepos adminRolesRepos,
        SessionTokenService tokenService
    ) {
        this.usersRepos = usersRepos;
        this.tenantsRepos = tenantsRepos;
        this.tenantMembersRepos = tenantMembersRepos;
        this.adminRolesRepos = adminRolesRepos;
        this.tokenService = tokenService;
    }

    public record LoginResult(
        boolean success,
        String errorMessage,
        SenecaUserSession session,
        NewCookie sessionCookie,
        String redirectUrl
    ) {}

    public LoginResult authenticate(String email, String rawPassword, UUID requestedTenantId) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return new LoginResult(false, "Email dan password wajib diisi", null, null, null);
        }

        var userOpt = usersRepos.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return new LoginResult(false, "Kredensial login tidak valid", null, null, null);
        }

        var user = userOpt.get();
        if (!user.isActive()) {
            return new LoginResult(false, "Akun ini telah dinonaktifkan. Hubungi administrator.", null, null, null);
        }

        if (!PasswordUtil.verify(rawPassword, user.passwordHash())) {
            return new LoginResult(false, "Kredensial login tidak valid", null, null, null);
        }

        // Update last login
        usersRepos.updateLastLogin(user.id());

        // Check if super user (sysadmin)
        boolean isSuper = "sysadmin@seneca.local".equalsIgnoreCase(user.email());

        UUID activeTenantId = null;
        String activeTenantName = null;
        String activeRole = isSuper ? "super" : "employee";
        List<String> permissions = Collections.emptyList();

        var userTenants = tenantsRepos.findUserTenants(user.id());
        if (!userTenants.isEmpty()) {
            TenantsEnt selectedTenant = userTenants.getFirst();
            if (requestedTenantId != null) {
                for (TenantsEnt t : userTenants) {
                    if (t.id().equals(requestedTenantId)) {
                        selectedTenant = t;
                        break;
                    }
                }
            }
            activeTenantId = selectedTenant.id();
            activeTenantName = selectedTenant.name();

            var membershipOpt = tenantMembersRepos.findByUserAndTenant(user.id(), activeTenantId);
            if (membershipOpt.isPresent()) {
                activeRole = membershipOpt.get().roles();
            }

            if ("admin".equalsIgnoreCase(activeRole)) {
                permissions = adminRolesRepos.getUserPermissionsByTenant(user.id(), activeTenantId);
            }
        }

        var session = new SenecaUserSession(
            user.id(),
            user.email(),
            user.fullName(),
            isSuper,
            activeTenantId,
            activeTenantName,
            activeRole,
            permissions
        );

        String token = tokenService.createToken(session);
        var cookie = new NewCookie.Builder(WebSessionHelper.SESSION_COOKIE_NAME)
            .value(token)
            .path("/")
            .httpOnly(true)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(86400) // 1 day
            .build();

        String redirectUrl = isSuper ? "/super/dashboard" : "/owner/dashboard";
        return new LoginResult(true, null, session, cookie, redirectUrl);
    }

    public NewCookie switchTenant(SenecaUserSession currentSession, UUID newTenantId) {
        var membershipOpt = tenantMembersRepos.findByUserAndTenant(currentSession.userId(), newTenantId);
        var tenantOpt = tenantsRepos.findById(newTenantId);
        if (membershipOpt.isEmpty() || tenantOpt.isEmpty()) {
            return null;
        }

        String newRole = membershipOpt.get().roles();
        List<String> permissions = Collections.emptyList();
        if ("admin".equalsIgnoreCase(newRole)) {
            permissions = adminRolesRepos.getUserPermissionsByTenant(currentSession.userId(), newTenantId);
        }

        var updatedSession = new SenecaUserSession(
            currentSession.userId(),
            currentSession.email(),
            currentSession.fullName(),
            currentSession.isSuper(),
            newTenantId,
            tenantOpt.get().name(),
            newRole,
            permissions
        );

        String token = tokenService.createToken(updatedSession);
        return new NewCookie.Builder(WebSessionHelper.SESSION_COOKIE_NAME)
            .value(token)
            .path("/")
            .httpOnly(true)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(86400)
            .build();
    }

    public NewCookie createLogoutCookie() {
        return new NewCookie.Builder(WebSessionHelper.SESSION_COOKIE_NAME)
            .value("")
            .path("/")
            .httpOnly(true)
            .sameSite(NewCookie.SameSite.LAX)
            .maxAge(0)
            .expiry(new Date(0))
            .build();
    }
}
