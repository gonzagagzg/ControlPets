package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.service.AuthService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Endpoint público usado desde el login: un usuario bloqueado (sin token)
 * notifica al administrador que desea ser reactivado.
 *
 * POST /api/auth/notificar-bloqueo
 * Body: {"usuario": "..."}
 */
@WebServlet("/api/auth/notificar-bloqueo")
public class NotificarBloqueoServlet extends HttpServlet {

    private final AuthService service = new AuthService();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Map<String, String> body =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Map.class
                            );

            String usuario =
                    body != null ? body.get("usuario") : null;

            if (usuario == null || usuario.trim().isEmpty()) {

                HttpUtil.error(
                        resp,
                        400,
                        "El usuario es obligatorio"
                );

                return;
            }

            service.notificarBloqueo(usuario);

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Notificación enviada al administrador",
                            null
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    401,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (Exception e) {

            HttpUtil.errorInterno(resp, e);
        }
    }
}