ALTER TABLE incidencias
ADD COLUMN IF NOT EXISTS usuario_asignado_id BIGINT;

ALTER TABLE peticiones
ADD COLUMN IF NOT EXISTS usuario_asignado_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_incidencias_usuario_asignado'
    ) THEN
        ALTER TABLE incidencias
        ADD CONSTRAINT fk_incidencias_usuario_asignado
        FOREIGN KEY (usuario_asignado_id) REFERENCES usuarios(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_peticiones_usuario_asignado'
    ) THEN
        ALTER TABLE peticiones
        ADD CONSTRAINT fk_peticiones_usuario_asignado
        FOREIGN KEY (usuario_asignado_id) REFERENCES usuarios(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_incidencias_usuario_asignado_id ON incidencias(usuario_asignado_id);
CREATE INDEX IF NOT EXISTS idx_peticiones_usuario_asignado_id ON peticiones(usuario_asignado_id);

CREATE TABLE IF NOT EXISTS comentarios_incidencia (
    id BIGSERIAL PRIMARY KEY,
    incidencia_id BIGINT NOT NULL REFERENCES incidencias(id) ON DELETE CASCADE,
    autor_id BIGINT NOT NULL REFERENCES usuarios(id),
    contenido VARCHAR(1000) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_comentarios_incidencia_ticket_id
    ON comentarios_incidencia(incidencia_id);

CREATE TABLE IF NOT EXISTS comentarios_peticion (
    id BIGSERIAL PRIMARY KEY,
    peticion_id BIGINT NOT NULL REFERENCES peticiones(id) ON DELETE CASCADE,
    autor_id BIGINT NOT NULL REFERENCES usuarios(id),
    contenido VARCHAR(1000) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_comentarios_peticion_ticket_id
    ON comentarios_peticion(peticion_id);
