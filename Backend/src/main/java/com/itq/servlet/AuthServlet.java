package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.dto.ConfirmarCuentaRequest;
import com.itq.dto.LoginRequest;
import com.itq.dto.LoginResponse;
import com.itq.dto.RecuperarPasswordRequest;
import com.itq.dto.RestablecerPasswordRequest;
import com.itq.service.AuthService;
import com.itq.service.ConfirmacionCuentaService;
import com.itq.service.RecuperacionPasswordService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService =
            new AuthService();

    private final RecuperacionPasswordService recuperacionService =
            new RecuperacionPasswordService();

    private final ConfirmacionCuentaService confirmacionService =
            new ConfirmacionCuentaService();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        String path =
                req.getPathInfo();

        if (path == null) {

            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
            );

            return;
        }

        try {

            switch (path) {

                case "/login" ->
                        login(
                                req,
                                resp
                        );

                case "/recuperar-password" ->
                        recuperarPassword(
                                req,
                                resp
                        );

                case "/restablecer-password" ->
                        restablecerPassword(
                                req,
                                resp
                        );

                case "/confirmar-cuenta" ->
                        confirmarCuenta(
                                req,
                                resp
                        );

                default ->
                        HttpUtil.error(
                                resp,
                                404,
                                "Ruta no encontrada"
                        );
            }

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

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {
            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void login(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException, SQLException {

        LoginRequest login =
                JsonUtil.gson()
                        .fromJson(
                                req.getReader(),
                                LoginRequest.class
                        );

        if (login == null) {

            HttpUtil.error(
                    resp,
                    400,
                    "Los datos de inicio de sesión son obligatorios"
            );

            return;
        }

        LoginResponse resultado =
                authService.login(
                        login.getUsuario(),
                        login.getClave()
                );

        HttpUtil.json(
                resp,
                200,
                ApiResponse.ok(
                        "Inicio de sesión correcto",
                        resultado
                )
        );
    }

    // =========================================================
    // RECUPERAR CONTRASEÑA
    // =========================================================

    private void recuperarPassword(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException, SQLException {

        RecuperarPasswordRequest body =
                JsonUtil.gson()
                        .fromJson(
                                req.getReader(),
                                RecuperarPasswordRequest.class
                        );

        if (body == null) {

            HttpUtil.error(
                    resp,
                    400,
                    "Los datos de recuperación son obligatorios"
            );

            return;
        }

        String token =
                recuperacionService.solicitarRecuperacion(
                        body.getCorreo()
                );

        /*
         * Mensaje genérico para no revelar
         * si el correo existe en el sistema.
         *
         * TEMPORAL:
         * el token se devuelve para pruebas
         * en Postman.
         */
        Map<String, Object> datos =
                new HashMap<>();

        if (token != null) {

            datos.put(
                    "token",
                    token
            );
        }

        HttpUtil.json(
                resp,
                200,
                ApiResponse.ok(
                        "Si el correo está registrado, se generó una solicitud de recuperación",
                        datos
                )
        );
    }

    // =========================================================
    // RESTABLECER CONTRASEÑA
    // =========================================================

    private void restablecerPassword(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException, SQLException {

        RestablecerPasswordRequest body =
                JsonUtil.gson()
                        .fromJson(
                                req.getReader(),
                                RestablecerPasswordRequest.class
                        );

        if (body == null) {

            HttpUtil.error(
                    resp,
                    400,
                    "Los datos para restablecer la contraseña son obligatorios"
            );

            return;
        }

        recuperacionService.restablecerPassword(
                body.getToken(),
                body.getNuevaClave()
        );

        HttpUtil.json(
                resp,
                200,
                ApiResponse.ok(
                        "Contraseña actualizada correctamente",
                        null
                )
        );
    }

    // =========================================================
    // CONFIRMAR CUENTA
    // =========================================================

    /**
     * Consume el token enviado por correo al crear una veterinaria y deja
     * la cuenta en estado_cuenta = 'activo'. Endpoint público: el enlace
     * del correo se abre sin sesión.
     */
    private void confirmarCuenta(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException, SQLException {

        ConfirmarCuentaRequest body =
                JsonUtil.gson()
                        .fromJson(
                                req.getReader(),
                                ConfirmarCuentaRequest.class
                        );

        if (body == null) {

            HttpUtil.error(
                    resp,
                    400,
                    "El token de confirmación es obligatorio"
            );

            return;
        }

        confirmacionService.confirmar(
                body.getToken()
        );

        HttpUtil.json(
                resp,
                200,
                ApiResponse.ok(
                        "Cuenta confirmada correctamente",
                        null
                )
        );
    }
}