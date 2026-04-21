ALTER TABLE roles
    ALTER COLUMN nombre SET NOT NULL;

ALTER TABLE roles
    ADD CONSTRAINT uk_roles_nombre UNIQUE (nombre);

ALTER TABLE grupos
    ALTER COLUMN nombre SET NOT NULL;

ALTER TABLE grupos
    ADD CONSTRAINT uk_grupos_nombre UNIQUE (nombre);

ALTER TABLE usuarios
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN password SET NOT NULL,
    ALTER COLUMN nombre SET NOT NULL,
    ALTER COLUMN apellidos SET NOT NULL,
    ALTER COLUMN dni SET NOT NULL,
    ALTER COLUMN activo SET NOT NULL,
    ALTER COLUMN rol_id SET NOT NULL,
    ALTER COLUMN grupo_id SET NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_username UNIQUE (username);

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_email UNIQUE (email);

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_dni UNIQUE (dni);
