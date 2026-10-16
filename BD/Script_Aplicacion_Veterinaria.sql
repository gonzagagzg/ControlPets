--Ver BDs
SELECT datname AS base_de_datos
FROM pg_database
WHERE datistemplate = false;

CREATE DATABASE aplicacion_veterinaria;

--ver tablas de BD
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;


-- Activar extensión para la generación automática de UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================================
-- 1. TABLAS MAESTRAS GLOBALES (No dependen de Empresa)
-- =========================================================================

CREATE TABLE sri_iva (
                         id_iva SERIAL PRIMARY KEY,
                         porcentaje NUMERIC(5,2) NOT NULL,
                         codigo_sri VARCHAR(10)
);

CREATE TABLE especie (
                         id_especie SERIAL PRIMARY KEY,
                         nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE raza (
                      id_raza SERIAL PRIMARY KEY,
                      id_especie INTEGER NOT NULL,
                      nombre VARCHAR(50) NOT NULL,
                      CONSTRAINT fk_raza_especie FOREIGN KEY (id_especie) REFERENCES especie(id_especie) ON DELETE RESTRICT
);

CREATE TABLE vacuna (
                        id_vacuna SERIAL PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL UNIQUE
);

-- =========================================================================
-- 2. SEGURIDAD Y ROLES (RBAC)
-- =========================================================================

CREATE TABLE rol (
                     id_rol SERIAL PRIMARY KEY,
                     nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permiso (
                         id_permiso SERIAL PRIMARY KEY,
                         modulo VARCHAR(50) NOT NULL,
                         accion VARCHAR(50) NOT NULL,
                         CONSTRAINT uq_modulo_accion UNIQUE (modulo, accion)
);

CREATE TABLE rol_permiso (
                             id_rol INTEGER NOT NULL,
                             id_permiso INTEGER NOT NULL,
                             PRIMARY KEY (id_rol, id_permiso),
                             CONSTRAINT fk_rp_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE CASCADE,
                             CONSTRAINT fk_rp_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE
);

-- =========================================================================
-- 3. MULTIEMPRESA (Eje Central SaaS)
-- =========================================================================

CREATE TABLE empresa (
                         id_empresa UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         ruc VARCHAR(13) NOT NULL UNIQUE,
                         razon_social VARCHAR(150) NOT NULL,
                         direccion VARCHAR(255) NOT NULL,
                         correo VARCHAR(100),
                         telefono VARCHAR(15),
                         activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- =========================================================================
-- 4. USUARIOS, PERSONAL Y CLIENTES
-- =========================================================================

CREATE TABLE usuario (
                         id_usuario UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         id_empresa UUID NOT NULL,
                         id_rol INTEGER NOT NULL,
                         usuario VARCHAR(50) NOT NULL,
                         clave_hash VARCHAR(255) NOT NULL,
                         nombres VARCHAR(100) NOT NULL,
                         activo BOOLEAN DEFAULT TRUE NOT NULL,
                         estado_cuenta VARCHAR(20) NOT NULL DEFAULT 'activo'
                             CHECK (estado_cuenta IN ('pendiente', 'activo')),
                         tipobloqueo VARCHAR(10) CHECK (tipobloqueo IN ('pago', 'tecnico')) DEFAULT NULL,
                         notificacion VARCHAR(10) CHECK (notificacion IN ('pago', 'tecnico')) DEFAULT NULL,
                         CONSTRAINT uq_usuario_global UNIQUE (usuario), -- El usuario es único en todo el sistema
                         CONSTRAINT fk_usuario_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                         CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE RESTRICT
);

CREATE TABLE veterinario (
                             id_veterinario UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             id_usuario UUID NOT NULL UNIQUE, -- Relación 1 a 1
                             id_empresa UUID NOT NULL,
                             especialidad VARCHAR(100),
                             CONSTRAINT fk_vet_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
                             CONSTRAINT fk_vet_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

CREATE TABLE cliente (
                         id_cliente UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         id_empresa UUID NOT NULL,
                         tipo_documento VARCHAR(10) NOT NULL DEFAULT 'CEDULA',
                         identificacion VARCHAR(20) NOT NULL,
                         nombres VARCHAR(150) NOT NULL,
                         direccion VARCHAR(200),
                         correo VARCHAR(120),
                         telefono VARCHAR(20),
                         CONSTRAINT uq_cliente_empresa UNIQUE (id_empresa, identificacion),
                         CONSTRAINT fk_cliente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

-- =========================================================================
-- 5. MÓDULO CLÍNICO MÍNIMO
-- =========================================================================

CREATE TABLE mascota (
                         id_mascota UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         id_empresa UUID NOT NULL,
                         id_cliente UUID NOT NULL,
                         id_raza INTEGER NOT NULL,
                         nombre VARCHAR(50) NOT NULL,
                         fecha_nacimiento DATE,
                         CONSTRAINT fk_mascota_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                         CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
                         CONSTRAINT fk_mascota_raza FOREIGN KEY (id_raza) REFERENCES raza(id_raza) ON DELETE RESTRICT
);

CREATE TABLE mascota_vacuna (
                                id_mascota_vacuna UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                id_empresa UUID NOT NULL,
                                id_mascota UUID NOT NULL,
                                id_vacuna INTEGER NOT NULL,
                                fecha_aplicacion DATE NOT NULL,
                                CONSTRAINT fk_mv_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                                CONSTRAINT fk_mv_mascota FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota) ON DELETE CASCADE,
                                CONSTRAINT fk_mv_vacuna FOREIGN KEY (id_vacuna) REFERENCES vacuna(id_vacuna) ON DELETE RESTRICT
);

CREATE TABLE cita (
                      id_cita UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      id_empresa UUID NOT NULL,
                      id_mascota UUID NOT NULL,
                      id_veterinario UUID NOT NULL,
                      fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
                      estado VARCHAR(20) DEFAULT 'Pendiente' NOT NULL, -- P.ej: Pendiente, Atendida, Cancelada
                      CONSTRAINT fk_cita_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                      CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota) ON DELETE RESTRICT,
                      CONSTRAINT fk_cita_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario) ON DELETE RESTRICT
);

CREATE TABLE historial_clinico (
                                   id_historial UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   id_empresa UUID NOT NULL,
                                   id_cita UUID NOT NULL UNIQUE, -- Relación 1 a 1 originada por cita
                                   peso_kg NUMERIC(6,2),
                                   temperatura_c NUMERIC(4,1),
                                   anamnesis TEXT,
                                   diagnostico TEXT,
                                   CONSTRAINT fk_hc_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                                   CONSTRAINT fk_hc_cita FOREIGN KEY (id_cita) REFERENCES cita(id_cita) ON DELETE RESTRICT
);

-- =========================================================================
-- 6. MÓDULO INVENTARIO Y PRODUCTOS
-- =========================================================================

CREATE TABLE categoria (
                           id_categoria SERIAL PRIMARY KEY,
                           id_empresa UUID NOT NULL,
                           nombre VARCHAR(50) NOT NULL,
                           CONSTRAINT uq_cat_empresa UNIQUE (id_empresa, nombre),
                           CONSTRAINT fk_cat_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

CREATE TABLE producto (
                          id_producto UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          id_empresa UUID NOT NULL,
                          id_categoria INTEGER NOT NULL,
                          id_iva INTEGER NOT NULL,
                          nombre VARCHAR(100) NOT NULL,
                          precio_unitario NUMERIC(10,2) NOT NULL,
                          stock_actual INTEGER DEFAULT 0 NOT NULL,
                          stock_minimo INTEGER DEFAULT 0 NOT NULL,
                          fecha_caducidad DATE,
                          CONSTRAINT fk_prod_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                          CONSTRAINT fk_prod_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
                          CONSTRAINT fk_prod_iva FOREIGN KEY (id_iva) REFERENCES sri_iva(id_iva) ON DELETE RESTRICT
);

-- Se crean las recetas clínicas tras conocer los productos
CREATE TABLE receta (
                        id_receta UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        id_empresa UUID NOT NULL,
                        id_historial UUID NOT NULL UNIQUE, -- Relación 1 a 1 con historial
                        indicaciones_generales TEXT,
                        CONSTRAINT fk_receta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                        CONSTRAINT fk_receta_hc FOREIGN KEY (id_historial) REFERENCES historial_clinico(id_historial) ON DELETE RESTRICT
);

CREATE TABLE receta_detalle (
                                id_detalle_receta UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                id_receta UUID NOT NULL,
                                id_producto UUID NOT NULL,
                                dosis VARCHAR(100) NOT NULL,
                                frecuencia VARCHAR(100) NOT NULL,
                                duracion_dias INTEGER NOT NULL,
                                CONSTRAINT fk_rd_receta FOREIGN KEY (id_receta) REFERENCES receta(id_receta) ON DELETE CASCADE,
                                CONSTRAINT fk_rd_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE RESTRICT
);

-- =========================================================================
-- 7. MÓDULO FACTURACIÓN Y AUDITORÍA
-- =========================================================================

CREATE TABLE factura (
                         id_factura UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         id_empresa UUID NOT NULL,
                         id_cliente UUID NOT NULL,
                         id_usuario UUID NOT NULL, -- Usuario que cobra/emite
                         total NUMERIC(10,2) DEFAULT 0.00 NOT NULL,
                         estado VARCHAR(20) DEFAULT 'Emitida' NOT NULL, -- Emitida, Anulada
                         fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         CONSTRAINT fk_fact_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                         CONSTRAINT fk_fact_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
                         CONSTRAINT fk_fact_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT
);

CREATE TABLE factura_detalle (
                                 id_detalle UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 id_factura UUID NOT NULL,
                                 id_producto UUID NOT NULL,
                                 id_iva INTEGER NOT NULL, -- Se guarda histórico del IVA aplicado al momento de vender
                                 cantidad INTEGER NOT NULL,
                                 precio_unitario NUMERIC(10,2) NOT NULL,
                                 subtotal NUMERIC(10,2) NOT NULL,
                                 CONSTRAINT fk_fd_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE CASCADE,
                                 CONSTRAINT fk_fd_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE RESTRICT,
                                 CONSTRAINT fk_fd_iva FOREIGN KEY (id_iva) REFERENCES sri_iva(id_iva) ON DELETE RESTRICT
);

CREATE TABLE movimiento_inventario (
                                       id_movimiento UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       id_empresa UUID NOT NULL,
                                       id_producto UUID NOT NULL,
                                       id_factura UUID, -- Opcional: Puede ser nulo si es un ajuste manual o ingreso por proveedor
                                       tipo VARCHAR(20) NOT NULL, -- P.ej: 'Ingreso', 'Egreso', 'Venta', 'Ajuste'
                                       cantidad INTEGER NOT NULL,
                                       fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       CONSTRAINT fk_mov_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
                                       CONSTRAINT fk_mov_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE CASCADE,
                                       CONSTRAINT fk_mov_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE SET NULL
);

-- =========================================================================
-- 8. ÍNDICES DE RENDIMIENTO
-- =========================================================================
-- Multiempresa: toda consulta filtra por id_empresa, por eso cada tabla de
-- negocio tiene su índice. En las tablas que además ordenan por fecha se usa
-- un índice compuesto (id_empresa, fecha) para evitar un sort extra.
-- Historial_clinico.id_cita y receta.id_historial ya están indexados por sus
-- restricciones UNIQUE; no se repiten aquí.
CREATE INDEX IF NOT EXISTS idx_usuario_empresa ON usuario(id_empresa);
CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuario(id_rol);
CREATE INDEX IF NOT EXISTS idx_cliente_empresa ON cliente(id_empresa);
CREATE INDEX IF NOT EXISTS idx_mascota_empresa ON mascota(id_empresa);
CREATE INDEX IF NOT EXISTS idx_mascota_cliente ON mascota(id_cliente);
CREATE INDEX IF NOT EXISTS idx_mascota_vacuna_mascota ON mascota_vacuna(id_mascota);
CREATE INDEX IF NOT EXISTS idx_producto_empresa ON producto(id_empresa);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(id_categoria);
CREATE INDEX IF NOT EXISTS idx_producto_iva ON producto(id_iva);
CREATE INDEX IF NOT EXISTS idx_cita_empresa_fecha ON cita(id_empresa, fecha_hora);
CREATE INDEX IF NOT EXISTS idx_cita_mascota ON cita(id_mascota);
CREATE INDEX IF NOT EXISTS idx_cita_veterinario ON cita(id_veterinario);
CREATE INDEX IF NOT EXISTS idx_historial_empresa ON historial_clinico(id_empresa);
CREATE INDEX IF NOT EXISTS idx_receta_empresa ON receta(id_empresa);
CREATE INDEX IF NOT EXISTS idx_receta_detalle_receta ON receta_detalle(id_receta);
CREATE INDEX IF NOT EXISTS idx_factura_empresa_fecha ON factura(id_empresa, fecha DESC);
CREATE INDEX IF NOT EXISTS idx_factura_cliente ON factura(id_cliente);
CREATE INDEX IF NOT EXISTS idx_factura_detalle_factura ON factura_detalle(id_factura);
CREATE INDEX IF NOT EXISTS idx_movimiento_empresa_fecha ON movimiento_inventario(id_empresa, fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_producto ON movimiento_inventario(id_producto);
CREATE INDEX IF NOT EXISTS idx_veterinario_empresa ON veterinario(id_empresa);

-- =========================================================================
-- DATOS INICIALES (INSTALACIÓN LIMPIA)
-- =========================================================================
-- Contenido mínimo para arrancar la aplicación desde cero:
--   * Catálogos base (IVA, especie, raza, vacuna).
--   * Todos los roles con sus permisos.
--   * La Empresa Matriz y el SuperUsuario.
-- No se cargan datos de prueba (clientes, mascotas, citas, productos,
-- facturas, etc.): la base queda lista para que el SuperUsuario cree
-- veterinarias desde la propia aplicación.
-- =========================================================================

-- =========================================================================
-- 1. TABLAS MAESTRAS GLOBALES
-- =========================================================================

INSERT INTO sri_iva (porcentaje, codigo_sri) VALUES
                                                 (0.00, '0'),
                                                 (15.00, '4'), -- IVA 15% (Ecuador actual)
                                                 (5.00, '5');

INSERT INTO especie (nombre) VALUES
                                 ('Canina'),
                                 ('Felina'),
                                 ('Ave'),
                                 ('Roedor'),
                                 ('Reptil');

INSERT INTO raza (id_especie, nombre) VALUES
                                          (1, 'Golden Retriever'),
                                          (1, 'Pastor Alemán'),
                                          (2, 'Siamés'),
                                          (2, 'Persa'),
                                          (3, 'Canario');

INSERT INTO vacuna (nombre) VALUES
                                ('Antirrábica'),
                                ('Parvovirus'),
                                ('Triple Felina'),
                                ('Quíntuple Canina'),
                                ('Leucemia Felina');

-- =========================================================================
-- 2. SEGURIDAD Y ROLES (RBAC)
-- =========================================================================

-- Roles del sistema: los 5 operativos de una veterinaria + SuperUsuario
-- (nivel plataforma) + Farmacéutico.
INSERT INTO rol (nombre) VALUES
                             ('Administrador Global'),
                             ('Administrador Local'),
                             ('Veterinario'),
                             ('Recepcionista'),
                             ('Asistente Clínico'),
                             ('SuperUsuario'),
                             ('Farmacéutico')
    ON CONFLICT DO NOTHING;

-- =========================================================================
-- CATÁLOGO DE PERMISOS
-- =========================================================================
-- Módulos/acciones que valida Autorizacion.exigir(req, modulo, accion) en los
-- Servlets del backend. Idempotente: se puede ejecutar varias veces.
INSERT INTO permiso (modulo, accion)
SELECT modulo, accion FROM (VALUES
    ('CATEGORIAS','LISTAR'),  ('CATEGORIAS','VER'),  ('CATEGORIAS','CREAR'),  ('CATEGORIAS','EDITAR'),  ('CATEGORIAS','ELIMINAR'),
    ('CITAS','LISTAR'),       ('CITAS','VER'),       ('CITAS','CREAR'),       ('CITAS','EDITAR'),       ('CITAS','ELIMINAR'),
    ('CLIENTES','LISTAR'),    ('CLIENTES','VER'),    ('CLIENTES','CREAR'),    ('CLIENTES','EDITAR'),    ('CLIENTES','ELIMINAR'),
    ('HISTORIALES','LISTAR'), ('HISTORIALES','VER'), ('HISTORIALES','CREAR'), ('HISTORIALES','EDITAR'), ('HISTORIALES','ELIMINAR'),
    ('MASCOTAS','LISTAR'),    ('MASCOTAS','VER'),    ('MASCOTAS','CREAR'),    ('MASCOTAS','EDITAR'),    ('MASCOTAS','ELIMINAR'),
    ('INVENTARIO','LISTAR'),  ('INVENTARIO','VER'),  ('INVENTARIO','CREAR'),  ('INVENTARIO','EDITAR'),  ('INVENTARIO','ELIMINAR'),
    ('PRODUCTOS','LISTAR'),   ('PRODUCTOS','VER'),   ('PRODUCTOS','CREAR'),   ('PRODUCTOS','EDITAR'),   ('PRODUCTOS','ELIMINAR'),
    ('USUARIOS','LISTAR'),    ('USUARIOS','VER'),    ('USUARIOS','CREAR'),    ('USUARIOS','EDITAR'),    ('USUARIOS','ELIMINAR'),
    ('VETERINARIOS','LISTAR'),('VETERINARIOS','VER'),('VETERINARIOS','CREAR'),('VETERINARIOS','EDITAR'),('VETERINARIOS','ELIMINAR'),
    ('FACTURAS','LISTAR'),    ('FACTURAS','VER'),    ('FACTURAS','CREAR'),    ('FACTURAS','EDITAR'),    ('FACTURAS','ELIMINAR'), ('FACTURAS','EMITIR'),
    ('RECETAS','LISTAR'),     ('RECETAS','VER'),     ('RECETAS','CREAR'),     ('RECETAS','EDITAR'),     ('RECETAS','ELIMINAR'),
    ('EMPRESAS','LISTAR'),    ('EMPRESAS','VER'),    ('EMPRESAS','CREAR'),    ('EMPRESAS','EDITAR'),    ('EMPRESAS','ACTIVAR'),  ('EMPRESAS','DESACTIVAR')
) AS catalogo(modulo, accion)
    ON CONFLICT DO NOTHING;

-- =========================================================================
-- ASIGNACIÓN DE PERMISOS POR ROL
-- =========================================================================

-- SuperUsuario: TODOS los permisos (nivel plataforma).
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         CROSS JOIN permiso p
WHERE r.nombre = 'SuperUsuario'
    ON CONFLICT DO NOTHING;

-- Administrador Global / Administrador Local: acceso completo a los módulos
-- clínicos y comerciales de su propia empresa. EMPRESAS queda reservado a
-- SuperUsuario (el backend lo exige a nivel de código).
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON p.modulo IN (
    'CATEGORIAS','CITAS','CLIENTES','HISTORIALES','MASCOTAS',
    'INVENTARIO','PRODUCTOS','USUARIOS','VETERINARIOS','FACTURAS','RECETAS'
)
WHERE r.nombre IN ('Administrador Global', 'Administrador Local')
    ON CONFLICT DO NOTHING;

-- Veterinario: agenda propia, historiales y recetas; consulta de
-- clientes/mascotas y de stock de productos para prescribir.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CITAS'        AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'HISTORIALES'  AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'RECETAS'      AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS'     AND p.accion IN ('LISTAR','VER','EDITAR'))
        OR (p.modulo = 'CLIENTES'     AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'PRODUCTOS'    AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'VETERINARIOS' AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Veterinario'
    ON CONFLICT DO NOTHING;

-- Recepcionista: agenda, clientes y mascotas completos, y factura al cliente.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CITAS'        AND p.accion IN ('LISTAR','VER','CREAR','EDITAR','ELIMINAR'))
        OR (p.modulo = 'CLIENTES'     AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS'     AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'FACTURAS'     AND p.accion IN ('LISTAR','VER','CREAR','EMITIR'))
        OR (p.modulo = 'VETERINARIOS' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'PRODUCTOS'    AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'HISTORIALES'  AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Recepcionista'
    ON CONFLICT DO NOTHING;

-- Asistente Clínico: igual que Recepcionista pero SIN facturación.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CITAS'        AND p.accion IN ('LISTAR','VER','CREAR','EDITAR','ELIMINAR'))
        OR (p.modulo = 'CLIENTES'     AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS'     AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'VETERINARIOS' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'PRODUCTOS'    AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'HISTORIALES'  AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Asistente Clínico'
    ON CONFLICT DO NOTHING;

-- Farmacéutico: inventario/productos, caja y apoyo en agenda.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CATEGORIAS'   AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'CLIENTES'     AND p.accion IN ('LISTAR','VER','CREAR'))
        OR (p.modulo = 'FACTURAS'     AND p.accion IN ('LISTAR','VER','EMITIR'))
        OR (p.modulo = 'INVENTARIO'   AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'PRODUCTOS'    AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'CITAS'        AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS'     AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'HISTORIALES'  AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'USUARIOS'     AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Farmacéutico'
    ON CONFLICT DO NOTHING;

-- =========================================================================
-- EMPRESA MATRIZ (hogar del SuperUsuario)
-- =========================================================================
-- Empresa técnica del sistema: no es una veterinaria real. Se conserva para
-- que el SuperUsuario tenga un id_empresa válido (usuario.id_empresa es NOT
-- NULL y con ON DELETE RESTRICT).
INSERT INTO empresa (id_empresa, ruc, razon_social, direccion, correo, telefono, activo)
VALUES (
           '00000000-0000-0000-0000-000000000000',
           '9999999999001',
           'ADMINISTRADOR',
           'ADMIN',
           'soporte@veterinariaitq.com',
           '0999999999',
           TRUE
       )
    ON CONFLICT DO NOTHING;

-- =========================================================================
-- SUPERUSUARIO
-- =========================================================================
-- Cuenta maestra de la plataforma. Todos los datos de la cuenta se declaran
-- explícitamente (estado de cuenta activo, sin bloqueos ni notificaciones).
INSERT INTO usuario (
    id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres,
    activo, estado_cuenta, tipobloqueo, notificacion
)
VALUES (
           'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f00',
           '00000000-0000-0000-0000-000000000000', -- Empresa Matriz
           (SELECT id_rol FROM rol WHERE nombre = 'SuperUsuario'),
           'superadmin',
           '$2a$12$y8Cvy6PssoEjD3ockjHv.Or0Nm9ALRg6UC8QhUbALNi2o0D5GdamO', -- Hash BCrypt de la contraseña 'Prueba123!'
           'Administrador del Sistema Global',
           TRUE,
           'activo',
           NULL,
           NULL
       )
    ON CONFLICT DO NOTHING;


-- =========================================================================
-- SELECT POR CADA TABLA
-- =========================================================================

-- 1. SRI_IVA
SELECT * FROM sri_iva;

-- 2. ESPECIE
SELECT * FROM especie;

-- 3. RAZA
SELECT * FROM raza;

-- 4. VACUNA
SELECT * FROM vacuna;

-- 5. ROL
SELECT * FROM rol;

-- 6. PERMISO
SELECT * FROM permiso;

-- 7. ROL_PERMISO
SELECT * FROM rol_permiso;

-- 8. EMPRESA
SELECT * FROM empresa;

-- 9. USUARIO
SELECT * FROM usuario;

-- 10. VETERINARIO
SELECT * FROM veterinario;

-- 11. CLIENTE
SELECT * FROM cliente;

-- 12. MASCOTA
SELECT * FROM mascota;

-- 13. MASCOTA_VACUNA
SELECT * FROM mascota_vacuna;

-- 14. CITA
SELECT * FROM cita;

-- 15. HISTORIAL_CLINICO
SELECT * FROM historial_clinico;

-- 16. RECETA
SELECT * FROM receta;

-- 17. RECETA_DETALLE
SELECT * FROM receta_detalle;

-- 18. CATEGORIA
SELECT * FROM categoria;

-- 19. PRODUCTO
SELECT * FROM producto;

-- 20. FACTURA
SELECT * FROM factura;

-- 21. FACTURA_DETALLE
SELECT * FROM factura_detalle;

-- 22. MOVIMIENTO_INVENTARIO
SELECT * FROM movimiento_inventario;

DELETE FROM public.empresa
WHERE ruc='0869322530001';

DELETE FROM public.usuario
WHERE id_usuario='eeefde11-62a9-4525-9bdf-72c0ebe80684';