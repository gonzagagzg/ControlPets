-- =========================================================================
-- MIGRACIÓN: columnas de bloqueo en usuario + módulo Mensualidades
--
-- Corrige el desfase entre el código Java (ya usa estas columnas/tabla) y
-- el esquema realmente aplicado en la base de datos local, detectado por
-- el 500 en POST /api/auth/login: "no existe la columna «tipobloqueo»".
-- Idempotente: se puede ejecutar varias veces sin error.
-- =========================================================================

-- Agregar columna tipobloqueo a la tabla usuario (nullable, opciones: 'pago', 'tecnico')
ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS tipobloqueo VARCHAR(10)
    CHECK (tipobloqueo IN ('pago', 'tecnico'));

-- Agregar columna notificacion a la tabla usuario (nullable, opciones: 'pago', 'tecnico')
ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS notificacion VARCHAR(10)
    CHECK (notificacion IN ('pago', 'tecnico'));

-- Tabla de mensualidades/pagos de la aplicación por veterinaria
-- (MensualidadEmpresaDAO/Service/Servlet ya la consumen; solo faltaba la DDL).
CREATE TABLE IF NOT EXISTS mensualidad_empresa (
    id_mensualidad     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa         UUID NOT NULL,
    periodo            VARCHAR(20) NOT NULL,
    valor              NUMERIC(10, 2) NOT NULL,
    fecha_vencimiento  DATE NOT NULL,
    fecha_pago         DATE,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA')),
    observacion        VARCHAR(255),
    activo             BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_mensualidad_empresa FOREIGN KEY (id_empresa)
        REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_mensualidad_empresa_empresa
    ON mensualidad_empresa (id_empresa);

-- Permisos del módulo MENSUALIDADES (LISTAR/VER/CREAR/EDITAR/ELIMINAR).
INSERT INTO permiso (modulo, accion)
SELECT 'MENSUALIDADES', accion
FROM (VALUES ('LISTAR'), ('VER'), ('CREAR'), ('EDITAR'), ('ELIMINAR')) AS acciones(accion)
WHERE NOT EXISTS (
    SELECT 1 FROM permiso p
    WHERE p.modulo = 'MENSUALIDADES' AND p.accion = acciones.accion
);

-- Administrador Local (id_rol = 2): solo LISTAR y VER, según lo reportado
-- (crear/editar/eliminar quedan reservados a SuperUsuario, que además
-- bypassa todo chequeo de permisos — ver Autorizacion.exigir).
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 2, p.id_permiso
FROM permiso p
WHERE p.modulo = 'MENSUALIDADES' AND p.accion IN ('LISTAR', 'VER')
  AND NOT EXISTS (
      SELECT 1 FROM rol_permiso rp
      WHERE rp.id_rol = 2 AND rp.id_permiso = p.id_permiso
  );
