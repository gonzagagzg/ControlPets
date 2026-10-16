-- Ejecutar sobre bases de datos ya existentes que fueron creadas antes de
-- agregar correo/telefono a la tabla empresa (ver Script_Aplicacion_Veterinaria.sql).
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS correo VARCHAR(100);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS telefono VARCHAR(15);
