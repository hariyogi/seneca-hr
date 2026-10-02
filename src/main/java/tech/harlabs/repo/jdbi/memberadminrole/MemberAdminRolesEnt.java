package tech.harlabs.repo.jdbi.memberadminrole;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record MemberAdminRolesEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(USER_ID) UUID userId,
    @ColumnName(ADMIN_ROLES_ID) UUID adminRolesId,
    @ColumnName(ASSIGN_BY) UUID assignBy,
    @ColumnName(ASSIGN_AT) LocalDateTime assignAt
) {
    public static final String TABLE_NAME = "member_admin_roles";
    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String ADMIN_ROLES_ID = "admin_roles_id";
    public static final String ASSIGN_BY = "assign_by";
    public static final String ASSIGN_AT = "assign_at";

    public static final String FIELDS = ID + ", " + USER_ID + ", " + ADMIN_ROLES_ID + ", " + ASSIGN_BY + ", " + ASSIGN_AT;
    public static final String BINDERS = ":id, :userId, :adminRolesId, :assignBy, :assignAt";

    public static MemberAdminRolesEnt create(UUID userId, UUID adminRolesId, UUID assignBy) {
        return new MemberAdminRolesEnt(
            RepoUtil.generateId(),
            userId,
            adminRolesId,
            assignBy,
            LocalDateTime.now()
        );
    }
}
