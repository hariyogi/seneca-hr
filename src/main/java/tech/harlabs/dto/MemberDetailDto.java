package tech.harlabs.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record MemberDetailDto(
    @ColumnName("id") UUID id,
    @ColumnName("user_id") UUID userId,
    @ColumnName("tenant_id") UUID tenantId,
    @ColumnName("roles") String roles,
    @ColumnName("full_name") String fullName,
    @ColumnName("email") String email,
    @ColumnName("phone") String phone,
    @ColumnName("avatar_url") String avatarUrl,
    @ColumnName("joined_at") LocalDateTime joinedAt,
    @ColumnName("admin_role_names") String adminRoleNames
) {}
