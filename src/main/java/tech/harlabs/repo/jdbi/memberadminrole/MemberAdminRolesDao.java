package tech.harlabs.repo.jdbi.memberadminrole;

import static tech.harlabs.repo.jdbi.memberadminrole.MemberAdminRolesEnt.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(MemberAdminRolesEnt.class)
public interface MemberAdminRolesDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ") ON CONFLICT (" + USER_ID + ", " + ADMIN_ROLES_ID + ") DO NOTHING")
    void insert(@BindMethods MemberAdminRolesEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Optional<MemberAdminRolesEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId")
    List<MemberAdminRolesEnt> findByUserId(@Bind("userId") UUID userId);

    @SqlQuery("SELECT mar.* FROM " + TABLE_NAME + " mar "
        + "JOIN admin_roles ar ON ar.id = mar.admin_roles_id "
        + "WHERE mar.user_id = :userId AND ar.tenant_id = :tenantId AND ar.deleted_at IS NULL")
    List<MemberAdminRolesEnt> findByUserAndTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT mar.admin_roles_id FROM " + TABLE_NAME + " mar "
        + "JOIN admin_roles ar ON ar.id = mar.admin_roles_id "
        + "WHERE mar.user_id = :userId AND ar.tenant_id = :tenantId AND ar.deleted_at IS NULL")
    List<UUID> findRoleIdsByUserAndTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    void deleteById(@Bind("id") UUID id);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId AND " + ADMIN_ROLES_ID + " = :adminRolesId")
    void deleteByUserAndRole(@Bind("userId") UUID userId, @Bind("adminRolesId") UUID adminRolesId);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId AND " + ADMIN_ROLES_ID + " IN (SELECT id FROM admin_roles WHERE tenant_id = :tenantId)")
    void deleteAllUserRolesInTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);
}
