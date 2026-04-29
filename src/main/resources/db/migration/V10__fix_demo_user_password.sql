UPDATE usuarios
SET password = '$2a$10$f5C9SodsyubdBg.o5m3dIehQ5VsAjh4cSnI0n373dxJc//WM46DCW',
    activo = TRUE,
    credenciales_actualizadas_en = COALESCE(credenciales_actualizadas_en, CURRENT_TIMESTAMP)
WHERE username = 'demo';
