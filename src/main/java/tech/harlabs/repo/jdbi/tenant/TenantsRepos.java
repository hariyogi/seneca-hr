package tech.harlabs.repo.jdbi.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class TenantsRepos {

    private final Jdbi jdbi;

    public TenantsRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public TenantsEnt insert(TenantsEnt tenant) {
        jdbi.useExtension(TenantsDao.class, dao -> dao.insert(tenant));
        return tenant;
    }

    public Optional<TenantsEnt> findById(UUID id) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findById(id));
    }

    public Optional<TenantsEnt> findByCode(String code) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findByCode(code.trim().toUpperCase()));
    }

    public List<TenantsEnt> findAll(int limit, int offset) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findAll(limit, offset));
    }

    public long countAll() {
        return jdbi.withExtension(TenantsDao.class, TenantsDao::countAll);
    }

    public List<TenantsEnt> findByStatus(String status, int limit, int offset) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findByStatus(status, limit, offset));
    }

    public long countByStatus(String status) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.countByStatus(status));
    }

    public List<TenantsEnt> findOwnedByUserId(UUID userId) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findOwnedByUserId(userId));
    }

    public List<TenantsEnt> findUserTenants(UUID userId) {
        return jdbi.withExtension(TenantsDao.class, dao -> dao.findUserTenants(userId));
    }

    public void updateDetails(UUID id, String name, String legalName, String npwp, String timezone) {
        jdbi.useExtension(TenantsDao.class, dao -> dao.updateDetails(id, name, legalName, npwp, timezone, LocalDateTime.now()));
    }

    public void updateStatus(UUID id, String status) {
        jdbi.useExtension(TenantsDao.class, dao -> dao.updateStatus(id, status, LocalDateTime.now()));
    }

    public void softDelete(UUID id) {
        jdbi.useExtension(TenantsDao.class, dao -> dao.softDelete(id, LocalDateTime.now()));
    }
}
