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

/*
 * Sistemas Multiempresa o SaaS
El sistema está diseñado para e muchas Empresas
compartan la misma base de datos si usaras números simples (1, 2, 3),
la Empresa A tendría al Cliente 1 y la Empresa B tendría al Cliente 2.
Si en el futuro necesitas mover datos, sincronizar sucursales offline o
fusionar información, los números chocarían de inmediato. Los UUID son
únicos en todo el universo; jamás se van a repetir entre empresas.*/


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
-- DATOS DE PRUEBA PARA EL APLICATIVO
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
-- 2. SEGURIDAD Y ROLES
-- =========================================================================

INSERT INTO rol (nombre) VALUES
                             ('Administrador Global'),
                             ('Administrador Local'),
                             ('Veterinario'),
                             ('Recepcionista'),
                             ('Asistente Clínico');

INSERT INTO permiso (modulo, accion) VALUES
                                         ('USUARIOS', 'CREAR'),
                                         ('MASCOTAS', 'VER'),
                                         ('Historial', 'Editar'),
                                         ('FACTURAS', 'EMITIR'),
                                         ('Inventario', 'Ajustar');

INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
                                                 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- Admin Global tiene todo
                                                 (3, 2), (3, 3),                         -- Veterinario: Ver mascotas y Editar historial
                                                 (4, 2), (4, 4);                         -- Recepcionista: Ver mascotas y Emitir facturas

-- =========================================================================
-- 3. MULTIEMPRESA
-- =========================================================================

-- Creamos variables simuladas para los IDs principales usando CTEs en cascada
-- Para que el script corra directo, insertamos con UUIDs fijos predecibles creados al azar:

INSERT INTO empresa (id_empresa, ruc, razon_social, direccion) VALUES
                                                                   ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1792457812001', 'Corporación VetCare S.A.', 'Av. Amazonas y Shyris, Quito'),
                                                                   ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0992345678001', 'PetSmile S.A.', 'Av. Carlos Julio Arosemena, Guayaquil'),
                                                                   ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', '0102345678001', 'Clínica Veterinaria El Establo', 'Calle Larga, Cuenca'),
                                                                   ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '1891234567001', 'Huellas Clinic', 'Av. Cevallos, Ambato'),
                                                                   ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', '1391234567001', 'Dany Pets', 'Malecón Tarqui, Manta');

-- =========================================================================
-- 4. USUARIOS, PERSONAL Y CLIENTES
-- =========================================================================

-- Contraseña de todos los usuarios de prueba: Prueba123!
-- (hash BCrypt real, cost 12, compatible con PasswordUtil.verificar del backend)
INSERT INTO usuario (id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres) VALUES
                                                                                       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 2, 'admin_quito', '$2a$12$y8Cvy6PssoEjD3ockjHv.Or0Nm9ALRg6UC8QhUbALNi2o0D5GdamO', 'Carlos Mendoza'),
                                                                                       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 3, 'doc_juarez', '$2a$12$AMw7LP.eKitsRLxZPlbM7uFUAAg.QJ/atKaiCgzXx2st1uztTL/v2', 'Ana Juárez'),
                                                                                       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 2, 'admin_gye', '$2a$12$zO6jKrUO1d7KCheik.0GqOGVt0rjDw7sxGAbkLGtQ8ZEfGVb4m9e6', 'Pedro Morales'),
                                                                                       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 3, 'doc_silva', '$2a$12$naeqCZu1OnwtR7ni45ceteXhZm/S50Iix5Mijwgpwf2qLoHBQJOwi', 'Luis Silva'),
                                                                                       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 4, 'recep_lucia', '$2a$12$wpvM4sowy9ulD19fTgX1fu68j.ZbyirO1eo.eAOPVtUowv2m6tBOq', 'Lucía Torres');

INSERT INTO veterinario (id_veterinario, id_usuario, id_empresa, especialidad) VALUES
                                                                                   ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Cirugía de Tejidos Blandos'),
                                                                                   ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Dermatología Felina'),
-- Generamos otros 3 aleatorios vinculados a los administradores para llenar la tabla (fines didácticos)
                                                                                   ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Medicina General'),
                                                                                   ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Cardiología'),
                                                                                   ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Nutrición Animal');

INSERT INTO cliente (id_cliente, id_empresa, identificacion, nombres) VALUES
                                                                          ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1723456789', 'Juan Pérez'),
                                                                          ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1755543210', 'María Flores'),
                                                                          ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0912345678', 'Roberto Gómez'),
                                                                          ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0928765432', 'Elena Castro'),
                                                                          ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', '0104567890', 'Diego Andrade');

-- =========================================================================
-- 5. MÓDULO CLÍNICO
-- =========================================================================

INSERT INTO mascota (id_mascota, id_empresa, id_cliente, id_raza, nombre, fecha_nacimiento) VALUES
                                                                                                ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 1, 'Max', '2021-05-10'),
                                                                                                ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 3, 'Luna', '2022-11-18'),
                                                                                                ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 2, 'Rocky', '2019-02-03'),
                                                                                                ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 4, 'Mimi', '2023-01-25'),
                                                                                                ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 1, 'Toby', '2024-06-14');

INSERT INTO mascota_vacuna (id_empresa, id_mascota, id_vacuna, fecha_aplicacion) VALUES
                                                                                     ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 1, '2025-01-10'),
                                                                                     ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 4, '2025-02-10'),
                                                                                     ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 3, '2025-03-15'),
                                                                                     ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 1, '2025-01-20'),
                                                                                     ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 5, '2025-04-02');

INSERT INTO cita (id_cita, id_empresa, id_mascota, id_veterinario, fecha_hora, estado) VALUES
                                                                                           ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '2026-07-10 09:30:00 -05', 'Atendida'),
                                                                                           ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '2026-07-11 10:00:00 -05', 'Atendida'),
                                                                                           ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', '2026-07-12 14:00:00 -05', 'Pendiente'),
                                                                                           ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', '2026-07-12 15:00:00 -05', 'Pendiente'),
                                                                                           ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e55', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '2026-07-13 11:30:00 -05', 'Pendiente');

INSERT INTO historial_clinico (id_historial, id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico) VALUES
                                                                                                                      ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f11', 25.50, 38.5, 'Presenta vómito y decaimiento desde hace 24 horas.', 'Gastroenteritis bacteriana leve.'),
                                                                                                                      ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f22', 4.20, 39.1, 'Prurito excesivo en la zona de las orejas y descamación.', 'Otitis externa por ácaros.'),
-- Agregamos tres registros dummy en base a IDs manuales para cumplir la cuota sin romper el UNIQUE en id_cita
                                                                                                                      ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380333', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f33', 12.00, 38.2, 'Control rutinario anual.', 'Mascota sana clínicamente.'),
                                                                                                                      ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380444', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f44', 3.10, 38.8, 'Cachorro para primera evaluación.', 'Sano, se sugiere plan vacunal.'),
                                                                                                                      ('50eebc99-9c0b-4ef8-bb6d-6bb9bd380555', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f55', 8.40, 37.9, 'Cojera en miembro posterior izquierdo.', 'Sospecha de luxación patelar.');

-- =========================================================================
-- 6. MÓDULO INVENTARIO Y PRODUCTOS
-- =========================================================================

INSERT INTO categoria (id_categoria, id_empresa, nombre) VALUES
                                                             (1, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Fármacos y Antibióticos'),
                                                             (2, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alimentos Premium'),
                                                             (3, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Accesorios y Juguetes'),
                                                             (4, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Antiparasitarios'),
                                                             (5, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Estética Canina');

INSERT INTO producto (id_producto, id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad) VALUES
                                                                                                                                               ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1, 2, 'Apoquel 5.4mg x 20 tabletas', 45.00, 15, 3, '2027-12-31'),
                                                                                                                                               ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 2, 1, 'Hills Metabolic Canino 3kg', 38.50, 8, 2, '2026-10-15'),
                                                                                                                                               ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 4, 2, 'Bravecto Perros 10-20kg', 32.00, 24, 5, '2028-04-20'),
                                                                                                                                               ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380644', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 3, 2, 'Collar Reflectivo Ajustable', 8.50, 50, 10, NULL),
                                                                                                                                               ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1, 2, 'Amoxicilina Suspensión Oral', 12.00, 10, 4, '2026-09-01');

INSERT INTO receta (id_receta, id_empresa, id_historial, indicaciones_generales) VALUES
                                                                                     ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380711', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '10eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'Mantener en reposo, dar agua hervida a libre acceso.'),
                                                                                     ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380722', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '20eebc99-9c0b-4ef8-bb6d-6bb9bd380222', 'Limpiar el pabellón auricular antes de aplicar el fármaco.'),
                                                                                     ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380733', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '30eebc99-9c0b-4ef8-bb6d-6bb9bd380333', 'Continuar con dieta habitual basada en croquetas premium.'),
                                                                                     ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380744', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '40eebc99-9c0b-4ef8-bb6d-6bb9bd380444', 'Evitar contacto con otras mascotas no vacunadas.'),
                                                                                     ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380755', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '50eebc99-9c0b-4ef8-bb6d-6bb9bd380555', 'Restringir saltos y ejercicios bruscos.');

INSERT INTO receta_detalle (id_receta, id_producto, dosis, frecuencia, duracion_dias) VALUES
                                                                                          ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380711', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '2.5 ml', 'Cada 12 horas', 7),
                                                                                          ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380722', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', '1/2 tableta', 'Cada 24 horas', 10),
                                                                                          ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380733', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', '200 gramos', 'Cada 8 horas', 30),
                                                                                          ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380744', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', '1 tableta masticable', 'Dosis única', 1),
                                                                                          ('70eebc99-9c0b-4ef8-bb6d-6bb9bd380755', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '1.5 ml', 'Cada 24 horas', 5);

-- =========================================================================
-- 7. MÓDULO FACTURACIÓN Y AUDITORÍA
-- =========================================================================

INSERT INTO factura (id_factura, id_empresa, id_cliente, id_usuario, total, estado) VALUES
                                                                                        ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 57.00, 'Emitida'),
                                                                                        ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 38.50, 'Emitida'),
                                                                                        ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 32.00, 'Emitida'),
                                                                                        ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380844', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 17.00, 'Anulada'),
                                                                                        ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380855', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 45.00, 'Emitida');

INSERT INTO factura_detalle (id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal) VALUES
                                                                                                       ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', 2, 1, 12.00, 12.00),
                                                                                                       ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', 2, 1, 45.00, 45.00),
                                                                                                       ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', 1, 1, 38.50, 38.50),
                                                                                                       ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', 2, 1, 32.00, 32.00),
                                                                                                       ('80eebc99-9c0b-4ef8-bb6d-6bb9bd380844', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380644', 2, 2, 8.50, 17.00);

INSERT INTO movimiento_inventario (id_empresa, id_producto, id_factura, tipo, cantidad) VALUES
                                                                                            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'Venta', 1),
                                                                                            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'Venta', 1),
                                                                                            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', 'Venta', 1),
                                                                                            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', 'Venta', 1),
                                                                                            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', NULL, 'Ingreso', 50);



--ver tablas de BD
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;



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


-- =========================================================================
-- SELECT PARA RELACIONAR DATOS DE FORMA FÁCIL
-- =========================================================================

-- Listar las razas junto con el nombre de su especie (JOIN)
SELECT r.id_raza, r.nombre AS raza, e.nombre AS especie
FROM raza r
         INNER JOIN especie e ON r.id_especie = e.id_especie;

--Seguridad: Usuarios con sus Roles y Permisos

SELECT
    u.usuario,
    u.nombres AS nombre_usuario,
    r.nombre AS rol_asignado,
    p.modulo,
    p.accion
FROM usuario u
         INNER JOIN rol r ON u.id_rol = r.id_rol
         INNER JOIN rol_permiso rp ON r.id_rol = rp.id_rol
         INNER JOIN permiso p ON rp.id_permiso = p.id_permiso
WHERE u.id_empresa = 'Inserta la empresa';


--Clínico: Expediente Completo de la Mascota

SELECT
    m.id_mascota,
    m.nombre AS nombre_mascota,
    m.fecha_nacimiento,
    c.nombres AS nombre_dueno,
    c.identificacion AS cedula_dueno,
    r.nombre AS raza,
    e.nombre AS especie
FROM mascota m
         INNER JOIN cliente c ON m.id_cliente = c.id_cliente
         INNER JOIN raza r ON m.id_raza = r.id_raza
         INNER JOIN especie e ON r.id_especie = e.id_especie
WHERE m.id_empresa = 'Inserta la empresa';

--Citas: Agenda de Atenciones
SELECT
    c.id_cita,
    c.fecha_hora,
    c.estado AS estado_cita,
    m.nombre AS nombre_mascota,
    uv.nombres AS nombre_veterinario,
    v.especialidad
FROM cita c
         INNER JOIN mascota m ON c.id_mascota = m.id_mascota
         INNER JOIN veterinario v ON c.id_veterinario = v.id_veterinario
         INNER JOIN usuario uv ON v.id_usuario = uv.id_usuario
WHERE c.id_empresa = 'inserta la empresa'
ORDER BY c.fecha_hora ASC;

--Citas: Agenda de Atenciones

SELECT
    c.id_cita,
    c.fecha_hora,
    c.estado AS estado_cita,
    m.nombre AS nombre_mascota,
    uv.nombres AS nombre_veterinario,
    v.especialidad
FROM cita c
         INNER JOIN mascota m ON c.id_mascota = m.id_mascota
         INNER JOIN veterinario v ON c.id_veterinario = v.id_veterinario
         INNER JOIN usuario uv ON v.id_usuario = uv.id_usuario
WHERE c.id_empresa = 'inserta la empresa'
ORDER BY c.fecha_hora ASC;



---SUPERUSUARIO---


-- =========================================================================
-- EMPRESA MATRIZ PARA SUPERUSUARIO
-- =========================================================================

INSERT INTO empresa (id_empresa, ruc, razon_social, direccion, activo)
VALUES (
           '00000000-0000-0000-0000-000000000000',
           '9999999999001',
           'ADMINISTRADOR',
           'ADMIN',
           TRUE
       )
    ON CONFLICT (id_empresa) DO NOTHING;

-- =========================================================================
-- ROL DE SUPERUSUARIO Y PERMISOS
-- =========================================================================

INSERT INTO permiso (modulo, accion) VALUES
                                         ('EMPRESAS', 'CREAR'),
                                         ('EMPRESAS', 'VER'),
                                         ('EMPRESAS', 'EDITAR'),
                                         ('EMPRESAS', 'ELIMINAR'),
                                         ('Superadmin', 'AccesoTotal')
    ON CONFLICT (modulo, accion) DO NOTHING;

-- Insertamos el Rol de SuperUsuario
INSERT INTO rol (nombre)
VALUES ('SuperUsuario')
    ON CONFLICT (nombre) DO NOTHING;


-- =========================================================================
-- ROL FARMACÉUTICO
-- =========================================================================
-- Rol solicitado para el personal encargado del módulo de farmacia.
-- Se inserta después de SuperUsuario para conservar la numeración actual
-- de los roles existentes y permitir que Farmacéutico continúe como rol 7
-- en una instalación nueva que utilice estos datos de prueba.

INSERT INTO rol (nombre)
VALUES ('Farmacéutico')
    ON CONFLICT (nombre) DO NOTHING;


-- =========================================================================
-- PERMISOS PARA FARMACÉUTICO
-- =========================================================================
-- Se crean con la misma nomenclatura utilizada actualmente por el Backend.
-- No se utilizan IDs fijos; las relaciones se realizan por nombre/módulo/acción.

INSERT INTO permiso (modulo, accion) VALUES
                                         ('CATEGORIAS', 'LISTAR'),
                                         ('CATEGORIAS', 'VER'),
                                         ('CLIENTES', 'CREAR'),
                                         ('CLIENTES', 'LISTAR'),
                                         ('CLIENTES', 'VER'),
                                         ('FACTURAS', 'LISTAR'),
                                         ('FACTURAS', 'VER'),
                                         ('INVENTARIO', 'CREAR'),
                                         ('INVENTARIO', 'EDITAR'),
                                         ('INVENTARIO', 'LISTAR'),
                                         ('INVENTARIO', 'VER'),
                                         ('PRODUCTOS', 'CREAR'),
                                         ('PRODUCTOS', 'EDITAR'),
                                         ('PRODUCTOS', 'LISTAR'),
                                         ('PRODUCTOS', 'VER')
    ON CONFLICT (modulo, accion) DO NOTHING;

-- =========================================================================
-- ASIGNAR TODOS LOS PERMISOS AL SUPERUSUARIO
-- =========================================================================

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         CROSS JOIN permiso p
WHERE r.nombre = 'SuperUsuario'
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;


-- =========================================================================
-- ASIGNAR PERMISOS AL FARMACÉUTICO
-- =========================================================================

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT
    r.id_rol,
    p.id_permiso
FROM rol r
         JOIN permiso p
              ON (
                  (p.modulo = 'CATEGORIAS' AND p.accion IN ('LISTAR', 'VER'))
                      OR
                  (p.modulo = 'CLIENTES' AND p.accion IN ('CREAR', 'LISTAR', 'VER'))
                      OR
                  (p.modulo = 'FACTURAS' AND p.accion IN ('EMITIR', 'LISTAR', 'VER'))
                      OR
                  (p.modulo = 'INVENTARIO' AND p.accion IN ('CREAR', 'EDITAR', 'LISTAR', 'VER'))
                      OR
                  (p.modulo = 'PRODUCTOS' AND p.accion IN ('CREAR', 'EDITAR', 'LISTAR', 'VER'))
                      OR
                  (p.modulo = 'CITAS' AND p.accion IN ('LISTAR', 'VER', 'CREAR', 'EDITAR'))
                      OR
                  (p.modulo = 'MASCOTAS' AND p.accion IN ('LISTAR', 'VER', 'CREAR', 'EDITAR'))
                      OR
                  (p.modulo = 'HISTORIALES' AND p.accion IN ('LISTAR', 'VER'))
                      OR
                  (p.modulo = 'USUARIOS' AND p.accion IN ('LISTAR', 'VER'))
                  )
WHERE r.nombre = 'Farmacéutico'
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;

-- =========================================================================
-- CATÁLOGO COMPLETO DE PERMISOS
-- =========================================================================
-- Las 5 filas originales de más arriba ('USUARIOS'/'CREAR', 'MASCOTAS'/'VER',
-- 'Historial'/'Editar', 'FACTURAS'/'EMITIR', 'Inventario'/'Ajustar') no
-- cubren ni de cerca los módulos que realmente valida
-- Autorizacion.exigir(req, modulo, accion) en cada Servlet — y dos de ellas
-- ('Historial'/'Editar', 'Inventario'/'Ajustar') ni siquiera coinciden en
-- mayúsculas/nombre con los módulos reales ('HISTORIALES', 'INVENTARIO'),
-- por lo que jamás conceden nada. Resultado: salvo Farmacéutico y
-- SuperUsuario, ningún rol podía operar casi ningún módulo ("No tiene
-- permisos para realizar esta operación").
--
-- Este bloque agrega el catálogo real (LISTAR/VER/CREAR/EDITAR/ELIMINAR por
-- módulo, más EMITIR en FACTURAS y ACTIVAR/DESACTIVAR en EMPRESAS) leído
-- directamente de cada Servlet. Es aditivo e idempotente (ON CONFLICT DO
-- NOTHING): no borra las filas viejas, solo completa lo que falta.

INSERT INTO permiso (modulo, accion)
SELECT modulo, accion FROM (VALUES
    ('CATEGORIAS','LISTAR'), ('CATEGORIAS','VER'), ('CATEGORIAS','CREAR'), ('CATEGORIAS','EDITAR'), ('CATEGORIAS','ELIMINAR'),
    ('CITAS','LISTAR'),      ('CITAS','VER'),      ('CITAS','CREAR'),      ('CITAS','EDITAR'),      ('CITAS','ELIMINAR'),
    ('CLIENTES','LISTAR'),   ('CLIENTES','VER'),   ('CLIENTES','CREAR'),   ('CLIENTES','EDITAR'),   ('CLIENTES','ELIMINAR'),
    ('HISTORIALES','LISTAR'),('HISTORIALES','VER'),('HISTORIALES','CREAR'),('HISTORIALES','EDITAR'),('HISTORIALES','ELIMINAR'),
    ('MASCOTAS','LISTAR'),   ('MASCOTAS','VER'),   ('MASCOTAS','CREAR'),   ('MASCOTAS','EDITAR'),   ('MASCOTAS','ELIMINAR'),
    ('INVENTARIO','LISTAR'), ('INVENTARIO','VER'), ('INVENTARIO','CREAR'), ('INVENTARIO','EDITAR'), ('INVENTARIO','ELIMINAR'),
    ('PRODUCTOS','LISTAR'),  ('PRODUCTOS','VER'),  ('PRODUCTOS','CREAR'),  ('PRODUCTOS','EDITAR'),  ('PRODUCTOS','ELIMINAR'),
    ('USUARIOS','LISTAR'),   ('USUARIOS','VER'),   ('USUARIOS','CREAR'),   ('USUARIOS','EDITAR'),   ('USUARIOS','ELIMINAR'),
    ('VETERINARIOS','LISTAR'),('VETERINARIOS','VER'),('VETERINARIOS','CREAR'),('VETERINARIOS','EDITAR'),('VETERINARIOS','ELIMINAR'),
    ('FACTURAS','LISTAR'),   ('FACTURAS','VER'),   ('FACTURAS','CREAR'),   ('FACTURAS','EDITAR'),   ('FACTURAS','ELIMINAR'), ('FACTURAS','EMITIR'),
    ('EMPRESAS','LISTAR'),   ('EMPRESAS','VER'),   ('EMPRESAS','CREAR'),   ('EMPRESAS','EDITAR'),   ('EMPRESAS','ACTIVAR'), ('EMPRESAS','DESACTIVAR'),
    ('RECETAS','LISTAR'),    ('RECETAS','VER'),    ('RECETAS','CREAR'),    ('RECETAS','EDITAR'),    ('RECETAS','ELIMINAR')
) AS catalogo(modulo, accion)
ON CONFLICT (modulo, accion) DO NOTHING;

-- =========================================================================
-- ASIGNAR PERMISOS A ADMINISTRADOR GLOBAL Y ADMINISTRADOR LOCAL
-- =========================================================================
-- Gerencia/administración de una veterinaria: acceso completo a todos los
-- módulos clínicos y comerciales de su propia empresa. EMPRESAS queda fuera
-- a propósito: ese módulo está reservado a SuperUsuario a nivel de código
-- (EmpresaServlet.exigirSuperUsuario), con permiso o sin él.

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON p.modulo IN (
    'CATEGORIAS','CITAS','CLIENTES','HISTORIALES','MASCOTAS',
    'INVENTARIO','PRODUCTOS','USUARIOS','VETERINARIOS','FACTURAS','RECETAS'
)
WHERE r.nombre IN ('Administrador Global', 'Administrador Local')
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;

-- =========================================================================
-- ASIGNAR PERMISOS A VETERINARIO
-- =========================================================================
-- Atención clínica: agenda propia, historiales y recetas, consulta de
-- clientes/mascotas y de stock de productos para prescribir — sin gestionar
-- usuarios, empresas ni facturación.

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CITAS' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'HISTORIALES' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'RECETAS' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS' AND p.accion IN ('LISTAR','VER','EDITAR'))
        OR (p.modulo = 'CLIENTES' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'PRODUCTOS' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'VETERINARIOS' AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Veterinario'
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;

-- =========================================================================
-- ASIGNAR PERMISOS A RECEPCIONISTA
-- =========================================================================
-- Front desk: agenda, clientes y mascotas completos, factura al cliente
-- (incluye EMITIR), consulta de veterinarios/productos/historiales — sin
-- editar historias clínicas ni gestionar usuarios/inventario.

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON (
        (p.modulo = 'CITAS' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR','ELIMINAR'))
        OR (p.modulo = 'CLIENTES' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'MASCOTAS' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR'))
        OR (p.modulo = 'FACTURAS' AND p.accion IN ('LISTAR','VER','CREAR','EMITIR'))
        OR (p.modulo = 'VETERINARIOS' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'PRODUCTOS' AND p.accion IN ('LISTAR','VER'))
        OR (p.modulo = 'HISTORIALES' AND p.accion IN ('LISTAR','VER'))
    )
WHERE r.nombre = 'Recepcionista'
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;

-- =========================================================================
-- CREAR EL USUARIO SUPERADMINISTRADOR
-- =========================================================================
INSERT INTO usuario (id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres, activo)
VALUES (
           'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f00',
           '00000000-0000-0000-0000-000000000000', -- Pertenece a la Empresa Matriz
           (SELECT id_rol FROM rol WHERE nombre = 'SuperUsuario'),
           'superadmin',
           '$2a$12$eOkp1DxC8FKMywhrusTjruA6rAeGkMpGkztUEplyZh5bCSgA2MLA6', -- Hash BCrypt de la contraseña inicial
           'Administrador del Sistema Global',
           TRUE
       )
    ON CONFLICT (id_empresa, usuario) DO NOTHING;

-- =========================================================================
-- CREAR USUARIO DE ROL FARMACÉUTICO
-- =========================================================================
INSERT INTO usuario (id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres, activo)
VALUES (
           'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b66',
           'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
           (SELECT id_rol FROM rol WHERE nombre = 'Farmacéutico'),
           'farma_rocio',
           '$2a$12$y8Cvy6PssoEjD3ockjHv.Or0Nm9ALRg6UC8QhUbALNi2o0D5GdamO', -- Prueba123!
           'Rocío Paredes',
           TRUE
       )
    ON CONFLICT (id_empresa, usuario) DO NOTHING;


--Consultas para funciones del Superusuario

-- =========================================================================
-- ver veterinarias--
-- =========================================================================
-- Obtener todas las clínicas registradas y su resumen básico sirve para poner en tarjetas y seleccionar la clinica que desea administrar
SELECT
    e.id_empresa,
    e.ruc,
    e.razon_social,
    e.direccion,
    e.activo,
    COUNT(DISTINCT u.id_usuario) AS total_usuarios,
    COUNT(DISTINCT c.id_cliente) AS total_clientes
FROM empresa e
         LEFT JOIN usuario u ON e.id_empresa = u.id_empresa
         LEFT JOIN cliente c ON e.id_empresa = c.id_empresa
GROUP BY e.id_empresa, e.ruc, e.razon_social, e.direccion, e.activo
ORDER BY e.razon_social ASC;

-- =========================================================================
--ver tablas de veterinaria
-- =========================================================================
-- Conteo dinámico de registros por tabla para la empresa seleccionada
-- Reemplazar ':id_empresa' por el UUID de la clínica seleccionada con comillas simples '''estas aquí ''el id de empresa''''' para que
--funcione con la tarjeta anterior debes poner un escuchador que permita captar
--la opción tomada anteriormente por el usuario y ponerla en la consulta con un placeholder

SELECT 'Mascotas' AS modulo, COUNT(*) AS total_registros FROM mascota WHERE id_empresa = :id_empresa
UNION ALL
SELECT 'Citas' AS modulo, COUNT(*) AS total_registros FROM cita WHERE id_empresa = :id_empresa
UNION ALL
SELECT 'Historial Clínico' AS modulo, COUNT(*) AS total_registros FROM historial_clinico WHERE id_empresa = :id_empresa
UNION ALL
SELECT 'Facturas' AS modulo, COUNT(*) AS total_registros FROM factura WHERE id_empresa = :id_empresa
UNION ALL
SELECT 'Productos / Stock' AS modulo, COUNT(*) AS total_registros FROM producto WHERE id_empresa = :id_empresa
UNION ALL
SELECT 'Usuarios' AS modulo, COUNT(*) AS total_registros FROM usuario WHERE id_empresa = :id_empresa;

-- =========================================================================
--ver contenido de las tablas especificas
-- =========================================================================

-- 1. Si selecciona la tabla MASCOTAS:
SELECT
    m.id_mascota,
    m.nombre AS mascota,
    e.nombre AS especie,
    r.nombre AS raza,
    c.nombres AS dueno,
    c.identificacion AS cedula_dueno
FROM mascota m
         INNER JOIN cliente c ON m.id_cliente = c.id_cliente
         INNER JOIN raza r ON m.id_raza = r.id_raza
         INNER JOIN especie e ON r.id_especie = e.id_especie
WHERE m.id_empresa = :id_empresa;

-- 2. Si selecciona la tabla FACTURAS:
SELECT
    f.id_factura,
    f.fecha,
    f.total,
    f.estado,
    c.nombres AS cliente,
    u.nombres AS emitido_por
FROM factura f
         INNER JOIN cliente c ON f.id_cliente = c.id_cliente
         INNER JOIN usuario u ON f.id_usuario = u.id_usuario
WHERE f.id_empresa = :id_empresa
ORDER BY f.fecha DESC;

-- 3. Si selecciona la tabla INVENTARIO / PRODUCTOS:
SELECT
    p.id_producto,
    p.nombre AS producto,
    cat.nombre AS categoria,
    p.precio_unitario,
    p.stock_actual,
    p.stock_minimo,
    sri.porcentaje AS iva
FROM producto p
         INNER JOIN categoria cat ON p.id_categoria = cat.id_categoria
         INNER JOIN sri_iva sri ON p.id_iva = sri.id_iva
WHERE p.id_empresa = :id_empresa;

-- =========================================================================
-- VERIFICACIÓN DEL ROL FARMACÉUTICO Y PERMISOS
-- =========================================================================

-- Verificar que el rol Farmacéutico fue creado
SELECT id_rol, nombre
FROM rol
WHERE nombre = 'Farmacéutico';

-- Verificar los 16 permisos asignados al Farmacéutico
SELECT
    r.nombre AS rol,
    p.modulo,
    p.accion
FROM rol_permiso rp
         JOIN rol r ON r.id_rol = rp.id_rol
         JOIN permiso p ON p.id_permiso = rp.id_permiso
WHERE r.nombre = 'Farmacéutico'
ORDER BY p.modulo, p.accion;

-- Verificar que no existan permisos duplicados únicamente por
-- diferencias de mayúsculas/minúsculas.
SELECT
    LOWER(modulo) AS modulo_normalizado,
    LOWER(accion) AS accion_normalizada,
    COUNT(*) AS cantidad
FROM permiso
GROUP BY LOWER(modulo), LOWER(accion)
HAVING COUNT(*) > 1
ORDER BY LOWER(modulo), LOWER(accion);

