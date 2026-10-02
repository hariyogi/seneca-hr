package tech.harlabs.repo.jdbi.face;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class FaceEmbeddingsRepos {

    private final Jdbi jdbi;

    public FaceEmbeddingsRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public FaceEmbeddingsEnt saveEmbedding(UUID userId, UUID tenantId, String vectorString, String modelName, String modelVersion) {
        return jdbi.inTransaction(handle -> {
            var dao = handle.attach(FaceEmbeddingsDao.class);
            dao.deactivateExisting(userId, tenantId, LocalDateTime.now());
            var ent = FaceEmbeddingsEnt.create(userId, tenantId, vectorString, modelName, modelVersion);
            dao.insert(ent);
            return ent;
        });
    }

    public Optional<FaceEmbeddingsEnt> findById(UUID id) {
        return jdbi.withExtension(FaceEmbeddingsDao.class, dao -> dao.findById(id));
    }

    public Optional<FaceEmbeddingsEnt> findActiveByUserAndTenant(UUID userId, UUID tenantId) {
        return jdbi.withExtension(FaceEmbeddingsDao.class, dao -> dao.findActiveByUserAndTenant(userId, tenantId));
    }

    public List<FaceEmbeddingsEnt> findByTenantId(UUID tenantId) {
        return jdbi.withExtension(FaceEmbeddingsDao.class, dao -> dao.findByTenantId(tenantId));
    }

    public long countActiveByTenantId(UUID tenantId) {
        return jdbi.withExtension(FaceEmbeddingsDao.class, dao -> dao.countActiveByTenantId(tenantId));
    }

    public void softDelete(UUID id) {
        jdbi.useExtension(FaceEmbeddingsDao.class, dao -> dao.softDelete(id, LocalDateTime.now()));
    }
}
