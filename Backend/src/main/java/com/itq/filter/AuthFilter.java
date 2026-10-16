package com.itq.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.itq.config.ConexionBD;
import com.itq.util.HttpUtil;
import com.itq.util.JwtUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    /** Logger único del backend; Tomcat lo escribe en sus archivos de log. */
    private static final Logger LOGGER =
            Logger.getLogger("com.itq");

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
        // No requiere inicialización especial.
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse resp =
                (HttpServletResponse) response;

        String path =
                req.getRequestURI()
                        .substring(
                                req.getContextPath().length()
                        );

        // =====================================================
        // ENDPOINTS PÚBLICOS DE AUTENTICACIÓN
        // =====================================================

        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/notificar-bloqueo") ||
                path.equals("/api/auth/recuperar-password") ||
                path.equals("/api/auth/restablecer-password") ||
                path.equals("/api/auth/confirmar-cuenta")) {

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        // =====================================================
        // CORS PREFLIGHT
        // =====================================================

        if ("OPTIONS".equalsIgnoreCase(
                req.getMethod()
        )) {

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        // =====================================================
        // OBTENER BEARER TOKEN
        // =====================================================

        String authorization =
                req.getHeader(
                        "Authorization"
                );

        if (authorization == null ||
                !authorization.startsWith(
                        "Bearer "
                )) {

            HttpUtil.error(
                    resp,
                    401,
                    "Token de autenticación requerido"
            );

            return;
        }

        String token =
                authorization
                        .substring(7)
                        .trim();

        if (token.isEmpty()) {

            HttpUtil.error(
                    resp,
                    401,
                    "Token de autenticación requerido"
            );

            return;
        }

        // =====================================================
        // VALIDAR JWT
        // =====================================================

        DecodedJWT jwt;

        try {

            jwt =
                    JwtUtil.verificarToken(
                            token
                    );

        } catch (Exception e) {

            HttpUtil.error(
                    resp,
                    401,
                    "Token inválido o expirado"
            );

            return;
        }

        try {

            // =================================================
            // EXTRAER IDENTIDAD DEL JWT
            // =================================================

            UUID idUsuario =
                    UUID.fromString(
                            jwt.getSubject()
                    );

            String idEmpresaClaim =
                    jwt.getClaim(
                            "idEmpresa"
                    ).asString();

            UUID idEmpresa =
                    idEmpresaClaim == null
                            ? null
                            : UUID.fromString(
                                    idEmpresaClaim
                            );

            Integer idRol =
                    jwt.getClaim(
                            "idRol"
                    ).asInt();

            String rol =
                    jwt.getClaim(
                            "rol"
                    ).asString();

            // =================================================
            // VALIDACIONES BÁSICAS DEL TOKEN
            // =================================================

            if (idUsuario == null) {

                HttpUtil.error(
                        resp,
                        401,
                        "Token sin usuario válido"
                );

                return;
            }

            if (idRol == null ||
                    rol == null ||
                    rol.isBlank()) {

                HttpUtil.error(
                        resp,
                        401,
                        "Token sin información de rol"
                );

                return;
            }

            // =================================================
            // SUPERUSUARIO
            //
            // El dueño global de la aplicación no depende
            // del estado de una veterinaria.
            // =================================================

            boolean superUsuario =
                    rol.equalsIgnoreCase(
                            "SuperUsuario"
                    );

            // =================================================
            // USUARIOS DE VETERINARIAS
            //
            // Antes de permitir CUALQUIER endpoint,
            // comprobamos que su empresa siga activa.
            //
            // Esto también funciona con JWT ya emitidos:
            //
            // activo = true  -> continúa
            // activo = false -> 403
            // =================================================

            if (!superUsuario) {

                if (idEmpresa == null) {

                    HttpUtil.error(
                            resp,
                            403,
                            "El usuario no tiene una empresa asignada"
                    );

                    return;
                }

                Boolean activa =
                        obtenerEstadoEmpresa(
                                idEmpresa
                        );

                /*
                 * null significa que la empresa
                 * indicada en el JWT ya no existe.
                 */
                if (activa == null) {

                    HttpUtil.error(
                            resp,
                            403,
                            "La empresa asociada al usuario no existe"
                    );

                    return;
                }

                if (!activa) {

                    HttpUtil.error(
                            resp,
                            403,
                            "La veterinaria se encuentra desactivada"
                    );

                    return;
                }
            }

            // =================================================
            // PROPAGAR IDENTIDAD A LOS SERVLETS
            // =================================================

            req.setAttribute(
                    "idUsuario",
                    idUsuario
            );

            req.setAttribute(
                    "idEmpresa",
                    idEmpresa
            );

            req.setAttribute(
                    "idRol",
                    idRol
            );

            req.setAttribute(
                    "rol",
                    rol
            );

            // =================================================
            // PETICIÓN AUTORIZADA PARA CONTINUAR
            // =================================================

            chain.doFilter(
                    request,
                    response
            );

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar el estado de la empresa", e);
            HttpUtil.error(
                    resp,
                    500,
                    HttpUtil.MENSAJE_ERROR_INTERNO
            );

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Token con información inválida", e);
            HttpUtil.error(
                    resp,
                    401,
                    "Token con información inválida"
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error interno de autenticación", e);
            HttpUtil.error(
                    resp,
                    500,
                    HttpUtil.MENSAJE_ERROR_INTERNO
            );
        }
    }

    // =========================================================
    // CONSULTAR ESTADO DE LA EMPRESA
    // =========================================================

    private Boolean obtenerEstadoEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT activo
                FROM empresa
                WHERE id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {

                    return null;
                }

                return rs.getBoolean(
                        "activo"
                );
            }
        }
    }

    @Override
    public void destroy() {
        // No requiere liberación especial.
    }
}