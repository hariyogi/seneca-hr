package tech.harlabs.repo.jdbi.adminrole;

import static tech.harlabs.repo.jdbi.adminrole.AdminRolesPermissionsEnt.*;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(AdminRolesPermissionsEnt.class)
public interface AdminRolesPermissionsDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ") ON CONFLICT (" + ADMIN_ROLES_ID + ", " + PERMISSION_CODE + ") DO NOTHING")
    void insert(@BindMethods AdminRolesPermissionsEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ADMIN_ROLES_ID + " = :adminRolesId")
    List<AdminRolesPermissionsEnt> findByAdminRoleId(@Bind("adminRolesId") UUID adminRolesId);

    @SqlQuery("SELECT " + PERMISSION_CODE + " FROM " + TABLE_NAME + " WHERE " + ADMIN_ROLES_ID + " = :adminRolesId")
    List<String> findPermissionCodesByRoleId(@Bind("adminRolesId") UUID adminRolesId);

    @SqlQuery("SELECT DISTINCT arp." + PERMISSION_CODE + " FROM " + TABLE_NAME + " arp "
        + "JOIN member_admin_roles mar ON mar.admin_roles_id = arp.admin_roles_id "
        + "JOIN admin_roles ar ON ar.id = arp.admin_roles_id "
        + "WHERE mar.user_id = :userId AND ar.tenant_id = :tenantId AND ar.deleted_at IS NULL")
    List<String> findUserPermissionsByTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + ADMIN_ROLES_ID + " = :adminRolesId")
    void deleteByAdminRoleId(@Bind("adminRolesId") UUID adminRolesId);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + ADMIN_ROLES_ID + " = :adminRolesId AND " + PERMISSION_CODE + " = :permissionCode")
    void deleteSpecificPermission(@Bind("adminRolesId") UUID adminRolesId, @Bind("permissionCode") String permissionCode);
}
