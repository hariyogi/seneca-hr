package tech.harlabs.config;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@ApplicationScoped
public class JdbiProvider {

    @Produces
    @Singleton
    public Jdbi createJdbi(AgroalDataSource dataSource) {
        var jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new PostgresPlugin());

        // Custom mapper for PostgreSQL timestamps to java.time.LocalDateTime
        jdbi.registerColumnMapper(LocalDateTime.class, (rs, col, ctx) -> {
            var ts = rs.getTimestamp(col);
            return ts != null ? ts.toLocalDateTime() : null;
        });

        return jdbi;
    }
}
