package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.security.Autorizacion;
import com.itq.service.EstadisticasService;
import com.itq.util.HttpUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Panel estadístico global de la plataforma.
 *
 * <p>GET /api/estadisticas — reservado al SuperUsuario (igual que
 * EmpresaServlet): agrega indicadores de todas las veterinarias para las
 * gráficas de /app. No expone datos de negocio de una empresa concreta.
 */
@WebServlet("/api/estadisticas/*")
public class EstadisticasServlet extends HttpServlet {

    private final EstadisticasService service =
            new EstadisticasService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "EMPRESAS",
                    "VER"
            );

            String[] partes =
                    partesRuta(req);

            // =================================================
            // GET /api/estadisticas
            // =================================================

            if (partes.length == 0) {

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Estadísticas de la plataforma",
                                service.obtenerPanel()
                        )
                );

                return;
            }

            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorNoDisponible(resp, e);

        } catch (Exception e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // SOLO SUPERUSUARIO
    // =========================================================

    private void exigirSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute(
                                "rol"
                        );

        if (rol == null ||
                !rol.equalsIgnoreCase(
                        "SuperUsuario"
                )) {

            throw new SecurityException(
                    "Esta operación requiere SuperUsuario"
            );
        }
    }

    // =========================================================
    // PARSEO DE RUTA
    // =========================================================

    private String[] partesRuta(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return new String[0];
        }

        String limpio =
                path.trim();

        if (limpio.startsWith("/")) {

            limpio =
                    limpio.substring(1);
        }

        if (limpio.endsWith("/")) {

            limpio =
                    limpio.substring(
                            0,
                            limpio.length() - 1
                    );
        }

        if (limpio.isBlank()) {

            return new String[0];
        }

        return limpio.split("/");
    }
}
