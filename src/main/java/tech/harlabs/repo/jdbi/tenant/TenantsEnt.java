package tech.harlabs.repo.jdbi.tenant;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record TenantsEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(CODE) String code,
    @ColumnName(NAME) String name,
    @ColumnName(LEGAL_NAME) String legalName,
    @ColumnName(NPWP) String npwp,
    @ColumnName(TIMEZONE) String timezone,
    @ColumnName(SETTINGS) String settings,
    @ColumnName(STATUS) String status,
    @ColumnName(CREATED_AT) LocalDateTime createdAt,
    @ColumnName(UPDATED_AT) LocalDateTime updatedAt,
    @ColumnName(DELETED_AT) LocalDateTime deletedAt
) {
    public static final String TABLE_NAME = "tenants";
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String LEGAL_NAME = "legal_name";
    public static final String NPWP = "npwp";
    public static final String TIMEZONE = "timezone";
    public static final String SETTINGS = "settings";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";

    public static final String FIELDS = ID + ", " + CODE + ", " + NAME + ", " + LEGAL_NAME + ", "
        + NPWP + ", " + TIMEZONE + ", " + SETTINGS + ", " + STATUS + ", "
        + CREATED_AT + ", " + UPDATED_AT + ", " + DELETED_AT;

    public static final String BINDERS = ":id, :code, :name, :legalName, :npwp, :timezone, :settings::jsonb, :status, :createdAt, :updatedAt, :deletedAt";

    public static TenantsEnt create(String code, String name, String legalName, String npwp, String timezone) {
        var now = LocalDateTime.now();
        return new TenantsEnt(
            RepoUtil.generateId(),
            code.trim().toUpperCase(),
            name,
            legalName,
            npwp,
            (timezone == null || timezone.isBlank()) ? "Asia/Jakarta" : timezone,
            "{}",
            "ACTIVE",
            now,
            now,
            null
        );
    }
}
