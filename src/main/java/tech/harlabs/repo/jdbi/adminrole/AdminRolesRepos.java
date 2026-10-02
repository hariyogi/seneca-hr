package tech.harlabs.repo.jdbi.adminrole;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class AdminRolesRepos {

    private final Jdbi jdbi;

    public AdminRolesRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public AdminRolesEnt insert(AdminRolesEnt role) {
        jdbi.useExtension(AdminRolesDao.class, dao -> dao.insert(role));
        return role;
    }

    public Optional<AdminRolesEnt> findById(UUID id) {
        return jdbi.withExtension(AdminRolesDao.class, dao -> dao.findById(id));
    }

    public List<AdminRolesEnt> findByTenantId(UUID tenantId) {
        return jdbi.withExtension(AdminRolesDao.class, dao -> dao.findByTenantId(tenantId));
    }

    public long countByTenantId(UUID tenantId) {
        return jdbi.withExtension(AdminRolesDao.class, dao -> dao.countByTenantId(tenantId));
    }

    public void update(UUID id, String name, String description, String updatedBy) {
        jdbi.useExtension(AdminRolesDao.class, dao -> dao.update(id, name, description, LocalDateTime.now(), updatedBy));
    }

    public void softDelete(UUID id) {
        jdbi.useExtension(AdminRolesDao.class, dao -> dao.softDelete(id, LocalDateTime.now()));
    }

    public void setRolePermissions(UUID roleId, List<String> permissions) {
        jdbi.useTransaction(handle -> {
            var permDao = handle.attach(AdminRolesPermissionsDao.class);
            permDao.deleteByAdminRoleId(roleId);
            if (permissions != null) {
                for (String p : permissions) {
                    if (p != null && !p.isBlank()) {
                        permDao.insert(AdminRolesPermissionsEnt.create(roleId, p));
                    }
                }
            }
        });
    }

    public List<String> getRolePermissions(UUID roleId) {
        return jdbi.withExtension(AdminRolesPermissionsDao.class, dao -> dao.findPermissionCodesByRoleId(roleId));
    }

    public List<String> getUserPermissionsByTenant(UUID userId, UUID tenantId) {
        return jdbi.withExtension(AdminRolesPermissionsDao.class, dao -> dao.findUserPermissionsByTenant(userId, tenantId));
    }
}
