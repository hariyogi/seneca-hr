-- V8: Seed Initial Sysadmin, Sample Tenant, and Standard Roles
-- Password for sysadmin@seneca.local and owner@seneca.local: Admin@Seneca2026!

-- 1. Sysadmin (Super User - Only One Allowed)
INSERT INTO users (
    id, email, phone, password_hash, full_name, avatar_url, verified_at, is_active, created_at, updated_at
) VALUES (
    '01910000-0000-7000-8000-000000000001',
    'sysadmin@seneca.local',
    '+6281100000001',
    '$2a$10$m6AYUzERi5Vcz9DEyyo.peasmXQLlUkeO4L6Sy.e1CVeesP0.Io3K',
    'System Administrator',
    NULL,
    CURRENT_TIMESTAMP,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 2. Initial Sample Owner User
INSERT INTO users (
    id, email, phone, password_hash, full_name, avatar_url, verified_at, is_active, created_at, updated_at
) VALUES (
    '01910000-0000-7000-8000-000000000002',
    'owner@seneca.local',
    '+6281200000002',
    '$2a$10$m6AYUzERi5Vcz9DEyyo.peasmXQLlUkeO4L6Sy.e1CVeesP0.Io3K',
    'Hariyogi (Company Owner)',
    NULL,
    CURRENT_TIMESTAMP,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 3. Initial Sample Tenant
INSERT INTO tenants (
    id, code, name, legal_name, npwp, timezone, settings, status, created_at, updated_at
) VALUES (
    '01910000-0000-7000-8000-000000000010',
    'SENECA-CORP',
    'Seneca Teknologi Nusantara',
    'PT Seneca Teknologi Nusantara',
    '01.234.567.8-901.000',
    'Asia/Jakarta',
    '{"industry": "Information Technology", "features": ["face_attendance", "multi_branch"]}'::jsonb,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- 4. Assign Owner to Tenant
INSERT INTO tenant_members (
    id, user_id, tenant_id, roles, invited_by, joined_at, created_at, updated_at
) VALUES (
    '01910000-0000-7000-8000-000000000020',
    '01910000-0000-7000-8000-000000000002',
    '01910000-0000-7000-8000-000000000010',
    'owner',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id, tenant_id) DO NOTHING;

-- 5. Standard Admin Role for the Sample Tenant
INSERT INTO admin_roles (
    id, tenant_id, name, description, is_system, created_at, updated_at, created_by, updated_by
) VALUES (
    '01910000-0000-7000-8000-000000000030',
    '01910000-0000-7000-8000-000000000010',
    'HR Administrator',
    'Full management for employee profiles and attendance monitoring',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT DO NOTHING;

-- 6. Standard Permissions for the Role
INSERT INTO admin_roles_permissions (
    id, admin_roles_id, permission_code
) VALUES
    ('01910000-0000-7000-8000-000000000041', '01910000-0000-7000-8000-000000000030', 'EMPLOYEE_MANAGE'),
    ('01910000-0000-7000-8000-000000000042', '01910000-0000-7000-8000-000000000030', 'ATTENDANCE_VIEW'),
    ('01910000-0000-7000-8000-000000000043', '01910000-0000-7000-8000-000000000030', 'AUDIT_VIEW'),
    ('01910000-0000-7000-8000-000000000044', '01910000-0000-7000-8000-000000000030', 'LOCATION_MANAGE')
ON CONFLICT (admin_roles_id, permission_code) DO NOTHING;
