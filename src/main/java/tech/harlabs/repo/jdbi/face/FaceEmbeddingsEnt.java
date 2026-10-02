package tech.harlabs.repo.jdbi.face;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record FaceEmbeddingsEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(USER_ID) UUID userId,
    @ColumnName(TENANT_ID) UUID tenantId,
    @ColumnName(EMBEDDING) String embedding,
    @ColumnName(MODEL_NAME) String modelName,
    @ColumnName(MODEL_VERSION) String modelVersion,
    @ColumnName(IS_ACTIVE) boolean isActive,
    @ColumnName(CONSENT_AT) LocalDateTime consentAt,
    @ColumnName(CREATED_AT) LocalDateTime createdAt,
    @ColumnName(UPDATED_AT) LocalDateTime updatedAt,
    @ColumnName(DELETED_AT) LocalDateTime deletedAt
) {
    public static final String TABLE_NAME = "face_embeddings";
    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String TENANT_ID = "tenant_id";
    public static final String EMBEDDING = "embedding";
    public static final String MODEL_NAME = "model_name";
    public static final String MODEL_VERSION = "model_version";
    public static final String IS_ACTIVE = "is_active";
    public static final String CONSENT_AT = "consent_at";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";

    public static final String FIELDS = ID + ", " + USER_ID + ", " + TENANT_ID + ", " + EMBEDDING + ", "
        + MODEL_NAME + ", " + MODEL_VERSION + ", " + IS_ACTIVE + ", " + CONSENT_AT + ", "
        + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT;

    public static final String BINDERS = ":id, :userId, :tenantId, :embedding::vector, :modelName, :modelVersion, :isActive, :consentAt, :createdAt, :updatedAt, :deletedAt";

    public static FaceEmbeddingsEnt create(UUID userId, UUID tenantId, String vectorString, String modelName, String modelVersion) {
        var now = LocalDateTime.now();
        return new FaceEmbeddingsEnt(
            RepoUtil.generateId(),
            userId,
            tenantId,
            vectorString,
            modelName,
            modelVersion,
            true,
            now,
            now,
            now,
            null
        );
    }
}
