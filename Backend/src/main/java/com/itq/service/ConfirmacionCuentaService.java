package com.itq.service;

import com.itq.dao.ConfirmacionCorreoDAO;
import com.itq.dao.ConfirmacionCorreoDAO.TokenConfirmacion;
import com.itq.dao.UsuarioDAO;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Confirmación del correo electrónico de una cuenta.
 *
 * Se apoya en la misma idea que {@link RecuperacionPasswordService}: se
 * genera un token seguro, se guarda con expiración y se envía por correo.
 * A diferencia de la recuperación, aquí el token confirma la cuenta
 * ({@code estado_cuenta} pasa de {@code 'pendiente'} a {@code 'activo'}).
 */
public class ConfirmacionCuentaService {

    /**
     * El enlace de confirmación es válido durante 24 horas.
     */
    private static final int HORAS_EXPIRACION = 24;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final ConfirmacionCorreoDAO dao =
            new ConfirmacionCorreoDAO();

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    private final CorreoService correoService =
            new CorreoService();

    // =========================================================
    // REGISTRAR TOKEN + ENVIAR CORREO (dentro de una transacción)
    // =========================================================

    /**
     * Guarda el token en la MISMA conexión de la transacción que crea la
     * empresa/usuario y envía el correo de confirmación.
     *
     * Si el envío falla se lanza {@link IllegalStateException}, de modo que
     * la transacción del llamador se revierta: no queda una veterinaria
     * creada sin correo de confirmación entregado.
     */
    public void registrarYEnviar(
            Connection cn,
            UUID idUsuario,
            String correo,
            String nombre
    ) throws SQLException {

        if (cn == null) {

            throw new IllegalArgumentException(
                    "La conexión es obligatoria"
            );
        }

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        String token =
                generarTokenSeguro();

        LocalDateTime expiracion =
                LocalDateTime.now()
                        .plusHours(
                                HORAS_EXPIRACION
                        );

        dao.guardarToken(
                cn,
                idUsuario,
                token,
                expiracion
        );

        try {

            correoService.enviarCorreoConfirmacion(
                    correo,
                    nombre,
                    token
            );

        } catch (RuntimeException e) {

            throw new IllegalStateException(
                    "No se pudo enviar el correo de confirmación",
                    e
            );
        }
    }

    // =========================================================
    // CONFIRMAR CUENTA
    // =========================================================

    /**
     * Consume el token del enlace enviado por correo y activa la cuenta.
     */
    public void confirmar(
            String token
    ) throws SQLException {

        if (token == null ||
                token.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El enlace de confirmación no es válido"
            );
        }

        Optional<TokenConfirmacion> encontrado =
                dao.buscarTokenValido(
                        token.trim()
                );

        if (encontrado.isEmpty()) {

            throw new IllegalArgumentException(
                    "El enlace de confirmación no es válido o ya expiró"
            );
        }

        TokenConfirmacion confirmacion =
                encontrado.get();

        /*
         * El token es de un solo uso: se marca utilizado aunque la cuenta
         * ya estuviera activa.
         */
        dao.marcarComoUtilizado(
                confirmacion.getIdConfirmacion()
        );

        usuarioDAO.marcarCuentaConfirmada(
                confirmacion.getIdUsuario()
        );
    }

    // =========================================================
    // VALIDAR TOKEN
    // =========================================================

    public boolean tokenValido(
            String token
    ) throws SQLException {

        if (token == null ||
                token.trim().isEmpty()) {

            return false;
        }

        return dao
                .buscarTokenValido(
                        token.trim()
                )
                .isPresent();
    }

    // =========================================================
    // GENERACIÓN SEGURA DEL TOKEN
    // =========================================================

    private String generarTokenSeguro() {

        /*
         * 32 bytes = 256 bits aleatorios, URL-safe porque el token viaja
         * dentro del enlace enviado por correo.
         */
        byte[] bytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                bytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }
}
