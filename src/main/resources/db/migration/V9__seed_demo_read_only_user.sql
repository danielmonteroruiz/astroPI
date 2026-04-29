INSERT INTO roles (nombre)
SELECT 'DEMO_READ_ONLY'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE nombre = 'DEMO_READ_ONLY'
);

INSERT INTO grupos (nombre)
SELECT 'Demo'
WHERE NOT EXISTS (
    SELECT 1 FROM grupos WHERE nombre = 'Demo'
);

INSERT INTO usuarios (username, password, nombre, apellidos, email, dni, activo, rol_id, grupo_id, credenciales_actualizadas_en)
SELECT
    'demo',
    '$2a$10$f5C9SodsyubdBg.o5m3dIehQ5VsAjh4cSnI0n373dxJc//WM46DCW',
    'Usuario',
    'Demo',
    'demo@astropi.local',
    '99999999D',
    TRUE,
    (SELECT id FROM roles WHERE nombre = 'DEMO_READ_ONLY'),
    (SELECT id FROM grupos WHERE nombre = 'Demo'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE username = 'demo'
);

INSERT INTO incidencias (codigo_ticket, titulo, descripcion, estado, usuario_id, servicio, categoria, grupo_id, fecha_creacion)
SELECT
    'I-DEMO-0001',
    'Error intermitente de acceso a VPN',
    'Un usuario informa de cortes intermitentes al acceder por VPN desde fuera de la oficina.',
    'EN_PROCESO',
    (SELECT id FROM usuarios WHERE username = 'demo'),
    'Infraestructura',
    'VPN',
    (SELECT id FROM grupos WHERE nombre = 'Demo'),
    CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE NOT EXISTS (
    SELECT 1 FROM incidencias WHERE codigo_ticket = 'I-DEMO-0001'
);

INSERT INTO incidencias (codigo_ticket, titulo, descripcion, estado, usuario_id, servicio, categoria, grupo_id, fecha_creacion)
SELECT
    'I-DEMO-0002',
    'Instalacion fallida de aplicacion corporativa',
    'La instalacion termina sin confirmar licencia y deja la aplicacion sin acceso al perfil del usuario.',
    'ABIERTA',
    (SELECT id FROM usuarios WHERE username = 'demo'),
    'Software',
    'Instalacion fallida',
    (SELECT id FROM grupos WHERE nombre = 'Demo'),
    CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE NOT EXISTS (
    SELECT 1 FROM incidencias WHERE codigo_ticket = 'I-DEMO-0002'
);

INSERT INTO peticiones (codigo_ticket, titulo, descripcion, servicio, categoria, estado, usuario_id, grupo_id, fecha_creacion)
SELECT
    'P-DEMO-0001',
    'Alta de permisos para herramienta de reporting',
    'Solicitud de acceso de lectura a informes financieros para incorporacion al equipo.',
    'Accesos',
    'Alta de permisos',
    'ABIERTA',
    (SELECT id FROM usuarios WHERE username = 'demo'),
    (SELECT id FROM grupos WHERE nombre = 'Demo'),
    CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE NOT EXISTS (
    SELECT 1 FROM peticiones WHERE codigo_ticket = 'P-DEMO-0001'
);

INSERT INTO peticiones (codigo_ticket, titulo, descripcion, servicio, categoria, estado, usuario_id, grupo_id, fecha_creacion)
SELECT
    'P-DEMO-0002',
    'Solicitud de monitor adicional',
    'Peticion de segundo monitor para puesto administrativo con revision de stock pendiente.',
    'Hardware',
    'Solicitud de monitor',
    'CERRADA',
    (SELECT id FROM usuarios WHERE username = 'demo'),
    (SELECT id FROM grupos WHERE nombre = 'Demo'),
    CURRENT_TIMESTAMP - INTERVAL '7 days'
WHERE NOT EXISTS (
    SELECT 1 FROM peticiones WHERE codigo_ticket = 'P-DEMO-0002'
);
