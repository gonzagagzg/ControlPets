package com.itq.service;

import com.itq.dao.RecuperacionPasswordDAO;
import com.itq.dao.UsuarioDAO;
import com.itq.model.Usuario;
import com.itq.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class RecuperacionPasswordService {

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    private final RecuperacionPasswordDAO recuperacionDAO =
            new RecuperacionPasswordDAO();

    private final CorreoService correoService =
            new CorreoService();

    /*
     * El token tendrá una vigencia de 15 minutos.
     */
    private static final int MINUTOS_EXPIRACION = 15;

    /*
     * Generador criptográficamente seguro.
     */
    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    // =========================================================
    // SOLICITAR RECUPERACIÓN
    // =========================================================

    public String solicitarRecuperacion(
            String correo
    ) throws SQLException {

        if (correo == null ||
                correo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El correo electrónico es obligatorio"
            );
        }

        String correoNormalizado =
                correo.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!PATRON_CORREO
                .matcher(correoNormalizado)
                .matches()) {

            throw new IllegalArgumentException(
                    "El correo electrónico no es válido"
            );
        }

        Optional<Usuario> usuarioOpt =
                usuarioDAO.buscarPorCorreo(
                        correoNormalizado
                );

        /*
         * No revelamos si una dirección de correo
         * existe o no en el sistema.
         */
        if (usuarioOpt.isEmpty()) {

            return null;
        }

        Usuario usuario =
                usuarioOpt.get();

        /*
         * Una cuenta inactiva no puede recuperar
         * la contraseña.
         */
        if (usuario.isActivo() == null ||
                !usuario.isActivo()) {

            return null;
        }

        /*
         * Invalidamos cualquier token anterior
         * que todavía estuviera pendiente.
         */
        recuperacionDAO.invalidarTokensAnteriores(
                usuario.getIdUsuario()
        );

        /*
         * Generamos un token criptográficamente
         * seguro.
         */
        String token =
                generarTokenSeguro();

        LocalDateTime expiracion =
                LocalDateTime.now()
                        .plusMinutes(
                                MINUTOS_EXPIRACION
                        );

        /*
         * Guardamos el token antes de enviar
         * el correo.
         */
        recuperacionDAO.guardarToken(
                usuario.getIdUsuario(),
                token,
                expiracion
        );

        /*
         * El token ya NO se devuelve al frontend.
         *
         * Se envía exclusivamente mediante correo.
         */
        try {

            correoService.enviarCorreoRecuperacion(
                    correoNormalizado,
                    usuario.getNombres(),
                    token
            );

        } catch (RuntimeException e) {

            /*
             * Si el correo falla, invalidamos el token
             * que acabamos de generar.
             *
             * Así no queda un token activo al que
             * el usuario nunca tuvo acceso.
             */
            recuperacionDAO.invalidarTokensAnteriores(
                    usuario.getIdUsuario()
            );

            throw new IllegalStateException(
                    "No fue posible enviar el correo de recuperación",
                    e
            );
        }

        /*
         * Antes devolvíamos el token aquí para Postman.
         *
         * Ahora devolvemos null para que el Servlet
         * mantenga únicamente el mensaje genérico.
         */
        return null;
    }

    // =========================================================
    // RESTABLECER CONTRASEÑA
    // =========================================================

    public void restablecerPassword(
            String token,
            String nuevaClave
    ) throws SQLException {

        if (token == null ||
                token.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El token de recuperación es obligatorio"
            );
        }

        if (nuevaClave == null ||
                nuevaClave.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria"
            );
        }

        /*
         * Política mínima actual.
         */
        if (nuevaClave.trim().length() < 6) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 6 caracteres"
            );
        }

        Optional<RecuperacionPasswordDAO.TokenRecuperacion> tokenOpt =
                recuperacionDAO.buscarTokenValido(
                        token.trim()
                );

        if (tokenOpt.isEmpty()) {

            throw new IllegalArgumentException(
                    "El token es inválido o ha expirado"
            );
        }

        RecuperacionPasswordDAO.TokenRecuperacion recuperacion =
                tokenOpt.get();

        /*
         * Generamos un nuevo hash BCrypt.
         *
         * La contraseña nunca se almacena
         * en texto plano.
         */
        String nuevoHash =
                PasswordUtil.hash(
                        nuevaClave.trim()
                );

        boolean actualizado =
                usuarioDAO.actualizarClave(
                        recuperacion.getIdUsuario(),
                        nuevoHash
                );

        if (!actualizado) {

            throw new IllegalStateException(
                    "No fue posible actualizar la contraseña"
            );
        }

        /*
         * Después del cambio invalidamos cualquier
         * token pendiente del usuario.
         *
         * De esta manera el enlace es de un solo uso.
         */
        recuperacionDAO.invalidarTokensAnteriores(
                recuperacion.getIdUsuario()
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

        return recuperacionDAO
                .buscarTokenValido(
                        token.trim()
                )
                .isPresent();
    }

    // =========================================================
    // LIMPIEZA DE TOKENS
    // =========================================================

    public int limpiarTokensVencidos()
            throws SQLException {

        return recuperacionDAO
                .eliminarTokensVencidos();
    }

    // =========================================================
    // GENERACIÓN SEGURA DEL TOKEN
    // =========================================================

    private String generarTokenSeguro() {

        /*
         * 32 bytes = 256 bits aleatorios.
         */
        byte[] bytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                bytes
        );

        /*
         * URL-safe porque el token viajará
         * dentro del enlace enviado por correo.
         */
        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }
}