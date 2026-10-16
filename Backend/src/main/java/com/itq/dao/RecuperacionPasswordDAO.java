package com.itq.dao;

import com.itq.config.ConexionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class RecuperacionPasswordDAO {

    // =========================================================
    // GUARDAR TOKEN
    // =========================================================

    public void guardarToken(
            UUID idUsuario,
            String token,
            LocalDateTime fechaExpiracion
    ) throws SQLException {

        String sql = """
                INSERT INTO recuperacion_password
                (
                    id_usuario,
                    token,
                    fecha_expiracion,
                    utilizado
                )
                VALUES (?, ?, ?, FALSE)
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idUsuario
            );

            ps.setString(
                    2,
                    token
            );

            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(fechaExpiracion)
            );

            ps.executeUpdate();
        }
    }

    // =========================================================
    // INVALIDAR TOKENS ANTERIORES DEL USUARIO
    // =========================================================

    public void invalidarTokensAnteriores(
            UUID idUsuario
    ) throws SQLException {

        String sql = """
                UPDATE recuperacion_password
                SET utilizado = TRUE
                WHERE id_usuario = ?
                  AND utilizado = FALSE
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idUsuario
            );

            ps.executeUpdate();
        }
    }

    // =========================================================
    // BUSCAR TOKEN VÁLIDO
    // =========================================================

    public Optional<TokenRecuperacion> buscarTokenValido(
            String token
    ) throws SQLException {

        String sql = """
                SELECT id_recuperacion,
                       id_usuario,
                       token,
                       fecha_creacion,
                       fecha_expiracion,
                       utilizado
                FROM recuperacion_password
                WHERE token = ?
                  AND utilizado = FALSE
                  AND fecha_expiracion > CURRENT_TIMESTAMP
                LIMIT 1
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    token
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    return Optional.empty();
                }

                TokenRecuperacion resultado =
                        new TokenRecuperacion();

                resultado.setIdRecuperacion(
                        rs.getObject(
                                "id_recuperacion",
                                UUID.class
                        )
                );

                resultado.setIdUsuario(
                        rs.getObject(
                                "id_usuario",
                                UUID.class
                        )
                );

                resultado.setToken(
                        rs.getString(
                                "token"
                        )
                );

                Timestamp fechaCreacion =
                        rs.getTimestamp(
                                "fecha_creacion"
                        );

                if (fechaCreacion != null) {

                    resultado.setFechaCreacion(
                            fechaCreacion.toLocalDateTime()
                    );
                }

                Timestamp fechaExpiracion =
                        rs.getTimestamp(
                                "fecha_expiracion"
                        );

                if (fechaExpiracion != null) {

                    resultado.setFechaExpiracion(
                            fechaExpiracion.toLocalDateTime()
                    );
                }

                resultado.setUtilizado(
                        rs.getBoolean(
                                "utilizado"
                        )
                );

                return Optional.of(
                        resultado
                );
            }
        }
    }

    // =========================================================
    // MARCAR TOKEN COMO UTILIZADO
    // =========================================================

    public boolean marcarComoUtilizado(
            UUID idRecuperacion
    ) throws SQLException {

        String sql = """
                UPDATE recuperacion_password
                SET utilizado = TRUE
                WHERE id_recuperacion = ?
                  AND utilizado = FALSE
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idRecuperacion
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR TOKENS VENCIDOS
    // =========================================================

    public int eliminarTokensVencidos()
            throws SQLException {

        String sql = """
                DELETE FROM recuperacion_password
                WHERE fecha_expiracion < CURRENT_TIMESTAMP
                   OR utilizado = TRUE
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            return ps.executeUpdate();
        }
    }

    // =========================================================
    // CLASE INTERNA PARA MAPEAR TOKEN
    // =========================================================

    public static class TokenRecuperacion {

        private UUID idRecuperacion;
        private UUID idUsuario;
        private String token;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaExpiracion;
        private boolean utilizado;

        public UUID getIdRecuperacion() {
            return idRecuperacion;
        }

        public void setIdRecuperacion(
                UUID idRecuperacion
        ) {
            this.idRecuperacion =
                    idRecuperacion;
        }

        public UUID getIdUsuario() {
            return idUsuario;
        }

        public void setIdUsuario(
                UUID idUsuario
        ) {
            this.idUsuario =
                    idUsuario;
        }

        public String getToken() {
            return token;
        }

        public void setToken(
                String token
        ) {
            this.token =
                    token;
        }

        public LocalDateTime getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(
                LocalDateTime fechaCreacion
        ) {
            this.fechaCreacion =
                    fechaCreacion;
        }

        public LocalDateTime getFechaExpiracion() {
            return fechaExpiracion;
        }

        public void setFechaExpiracion(
                LocalDateTime fechaExpiracion
        ) {
            this.fechaExpiracion =
                    fechaExpiracion;
        }

        public boolean isUtilizado() {
            return utilizado;
        }

        public void setUtilizado(
                boolean utilizado
        ) {
            this.utilizado =
                    utilizado;
        }
    }
}