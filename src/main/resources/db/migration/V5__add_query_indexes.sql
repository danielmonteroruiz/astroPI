CREATE INDEX IF NOT EXISTS idx_usuarios_grupo_id
    ON usuarios (grupo_id);

CREATE INDEX IF NOT EXISTS idx_usuarios_rol_id_activo
    ON usuarios (rol_id, activo);

CREATE INDEX IF NOT EXISTS idx_incidencias_usuario_id
    ON incidencias (usuario_id);

CREATE INDEX IF NOT EXISTS idx_incidencias_grupo_id
    ON incidencias (grupo_id);

CREATE INDEX IF NOT EXISTS idx_incidencias_estado
    ON incidencias (estado);

CREATE INDEX IF NOT EXISTS idx_incidencias_fecha_creacion
    ON incidencias (fecha_creacion);

CREATE INDEX IF NOT EXISTS idx_peticiones_usuario_id
    ON peticiones (usuario_id);

CREATE INDEX IF NOT EXISTS idx_peticiones_grupo_id
    ON peticiones (grupo_id);

CREATE INDEX IF NOT EXISTS idx_peticiones_estado
    ON peticiones (estado);

CREATE INDEX IF NOT EXISTS idx_peticiones_fecha_creacion
    ON peticiones (fecha_creacion);
