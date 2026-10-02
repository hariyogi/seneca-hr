-- V6: Create Member Admin Roles Table
CREATE TABLE IF NOT EXISTS member_admin_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    admin_roles_id UUID NOT NULL REFERENCES admin_roles(id) ON DELETE CASCADE,
    assign_by UUID REFERENCES users(id) ON DELETE SET NULL,
    assign_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_member_admin_roles UNIQUE (user_id, admin_roles_id)
);

CREATE INDEX IF NOT EXISTS idx_member_admin_roles_user_id ON member_admin_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_member_admin_roles_role_id ON member_admin_roles(admin_roles_id);
