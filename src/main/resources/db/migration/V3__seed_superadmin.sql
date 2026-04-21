INSERT INTO usuarios (username, password, nombre, apellidos, email, dni, activo, rol_id, grupo_id)
SELECT
    'superadmin',
    '$2a$10$0Yg0N.24ji2e6LhEVcTj3uo9sZQyNHG.kyuaJkJxk/TC8.je2CK6.',
    'Admin',
    'Principal',
    'admin@astropi.com',
    '00000000X',
    TRUE,
    (SELECT id FROM roles WHERE nombre = 'SUPER_ADMIN'),
    COALESCE(
        (SELECT id FROM grupos WHERE nombre = 'Administradores'),
        (SELECT id FROM grupos WHERE nombre = 'Sistemas IT')
    )
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE username = 'superadmin'
);
