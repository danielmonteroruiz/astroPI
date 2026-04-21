INSERT INTO grupos (nombre)
SELECT 'Administradores'
WHERE NOT EXISTS (
    SELECT 1 FROM grupos WHERE nombre = 'Administradores'
);

INSERT INTO grupos (nombre)
SELECT 'Desarrollo'
WHERE NOT EXISTS (
    SELECT 1 FROM grupos WHERE nombre = 'Desarrollo'
);

INSERT INTO grupos (nombre)
SELECT 'Contabilidad'
WHERE NOT EXISTS (
    SELECT 1 FROM grupos WHERE nombre = 'Contabilidad'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'GESTIONAR_USUARIOS', 'Permite gestionar usuarios desde administracion'
WHERE NOT EXISTS (
    SELECT 1 FROM permisos WHERE nombre = 'GESTIONAR_USUARIOS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'GESTIONAR_GRUPOS', 'Permite gestionar grupos desde administracion'
WHERE NOT EXISTS (
    SELECT 1 FROM permisos WHERE nombre = 'GESTIONAR_GRUPOS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'GESTIONAR_PERMISOS', 'Permite gestionar roles y permisos desde administracion'
WHERE NOT EXISTS (
    SELECT 1 FROM permisos WHERE nombre = 'GESTIONAR_PERMISOS'
);
