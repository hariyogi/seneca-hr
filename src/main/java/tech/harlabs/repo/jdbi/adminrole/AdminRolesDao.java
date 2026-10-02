package tech.harlabs.repo.jdbi.adminrole;

import static tech.harlabs.repo.jdbi.adminrole.AdminRolesEnt.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(AdminRolesEnt.class)
public interface AdminRolesDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ")")
    void insert(@BindMethods AdminRolesEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id AND " + DELETED_AT + " IS NULL")
    Optional<AdminRolesEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId AND " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " ASC")
    List<AdminRolesEnt> findByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TENANT_ID + " = :tenantId AND " + DELETED_AT + " IS NULL")
    long countByTenantId(@Bind("tenantId") UUID tenantId);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + NAME + " = :name, " + DESCRIPTION + " = :description, "
        + UPDATED_AT + " = :updatedAt, " + UPDATED_BY + " = :updatedBy WHERE " + ID + " = :id")
    void update(@Bind("id") UUID id, @Bind("name") String name, @Bind("description") String description,
                @Bind("updatedAt") LocalDateTime updatedAt, @Bind("updatedBy") String updatedBy);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + DELETED_AT + " = :deletedAt WHERE " + ID + " = :id")
    void softDelete(@Bind("id") UUID id, @Bind("deletedAt") LocalDateTime deletedAt);
}
