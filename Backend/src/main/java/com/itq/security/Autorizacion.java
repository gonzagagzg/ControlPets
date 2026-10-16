package com.itq.security;

import com.itq.config.ConexionBD;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Autorizacion {

    private Autorizacion() {
    }

    public static void exigir(
            HttpServletRequest req,
            String modulo,
            String accion
    ) {

        Integer idRol =
                (Integer) req.getAttribute("idRol");

        String rol =
                (String) req.getAttribute("rol");

        if (idRol == null) {
            throw new SecurityException(
                    "No se pudo determinar el rol del usuario"
            );
        }

        /*
         * El SuperUsuario tiene acceso global.
         */
        if (rol != null &&
                rol.equalsIgnoreCase("SuperUsuario")) {

            return;
        }

        try {

            if (!permite(
                    idRol,
                    modulo,
                    accion
            )) {

                throw new SecurityException(
                        "No tiene permisos para realizar esta operación"
                );
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "No se pudo verificar la autorización",
                    e
            );
        }
    }

    public static boolean permite(
            Integer idRol,
            String modulo,
            String accion
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM rol_permiso rp
                INNER JOIN permiso p
                    ON p.id_permiso = rp.id_permiso
                WHERE rp.id_rol = ?
                  AND UPPER(p.modulo) = UPPER(?)
                  AND UPPER(p.accion) = UPPER(?)
                LIMIT 1
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setInt(1, idRol);
            ps.setString(2, modulo);
            ps.setString(3, accion);

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }
        }
    }
}