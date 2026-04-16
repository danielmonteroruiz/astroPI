-- ATENCION:
-- Este script elimina la tabla peticiones y sus datos.
-- Usarlo solo en entorno de desarrollo si la tabla peticiones contiene una estructura antigua.

DROP TABLE IF EXISTS peticiones CASCADE;

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
    CONSTRAINT fk_peticiones_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_peticiones_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT peticiones_estado_check
        CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'))
);
