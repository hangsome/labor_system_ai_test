-- Seed data: default admin user
-- Password: admin123 (stored as plaintext for development; in production use bcrypt)

INSERT INTO user_account (username, password_hash, display_name, status)
SELECT 'admin', 'admin123', '系统管理员', 'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM user_account WHERE username = 'admin'
);

-- Default roles
INSERT INTO role (code, name) SELECT 'SUPER_ADMIN', '超级管理员' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE code = 'SUPER_ADMIN');
INSERT INTO role (code, name) SELECT 'FINANCE', '财务专员' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE code = 'FINANCE');
INSERT INTO role (code, name) SELECT 'HR_MANAGER', '人力主管' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE code = 'HR_MANAGER');

-- Assign admin to SUPER_ADMIN role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
AND NOT EXISTS (
  SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);
