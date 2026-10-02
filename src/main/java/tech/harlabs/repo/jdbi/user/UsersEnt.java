package tech.harlabs.repo.jdbi.user;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record UsersEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(EMAIL) String email,
    @ColumnName(PHONE) String phone,
    @ColumnName(PASSWORD_HASH) String passwordHash,
    @ColumnName(FULL_NAME) String fullName,
    @ColumnName(AVATAR_URL) String avatarUrl,
    @ColumnName(VERIFIED_AT) LocalDateTime verifiedAt,
    @ColumnName(IS_ACTIVE) boolean isActive,
    @ColumnName(LAST_LOGIN_AT) LocalDateTime lastLoginAt,
    @ColumnName(CREATED_AT) LocalDateTime createdAt,
    @ColumnName(UPDATED_AT) LocalDateTime updatedAt,
    @ColumnName(DELETED_AT) LocalDateTime deletedAt
) {
    public static final String TABLE_NAME = "users";
    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String PASSWORD_HASH = "password_hash";
    public static final String FULL_NAME = "full_name";
    public static final String AVATAR_URL = "avatar_url";
    public static final String VERIFIED_AT = "verified_at";
    public static final String IS_ACTIVE = "is_active";
    public static final String LAST_LOGIN_AT = "last_login_at";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";

    public static final String FIELDS = ID + ", " + EMAIL + ", " + PHONE + ", " + PASSWORD_HASH + ", "
        + FULL_NAME + ", " + AVATAR_URL + ", " + VERIFIED_AT + ", " + IS_ACTIVE + ", "
        + LAST_LOGIN_AT + ", " + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT;

    public static final String BINDERS = ":id, :email, :phone, :passwordHash, :fullName, :avatarUrl, :verifiedAt, :isActive, :lastLoginAt, :createdAt, :updatedAt, :deletedAt";

    public static UsersEnt create(String email, String phone, String passwordHash, String fullName) {
        var now = LocalDateTime.now();
        return new UsersEnt(
            RepoUtil.generateId(),
            email,
            phone,
            passwordHash,
            fullName,
            null,
            now,
            true,
            null,
            now,
            now,
            null
        );
    }
}
