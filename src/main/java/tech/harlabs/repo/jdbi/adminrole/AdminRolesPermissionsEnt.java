package tech.harlabs.repo.jdbi.adminrole;

import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record AdminRolesPermissionsEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(ADMIN_ROLES_ID) UUID adminRolesId,
    @ColumnName(PERMISSION_CODE) String permissionCode
) {
    public static final String TABLE_NAME = "admin_roles_permissions";
    public static final String ID = "id";
    public static final String ADMIN_ROLES_ID = "admin_roles_id";
    public static final String PERMISSION_CODE = "permission_code";

    public static final String FIELDS = ID + ", " + ADMIN_ROLES_ID + ", " + PERMISSION_CODE;
    public static final String BINDERS = ":id, :adminRolesId, :permissionCode";

    public static AdminRolesPermissionsEnt create(UUID adminRolesId, String permissionCode) {
        return new AdminRolesPermissionsEnt(RepoUtil.generateId(), adminRolesId, permissionCode.trim().toUpperCase());
    }
}
