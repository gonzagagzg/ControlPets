package com.itq.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    private static String SECRET;
    private static Algorithm ALGORITHM;
    private static JWTVerifier VERIFIER;

    static {
        com.itq.config.ApplicationInitializer.initialize();

        String valor = System.getenv("JWT_SECRET");
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty("JWT_SECRET");
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable de entorno JWT_SECRET"
            );
        }
        SECRET = valor.trim();
        ALGORITHM = Algorithm.HMAC256(SECRET);
        VERIFIER = JWT.require(ALGORITHM)
                .withIssuer("VETERINARIAITQ")
                .build();
    }

    private JwtUtil() {
    }

    public static String generarToken(
            UUID idUsuario,
            UUID idEmpresa,
            Integer idRol,
            String rol
    ) {

        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(8, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer("VETERINARIAITQ")
                .withSubject(idUsuario.toString())
                .withClaim(
                        "idEmpresa",
                        idEmpresa == null ? null : idEmpresa.toString()
                )
                .withClaim("idRol", idRol)
                .withClaim("rol", rol)
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(ALGORITHM);
    }

    public static DecodedJWT verificarToken(String token) {
        return VERIFIER.verify(token);
    }
}