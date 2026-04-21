CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255)
);

CREATE TABLE grupos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255)
);

CREATE TABLE permisos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    nombre VARCHAR(255),
    apellidos VARCHAR(255),
    email VARCHAR(255),
    dni VARCHAR(255),
    activo BOOLEAN,
    rol_id BIGINT,
    grupo_id BIGINT,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(id),
    CONSTRAINT fk_usuarios_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id)
);

CREATE TABLE incidencias (
    id BIGSERIAL PRIMARY KEY,
    codigo_ticket VARCHAR(255) UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    estado VARCHAR(255),
    usuario_id BIGINT,
    servicio VARCHAR(100) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    grupo_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP,
    CONSTRAINT fk_incidencias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_incidencias_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT incidencias_estado_check CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'))
);

CREATE TABLE peticiones (
    id BIGSERIAL PRIMARY KEY,
    codigo_ticket VARCHAR(255) UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    estado VARCHAR(255),
    usuario_id BIGINT,
    grupo_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP,
    CONSTRAINT fk_peticiones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_peticiones_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT peticiones_estado_check CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'))
);

CREATE TABLE roles_permisos (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_roles_permisos_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

INSERT INTO roles (id, nombre)
VALUES
    (1, 'SUPER_ADMIN'),
    (2, 'USER')
ON CONFLICT (id) DO NOTHING;

INSERT INTO grupos (id, nombre)
VALUES
    (1, 'Sistemas IT')
ON CONFLICT (id) DO NOTHING;
