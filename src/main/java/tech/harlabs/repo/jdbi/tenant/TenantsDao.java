package tech.harlabs.repo.jdbi.tenant;

import static tech.harlabs.repo.jdbi.tenant.TenantsEnt.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(TenantsEnt.class)
public interface TenantsDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ")")
    void insert(@BindMethods TenantsEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id AND " + DELETED_AT + " IS NULL")
    Optional<TenantsEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + CODE + " = :code AND " + DELETED_AT + " IS NULL")
    Optional<TenantsEnt> findByCode(@Bind("code") String code);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " DESC LIMIT :limit OFFSET :offset")
    List<TenantsEnt> findAll(@Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL")
    long countAll();

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS + " = :status AND " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " DESC LIMIT :limit OFFSET :offset")
    List<TenantsEnt> findByStatus(@Bind("status") String status, @Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + STATUS + " = :status AND " + DELETED_AT + " IS NULL")
    long countByStatus(@Bind("status") String status);

    @SqlQuery("SELECT t.* FROM " + TABLE_NAME + " t "
        + "JOIN tenant_members tm ON tm.tenant_id = t.id "
        + "WHERE tm.user_id = :userId AND tm.roles = 'owner' AND t.deleted_at IS NULL "
        + "ORDER BY t.created_at DESC")
    List<TenantsEnt> findOwnedByUserId(@Bind("userId") UUID userId);

    @SqlQuery("SELECT t.* FROM " + TABLE_NAME + " t "
        + "JOIN tenant_members tm ON tm.tenant_id = t.id "
        + "WHERE tm.user_id = :userId AND t.deleted_at IS NULL "
        + "ORDER BY t.created_at DESC")
    List<TenantsEnt> findUserTenants(@Bind("userId") UUID userId);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + NAME + " = :name, " + LEGAL_NAME + " = :legalName, "
        + NPWP + " = :npwp, " + TIMEZONE + " = :timezone, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updateDetails(@Bind("id") UUID id, @Bind("name") String name, @Bind("legalName") String legalName,
                       @Bind("npwp") String npwp, @Bind("timezone") String timezone, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + STATUS + " = :status, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updateStatus(@Bind("id") UUID id, @Bind("status") String status, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + DELETED_AT + " = :deletedAt WHERE " + ID + " = :id")
    void softDelete(@Bind("id") UUID id, @Bind("deletedAt") LocalDateTime deletedAt);
}
