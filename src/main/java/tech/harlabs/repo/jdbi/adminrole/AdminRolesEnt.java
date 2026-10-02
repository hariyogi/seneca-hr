package tech.harlabs.repo.jdbi.adminrole;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record AdminRolesEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(TENANT_ID) UUID tenantId,
    @ColumnName(NAME) String name,
    @ColumnName(DESCRIPTION) String description,
    @ColumnName(IS_SYSTEM) boolean isSystem,
    @ColumnName(CREATED_AT) LocalDateTime createdAt,
    @ColumnName(UPDATED_AT) LocalDateTime updatedAt,
    @ColumnName(DELETED_AT) LocalDateTime deletedAt,
    @ColumnName(CREATED_BY) String createdBy,
    @ColumnName(UPDATED_BY) String updatedBy
) {
    public static final String TABLE_NAME = "admin_roles";
    public static final String ID = "id";
    public static final String TENANT_ID = "tenant_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String IS_SYSTEM = "is_system";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";
    public static final String CREATED_BY = "created_by";
    public static final String UPDATED_BY = "updated_by";

    public static final String FIELDS = ID + ", " + TENANT_ID + ", " + NAME + ", " + DESCRIPTION + ", "
        + IS_SYSTEM + ", " + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT + ", "
        + CREATED_BY + ", " + UPDATED_BY;

    public static final String BINDERS = ":id, :tenantId, :name, :description, :isSystem, :createdAt, :updatedAt, :deletedAt, :createdBy, :updatedBy";

    public static AdminRolesEnt create(UUID tenantId, String name, String description, String createdBy) {
        var now = LocalDateTime.now();
        return new AdminRolesEnt(
            RepoUtil.generateId(),
            tenantId,
            name,
            description,
            false,
            now,
            now,
            null,
            createdBy,
            createdBy
        );
    }
}
