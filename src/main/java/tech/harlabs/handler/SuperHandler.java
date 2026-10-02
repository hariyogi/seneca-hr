package tech.harlabs.handler;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tech.harlabs.repo.jdbi.tenant.TenantsEnt;
import tech.harlabs.repo.jdbi.tenant.TenantsRepos;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersEnt;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersRepos;
import tech.harlabs.repo.jdbi.user.UsersEnt;
import tech.harlabs.repo.jdbi.user.UsersRepos;

@ApplicationScoped
public class SuperHandler {

    private final TenantsRepos tenantsRepos;
    private final UsersRepos usersRepos;
    private final TenantMembersRepos tenantMembersRepos;

    public SuperHandler(TenantsRepos tenantsRepos, UsersRepos usersRepos, TenantMembersRepos tenantMembersRepos) {
        this.tenantsRepos = tenantsRepos;
        this.usersRepos = usersRepos;
        this.tenantMembersRepos = tenantMembersRepos;
    }

    public record SystemMetrics(
        long totalTenants,
        long activeTenants,
        long suspendedTenants,
        long closedTenants,
        long totalUsers
    ) {}

    public SystemMetrics getSystemMetrics() {
        long totalTenants = tenantsRepos.countAll();
        long activeTenants = tenantsRepos.countByStatus("ACTIVE");
        long suspendedTenants = tenantsRepos.countByStatus("SUSPEND");
        long closedTenants = tenantsRepos.countByStatus("CLOSED");
        long totalUsers = usersRepos.countAll();

        return new SystemMetrics(totalTenants, activeTenants, suspendedTenants, closedTenants, totalUsers);
    }

    public List<TenantsEnt> listTenants(String status, int limit, int offset) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            return tenantsRepos.findByStatus(status.toUpperCase(), limit, offset);
        }
        return tenantsRepos.findAll(limit, offset);
    }

    public TenantsEnt createTenant(String code, String name, String legalName, String npwp, String timezone, UUID ownerUserId) {
        var tenant = TenantsEnt.create(code, name, legalName, npwp, timezone);
        tenantsRepos.insert(tenant);

        if (ownerUserId != null) {
            var member = TenantMembersEnt.create(ownerUserId, tenant.id(), "owner", null);
            tenantMembersRepos.insert(member);
        }

        return tenant;
    }

    public void updateTenantStatus(UUID tenantId, String status) {
        tenantsRepos.updateStatus(tenantId, status.toUpperCase());
    }

    public Optional<TenantsEnt> getTenantById(UUID tenantId) {
        return tenantsRepos.findById(tenantId);
    }

    public List<UsersEnt> listUsers(String query, int limit, int offset) {
        if (query != null && !query.isBlank()) {
            return usersRepos.search(query, limit, offset);
        }
        return usersRepos.findAll(limit, offset);
    }

    public long countUsers(String query) {
        if (query != null && !query.isBlank()) {
            return usersRepos.countSearch(query);
        }
        return usersRepos.countAll();
    }

    public void toggleUserActiveStatus(UUID userId, boolean isActive) {
        usersRepos.updateActiveStatus(userId, isActive);
    }
}
