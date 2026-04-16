ALTER TABLE peticiones
DROP CONSTRAINT IF EXISTS peticiones_estado_check;

ALTER TABLE peticiones
ADD CONSTRAINT peticiones_estado_check
CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'));
