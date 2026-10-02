package tech.harlabs.repo.jdbi.tenantmember;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record TenantMembersEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(USER_ID) UUID userId,
    @ColumnName(TENANT_ID) UUID tenantId,
    @ColumnName(ROLES) String roles,
    @ColumnName(INVITED_BY) UUID invitedBy,
    @ColumnName(JOINED_AT) LocalDateTime joinedAt,
    @ColumnName(CREATED_AT) LocalDateTime createdAt,
    @ColumnName(UPDATED_AT) LocalDateTime updatedAt
) {
    public static final String TABLE_NAME = "tenant_members";
    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String TENANT_ID = "tenant_id";
    public static final String ROLES = "roles";
    public static final String INVITED_BY = "invited_by";
    public static final String JOINED_AT = "joined_at";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    public static final String FIELDS = ID + ", " + USER_ID + ", " + TENANT_ID + ", " + ROLES + ", "
        + INVITED_BY + ", " + JOINED_AT + ", " + CREATED_AT + ", " + UPDATED_AT;

    public static final String BINDERS = ":id, :userId, :tenantId, :roles, :invitedBy, :joinedAt, :createdAt, :updatedAt";

    public static TenantMembersEnt create(UUID userId, UUID tenantId, String roles, UUID invitedBy) {
        var now = LocalDateTime.now();
        return new TenantMembersEnt(
            RepoUtil.generateId(),
            userId,
            tenantId,
            roles,
            invitedBy,
            now,
            now,
            now
        );
    }
}
