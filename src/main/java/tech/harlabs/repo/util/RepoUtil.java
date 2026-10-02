package tech.harlabs.repo.util;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

/**
 * Utility helper for repository operations and UUID v7 identifier generation.
 */
public final class RepoUtil {

    private RepoUtil() {
    }

    /**
     * Generates a time-ordered sequential UUID v7.
     * Guarantees high-performance indexing in PostgreSQL B-Tree indexes.
     */
    public static UUID generateId() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
