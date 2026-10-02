package tech.harlabs.handler;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tech.harlabs.dto.MemberDetailDto;
import tech.harlabs.repo.jdbi.adminrole.AdminRolesEnt;
import tech.harlabs.repo.jdbi.adminrole.AdminRolesRepos;
import tech.harlabs.repo.jdbi.face.FaceEmbeddingsEnt;
import tech.harlabs.repo.jdbi.face.FaceEmbeddingsRepos;
import tech.harlabs.repo.jdbi.memberadminrole.MemberAdminRolesRepos;
import tech.harlabs.repo.jdbi.tenant.TenantsEnt;
import tech.harlabs.repo.jdbi.tenant.TenantsRepos;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersEnt;
import tech.harlabs.repo.jdbi.tenantmember.TenantMembersRepos;
import tech.harlabs.repo.jdbi.user.UsersEnt;
import tech.harlabs.repo.jdbi.user.UsersRepos;
import tech.harlabs.web.security.PasswordUtil;

@ApplicationScoped
public class OwnerHandler {

    private final TenantsRepos tenantsRepos;
    private final TenantMembersRepos tenantMembersRepos;
    private final UsersRepos usersRepos;
    private final AdminRolesRepos adminRolesRepos;
    private final MemberAdminRolesRepos memberAdminRolesRepos;
    private final FaceEmbeddingsRepos faceEmbeddingsRepos;

    public OwnerHandler(
        TenantsRepos tenantsRepos,
        TenantMembersRepos tenantMembersRepos,
        UsersRepos usersRepos,
        AdminRolesRepos adminRolesRepos,
        MemberAdminRolesRepos memberAdminRolesRepos,
        FaceEmbeddingsRepos faceEmbeddingsRepos
    ) {
        this.tenantsRepos = tenantsRepos;
        this.tenantMembersRepos = tenantMembersRepos;
        this.usersRepos = usersRepos;
        this.adminRolesRepos = adminRolesRepos;
        this.memberAdminRolesRepos = memberAdminRolesRepos;
        this.faceEmbeddingsRepos = faceEmbeddingsRepos;
    }

    public List<TenantsEnt> getOwnedTenants(UUID userId) {
        return tenantsRepos.findOwnedByUserId(userId);
    }

    public Optional<TenantsEnt> getTenantById(UUID tenantId) {
        return tenantsRepos.findById(tenantId);
    }

    public TenantsEnt createTenant(UUID ownerUserId, String code, String name, String legalName, String npwp, String timezone) {
        var tenant = TenantsEnt.create(code, name, legalName, npwp, timezone);
        tenantsRepos.insert(tenant);

        var member = TenantMembersEnt.create(ownerUserId, tenant.id(), "owner", null);
        tenantMembersRepos.insert(member);

        return tenant;
    }

    public void updateTenantDetails(UUID tenantId, String name, String legalName, String npwp, String timezone) {
        tenantsRepos.updateDetails(tenantId, name, legalName, npwp, timezone);
    }

    public List<MemberDetailDto> listMembers(UUID tenantId) {
        return tenantMembersRepos.findMemberDetailsByTenantId(tenantId);
    }

    public TenantMembersEnt addMember(UUID tenantId, String email, String fullName, String phone, String role, UUID invitedBy) {
        String cleanEmail = email.trim().toLowerCase();
        var userOpt = usersRepos.findByEmail(cleanEmail);
        UsersEnt user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Create user with default initial password
            String hash = PasswordUtil.hash("Seneca@2026");
            user = UsersEnt.create(cleanEmail, phone, hash, fullName);
            usersRepos.insert(user);
        }

        var member = TenantMembersEnt.create(user.id(), tenantId, role.toLowerCase(), invitedBy);
        return tenantMembersRepos.insert(member);
    }

    public void updateMemberRole(UUID membershipId, String newRole) {
        tenantMembersRepos.updateRole(membershipId, newRole.toLowerCase());
    }

    public void removeMember(UUID membershipId) {
        tenantMembersRepos.deleteById(membershipId);
    }

    public List<AdminRolesEnt> listAdminRoles(UUID tenantId) {
        return adminRolesRepos.findByTenantId(tenantId);
    }

    public AdminRolesEnt createAdminRole(UUID tenantId, String name, String description, List<String> permissions, String createdBy) {
        var role = AdminRolesEnt.create(tenantId, name, description, createdBy);
        adminRolesRepos.insert(role);
        adminRolesRepos.setRolePermissions(role.id(), permissions);
        return role;
    }

    public void updateAdminRole(UUID roleId, String name, String description, List<String> permissions, String updatedBy) {
        adminRolesRepos.update(roleId, name, description, updatedBy);
        adminRolesRepos.setRolePermissions(roleId, permissions);
    }

    public void deleteAdminRole(UUID roleId) {
        adminRolesRepos.softDelete(roleId);
    }

    public List<String> getRolePermissions(UUID roleId) {
        return adminRolesRepos.getRolePermissions(roleId);
    }

    public void assignAdminRolesToMember(UUID userId, UUID tenantId, List<UUID> roleIds, UUID assignBy) {
        memberAdminRolesRepos.syncUserRolesInTenant(userId, tenantId, roleIds, assignBy);
    }

    public List<UUID> getMemberAssignedRoles(UUID userId, UUID tenantId) {
        return memberAdminRolesRepos.findRoleIdsByUserAndTenant(userId, tenantId);
    }

    public List<FaceEmbeddingsEnt> listFaceEmbeddings(UUID tenantId) {
        return faceEmbeddingsRepos.findByTenantId(tenantId);
    }

    public FaceEmbeddingsEnt saveFaceEmbedding(UUID userId, UUID tenantId, String vectorString, String modelName, String modelVersion) {
        return faceEmbeddingsRepos.saveEmbedding(userId, tenantId, vectorString, modelName, modelVersion);
    }

    public long countActiveFaceEmbeddings(UUID tenantId) {
        return faceEmbeddingsRepos.countActiveByTenantId(tenantId);
    }
}
