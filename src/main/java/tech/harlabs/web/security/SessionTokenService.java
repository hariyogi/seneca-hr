package tech.harlabs.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SessionTokenService {

    @ConfigProperty(name = "seneca.auth.session-secret", defaultValue = "9f8c6b7e5d4a3b2c1f0e9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a")
    String secret;

    @ConfigProperty(name = "seneca.auth.session-timeout-minutes", defaultValue = "1440")
    long timeoutMinutes;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(SenecaUserSession session) {
        long now = System.currentTimeMillis();
        long exp = now + (timeoutMinutes * 60 * 1000);

        var builder = Jwts.builder()
            .subject(session.userId().toString())
            .claim("email", session.email())
            .claim("fullName", session.fullName())
            .claim("isSuper", session.isSuper())
            .claim("currentRole", session.currentRole())
            .issuedAt(new Date(now))
            .expiration(new Date(exp))
            .signWith(key);

        if (session.currentTenantId() != null) {
            builder.claim("currentTenantId", session.currentTenantId().toString());
        }
        if (session.currentTenantName() != null) {
            builder.claim("currentTenantName", session.currentTenantName());
        }
        if (session.permissions() != null && !session.permissions().isEmpty()) {
            builder.claim("permissions", session.permissions());
        }

        return builder.compact();
    }

    @SuppressWarnings("unchecked")
    public Optional<SenecaUserSession> parseToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims payload = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();

            UUID userId = UUID.fromString(payload.getSubject());
            String email = payload.get("email", String.class);
            String fullName = payload.get("fullName", String.class);
            Boolean isSuper = payload.get("isSuper", Boolean.class);
            String currentRole = payload.get("currentRole", String.class);

            String tenantIdStr = payload.get("currentTenantId", String.class);
            UUID currentTenantId = (tenantIdStr != null && !tenantIdStr.isBlank()) ? UUID.fromString(tenantIdStr) : null;
            String currentTenantName = payload.get("currentTenantName", String.class);

            List<String> permissions = payload.get("permissions", List.class);
            if (permissions == null) {
                permissions = Collections.emptyList();
            }

            return Optional.of(new SenecaUserSession(
                userId,
                email,
                fullName,
                Boolean.TRUE.equals(isSuper),
                currentTenantId,
                currentTenantName,
                currentRole,
                permissions
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
