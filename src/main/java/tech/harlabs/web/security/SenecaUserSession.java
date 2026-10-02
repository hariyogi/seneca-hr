package tech.harlabs.web.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record SenecaUserSession(
    UUID userId,
    String email,
    String fullName,
    boolean isSuper,
    UUID currentTenantId,
    String currentTenantName,
    String currentRole,
    List<String> permissions
) implements Serializable {

    public SenecaUserSession {
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
    }

    public boolean hasPermission(String permissionCode) {
        if (isSuper || "owner".equalsIgnoreCase(currentRole)) {
            return true;
        }
        return permissions != null && permissions.contains(permissionCode.toUpperCase());
    }

    public boolean isOwner() {
        return "owner".equalsIgnoreCase(currentRole);
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    public boolean isEmployee() {
        return "employee".equalsIgnoreCase(currentRole);
    }
}
