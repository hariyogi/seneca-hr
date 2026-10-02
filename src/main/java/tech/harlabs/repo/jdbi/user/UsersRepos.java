package tech.harlabs.repo.jdbi.user;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class UsersRepos {

    private final Jdbi jdbi;

    public UsersRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public UsersEnt insert(UsersEnt user) {
        jdbi.useExtension(UsersDao.class, dao -> dao.insert(user));
        return user;
    }

    public Optional<UsersEnt> findById(UUID id) {
        return jdbi.withExtension(UsersDao.class, dao -> dao.findById(id));
    }

    public Optional<UsersEnt> findByEmail(String email) {
        return jdbi.withExtension(UsersDao.class, dao -> dao.findByEmail(email));
    }

    public List<UsersEnt> findAll(int limit, int offset) {
        return jdbi.withExtension(UsersDao.class, dao -> dao.findAll(limit, offset));
    }

    public long countAll() {
        return jdbi.withExtension(UsersDao.class, UsersDao::countAll);
    }

    public List<UsersEnt> search(String query, int limit, int offset) {
        String wrapped = "%" + query.trim() + "%";
        return jdbi.withExtension(UsersDao.class, dao -> dao.search(wrapped, limit, offset));
    }

    public long countSearch(String query) {
        String wrapped = "%" + query.trim() + "%";
        return jdbi.withExtension(UsersDao.class, dao -> dao.countSearch(wrapped));
    }

    public void updateActiveStatus(UUID id, boolean isActive) {
        jdbi.useExtension(UsersDao.class, dao -> dao.updateActiveStatus(id, isActive, LocalDateTime.now()));
    }

    public void updateLastLogin(UUID id) {
        jdbi.useExtension(UsersDao.class, dao -> dao.updateLastLogin(id, LocalDateTime.now()));
    }

    public void softDelete(UUID id) {
        jdbi.useExtension(UsersDao.class, dao -> dao.softDelete(id, LocalDateTime.now()));
    }
}
