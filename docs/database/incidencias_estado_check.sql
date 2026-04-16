ALTER TABLE incidencias
DROP CONSTRAINT IF EXISTS incidencias_estado_check;

ALTER TABLE incidencias
ADD CONSTRAINT incidencias_estado_check
CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'PARADA', 'RESUELTA', 'CERRADA'));
