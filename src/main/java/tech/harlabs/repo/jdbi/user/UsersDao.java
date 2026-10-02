package tech.harlabs.repo.jdbi.user;

import static tech.harlabs.repo.jdbi.user.UsersEnt.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(UsersEnt.class)
public interface UsersDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ")")
    void insert(@BindMethods UsersEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id AND " + DELETED_AT + " IS NULL")
    Optional<UsersEnt> findById(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + EMAIL + " = :email AND " + DELETED_AT + " IS NULL")
    Optional<UsersEnt> findByEmail(@Bind("email") String email);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL ORDER BY " + CREATED_AT + " DESC LIMIT :limit OFFSET :offset")
    List<UsersEnt> findAll(@Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL")
    long countAll();

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL AND (LOWER(" + FULL_NAME + ") LIKE LOWER(:query) OR LOWER(" + EMAIL + ") LIKE LOWER(:query)) ORDER BY " + CREATED_AT + " DESC LIMIT :limit OFFSET :offset")
    List<UsersEnt> search(@Bind("query") String query, @Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + DELETED_AT + " IS NULL AND (LOWER(" + FULL_NAME + ") LIKE LOWER(:query) OR LOWER(" + EMAIL + ") LIKE LOWER(:query))")
    long countSearch(@Bind("query") String query);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + FULL_NAME + " = :fullName, " + PHONE + " = :phone, " + AVATAR_URL + " = :avatarUrl, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updateProfile(@Bind("id") UUID id, @Bind("fullName") String fullName, @Bind("phone") String phone, @Bind("avatarUrl") String avatarUrl, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + PASSWORD_HASH + " = :passwordHash, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updatePassword(@Bind("id") UUID id, @Bind("passwordHash") String passwordHash, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + IS_ACTIVE + " = :isActive, " + UPDATED_AT + " = :updatedAt WHERE " + ID + " = :id")
    void updateActiveStatus(@Bind("id") UUID id, @Bind("isActive") boolean isActive, @Bind("updatedAt") LocalDateTime updatedAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + LAST_LOGIN_AT + " = :lastLoginAt WHERE " + ID + " = :id")
    void updateLastLogin(@Bind("id") UUID id, @Bind("lastLoginAt") LocalDateTime lastLoginAt);

    @SqlUpdate("UPDATE " + TABLE_NAME + " SET " + DELETED_AT + " = :deletedAt WHERE " + ID + " = :id")
    void softDelete(@Bind("id") UUID id, @Bind("deletedAt") LocalDateTime deletedAt);
}
