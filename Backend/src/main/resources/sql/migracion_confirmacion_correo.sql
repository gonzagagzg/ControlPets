-- =========================================================================
-- CONFIRMACIÓN DE CORREO AL CREAR UNA VETERINARIA
-- =========================================================================
-- Ejecutar sobre bases de datos ya existentes que fueron creadas antes de
-- agregar estado_cuenta/confirmacion_correo (ver Script_Aplicacion_Veterinaria.sql).
--
-- Flujo:
--   1. El SuperUsuario crea la veterinaria + su Administrador Local.
--   2. Si indicó un correo, la cuenta queda estado_cuenta = 'pendiente' y se
--      envía un enlace de confirmación a ese correo.
--   3. Al abrir el enlace, la cuenta pasa a estado_cuenta = 'activo'.
--   3. Sin correo, la cuenta se crea directamente 'activo'.
-- =========================================================================

-- ============================================================
-- 1. ESTADO DE LA CUENTA
-- ============================================================

ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS estado_cuenta VARCHAR(20) NOT NULL DEFAULT 'activo';

-- Postgres no soporta ADD CONSTRAINT IF NOT EXISTS: se ignora el error
-- si la restricción ya existía.
DO $$
BEGIN
    ALTER TABLE usuario
        ADD CONSTRAINT ck_usuario_estado_cuenta
        CHECK (estado_cuenta IN ('pendiente', 'activo'));
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;


-- ============================================================
-- 2. TOKENS DE CONFIRMACIÓN DE CORREO
-- =========================================================
-- Mismo patrón que recuperacion_password: token de un solo uso que expira.
-- ============================================================

CREATE TABLE IF NOT EXISTS confirmacion_correo (
    id_confirmacion UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_confirmacion_correo_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_confirmacion_correo_token
ON confirmacion_correo(token);


-- ============================================================
-- VERIFICACIÓN
-- ============================================================

SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'usuario'
  AND column_name = 'estado_cuenta';

SELECT to_regclass('public.confirmacion_correo') AS tabla_confirmacion_correo;
