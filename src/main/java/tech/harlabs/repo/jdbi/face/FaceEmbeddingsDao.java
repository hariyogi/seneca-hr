package tech.harlabs.repo.jdbi.face;

import static tech.harlabs.repo.jdbi.face.FaceEmbeddingsEnt.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(FaceEmbeddingsEnt.class)
public interface FaceEmbeddingsDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ")")
    void insert(@BindMethods FaceEmbeddingsEnt ent);

    @SqlQuery("SELECT " + ID + ", " + USER_ID + ", " + TENANT_ID + ", " + EMBEDDING + "::text AS " + EMBEDDING + ", "
        + MODEL_NAME + ", " + MODEL_VERSION + ", " + IS_ACTIVE + ", " + CONSENT_AT + ", "
        + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT + " "
        + "FROM " + TABLE_NAME + " WHERE " + ID + " = :id AND " + DELETED_AT + " IS NULL")
    Optional<FaceEmbeddingsEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT " + ID + ", " + USER_ID + ", " + TENANT_ID + ", " + EMBEDDING + "::text AS " + EMBEDDING + ", "
        + MODEL_NAME + ", " + MODEL_VERSION + ", " + IS_ACTIVE + ", " + CONSENT_AT + ", "
        + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT + " "
        + "FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId AND " + TENANT_ID + " = :tenantId AND " + IS_ACTIVE + " = true AND " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " DESC LIMIT 1")
    Optional<FaceEmbeddingsEnt> findActiveByUserAndTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT " + ID + ", " + USER_ID + ", " + TENANT_ID + ", " + EMBEDDING + "::text AS " + EMBEDDING + ", "
        + MODEL_NAME + ", " + MODEL_VERSION + ", " + IS_ACTIVE + ", " + CONSENT_AT + ", "
        + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT + " "
        + "FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId AND " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " DESC")
    List<FaceEmbeddingsEnt> findByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId AND " + IS_ACTIVE + " = true AND " + DELETED_AT + " IS NULL")
    long countActiveByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + IS_ACTIVE + " = false, " + UPDATED_AT + " = :updatedAt WHERE " + USER_ID + " = :userId AND " + TENANT_ID + " = :tenantId")
    void deactivateExisting(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + DELETED_AT + " = :deletedAt WHERE " + ID + " = :id")
    void softDelete(@Bind("id") UUID id, @Bind("deletedAt") LocalDateTime deletedAt);
}
