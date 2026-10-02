package tech.harlabs.repo.jdbi.memberadminrole;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class MemberAdminRolesRepos {

    private final Jdbi jdbi;

    public MemberAdminRolesRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public MemberAdminRolesEnt assignRole(UUID userId, UUID adminRoleId, UUID assignBy) {
        var ent = MemberAdminRolesEnt.create(userId, adminRoleId, assignBy);
        jdbi.useExtension(MemberAdminRolesDao.class, dao -> dao.insert(ent));
        return ent;
    }

    public Optional<MemberAdminRolesEnt> findById(UUID id) {
        return jdbi.withExtension(MemberAdminRolesDao.class, dao -> dao.findById(id));
    }

    public List<MemberAdminRolesEnt> findByUserAndTenant(UUID userId, UUID tenantId) {
        return jdbi.withExtension(MemberAdminRolesDao.class, dao -> dao.findByUserAndTenant(userId, tenantId));
    }

    public List<UUID> findRoleIdsByUserAndTenant(UUID userId, UUID tenantId) {
        return jdbi.withExtension(MemberAdminRolesDao.class, dao -> dao.findRoleIdsByUserAndTenant(userId, tenantId));
    }

    public void removeRole(UUID userId, UUID adminRoleId) {
        jdbi.useExtension(MemberAdminRolesDao.class, dao -> dao.deleteByUserAndRole(userId, adminRoleId));
    }

    public void syncUserRolesInTenant(UUID userId, UUID tenantId, List<UUID> roleIds, UUID assignBy) {
        jdbi.useTransaction(handle -> {
            var dao = handle.attach(MemberAdminRolesDao.class);
            dao.deleteAllUserRolesInTenant(userId, tenantId);
            if (roleIds != null) {
                for (UUID roleId : roleIds) {
                    dao.insert(MemberAdminRolesEnt.create(userId, roleId, assignBy));
                }
            }
        });
    }
}
