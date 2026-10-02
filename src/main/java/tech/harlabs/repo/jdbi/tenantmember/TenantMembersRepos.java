package tech.harlabs.repo.jdbi.tenantmember;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import tech.harlabs.dto.MemberDetailDto;

@ApplicationScoped
public class TenantMembersRepos {

    private final Jdbi jdbi;

    public TenantMembersRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public TenantMembersEnt insert(TenantMembersEnt member) {
        jdbi.useExtension(TenantMembersDao.class, dao -> dao.insert(member));
        return member;
    }

    public Optional<TenantMembersEnt> findById(UUID id) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.findById(id));
    }

    public Optional<TenantMembersEnt> findByUserAndTenant(UUID userId, UUID tenantId) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.findByUserAndTenant(userId, tenantId));
    }

    public List<TenantMembersEnt> findByTenantId(UUID tenantId) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.findByTenantId(tenantId));
    }

    public List<TenantMembersEnt> findByUserId(UUID userId) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.findByUserId(userId));
    }

    public long countByTenantId(UUID tenantId) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.countByTenantId(tenantId));
    }

    public List<MemberDetailDto> findMemberDetailsByTenantId(UUID tenantId) {
        return jdbi.withExtension(TenantMembersDao.class, dao -> dao.findMemberDetailsByTenantId(tenantId));
    }

    public void updateRole(UUID id, String roles) {
        jdbi.useExtension(TenantMembersDao.class, dao -> dao.updateRole(id, roles, LocalDateTime.now()));
    }

    public void deleteById(UUID id) {
        jdbi.useExtension(TenantMembersDao.class, dao -> dao.deleteById(id));
    }

    public void deleteByUserAndTenant(UUID userId, UUID tenantId) {
        jdbi.useExtension(TenantMembersDao.class, dao -> dao.deleteByUserAndTenant(userId, tenantId));
    }
}
