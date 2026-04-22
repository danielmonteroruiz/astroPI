ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS credenciales_actualizadas_en TIMESTAMP;

UPDATE usuarios
SET credenciales_actualizadas_en = COALESCE(credenciales_actualizadas_en, CURRENT_TIMESTAMP);

ALTER TABLE usuarios
ALTER COLUMN credenciales_actualizadas_en SET NOT NULL;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    motivo VARCHAR(40) NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    expira_en TIMESTAMP NOT NULL,
    usado_en TIMESTAMP NULL,
    CONSTRAINT fk_password_reset_tokens_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_usuario_id
    ON password_reset_tokens(usuario_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_tokens_token_hash
    ON password_reset_tokens(token_hash);
