-- V5: Create Admin Roles Permissions Table
CREATE TABLE IF NOT EXISTS admin_roles_permissions (
    id UUID PRIMARY KEY,
    admin_roles_id UUID NOT NULL REFERENCES admin_roles(id) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL,
    CONSTRAINT uq_admin_roles_permissions UNIQUE (admin_roles_id, permission_code)
);

CREATE INDEX IF NOT EXISTS idx_admin_roles_permissions_role_id ON admin_roles_permissions(admin_roles_id);
