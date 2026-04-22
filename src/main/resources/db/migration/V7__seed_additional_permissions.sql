INSERT INTO permisos (nombre, descripcion)
SELECT 'VER_TICKETS_GRUPO', 'Permite consultar tickets del grupo asignado'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'VER_TICKETS_GRUPO'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'GESTIONAR_TICKETS', 'Permite crear y actualizar incidencias y peticiones'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'GESTIONAR_TICKETS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'CAMBIAR_ESTADO_TICKETS', 'Permite cambiar el estado de los tickets'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'CAMBIAR_ESTADO_TICKETS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'ASIGNAR_TICKETS', 'Permite asignar tickets a usuarios'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'ASIGNAR_TICKETS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'GESTIONAR_COMENTARIOS', 'Permite añadir y mantener comentarios en tickets'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'GESTIONAR_COMENTARIOS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'CREAR_USUARIOS', 'Permite crear usuarios desde administracion o desde tickets'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'CREAR_USUARIOS'
);

INSERT INTO permisos (nombre, descripcion)
SELECT 'RESETEAR_PASSWORDS', 'Permite cambiar passwords de otros usuarios'
WHERE NOT EXISTS (
    SELECT 1
    FROM permisos
    WHERE nombre = 'RESETEAR_PASSWORDS'
);
