package tech.harlabs.repo.jdbi.tenantmember;

import static tech.harlabs.repo.jdbi.tenantmember.TenantMembersEnt.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import tech.harlabs.dto.MemberDetailDto;

@RegisterConstructorMapper(TenantMembersEnt.class)
public interface TenantMembersDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ") ON CONFLICT (" + USER_ID + ", " + TENANT_ID + ") DO NOTHING")
    void insert(@BindMethods TenantMembersEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Optional<TenantMembersEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId AND " + TENANT_ID + " = :tenantId")
    Optional<TenantMembersEnt> findByUserAndTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId ORDER BY " + JOINED_AT + " DESC")
    List<TenantMembersEnt> findByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId ORDER BY " + JOINED_AT + " DESC")
    List<TenantMembersEnt> findByUserId(@Bind("userId") UUID userId);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId")
    long countByTenantId(@Bind("tenantId") UUID tenantId);

    @RegisterConstructorMapper(MemberDetailDto.class)
    @SqlQuery("SELECT tm.id, tm.user_id, tm.tenant_id, tm.roles, u.full_name, u.email, u.phone, u.avatar_url, tm.joined_at, "
        + "COALESCE(string_agg(DISTINCT ar.name, ', '), '-') AS admin_role_names "
        + "FROM " + TABLE_NAME + " tm "
        + "JOIN users u ON u.id = tm.user_id "
        + "LEFT JOIN member_admin_roles mar ON mar.user_id = tm.user_id "
        + "LEFT JOIN admin_roles ar ON ar.id = mar.admin_roles_id AND ar.tenant_id = tm.tenant_id AND ar.deleted_at IS NULL "
        + "WHERE tm.tenant_id = :tenantId AND u.deleted_at IS NULL "
        + "GROUP BY tm.id, tm.user_id, tm.tenant_id, tm.roles, u.full_name, u.email, u.phone, u.avatar_url, tm.joined_at "
        + "ORDER BY tm.joined_at DESC")
    List<MemberDetailDto> findMemberDetailsByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + ROLES + " = :roles, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updateRole(@Bind("id") UUID id, @Bind("roles") String roles, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    void deleteById(@Bind("id") UUID id);

    @SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID + " = :userId AND " + TENANT_ID + " = :tenantId")
    void deleteByUserAndTenant(@Bind("userId") UUID userId, @Bind("tenantId") UUID tenantId);
}
