-- Ejecutar sobre bases de datos ya existentes: el módulo RECETAS nunca se
-- sembró en la tabla permiso, así que no aparecía en "Roles y Permisos"
-- ni podía asignarse a ningún rol (ver Script_Aplicacion_Veterinaria.sql).

INSERT INTO permiso (modulo, accion)
SELECT modulo, accion FROM (VALUES
    ('RECETAS','LISTAR'), ('RECETAS','VER'), ('RECETAS','CREAR'), ('RECETAS','EDITAR'), ('RECETAS','ELIMINAR')
) AS catalogo(modulo, accion)
ON CONFLICT (modulo, accion) DO NOTHING;

-- Administrador Global / Administrador Local: acceso completo a RECETAS.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON p.modulo = 'RECETAS'
WHERE r.nombre IN ('Administrador Global', 'Administrador Local')
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;

-- Veterinario: puede listar, ver, crear y editar recetas (no eliminar).
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
         JOIN permiso p ON p.modulo = 'RECETAS' AND p.accion IN ('LISTAR','VER','CREAR','EDITAR')
WHERE r.nombre = 'Veterinario'
    ON CONFLICT (id_rol, id_permiso) DO NOTHING;
