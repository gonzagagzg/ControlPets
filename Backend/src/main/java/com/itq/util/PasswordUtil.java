package com.itq.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String clave) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        return BCrypt.hashpw(clave, BCrypt.gensalt(12));
    }

    public static boolean verificar(String clave, String hash) {
        if (clave == null || hash == null || hash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(clave, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean esHashBCrypt(String valor) {
        return valor != null &&
                (valor.startsWith("$2a$")
                        || valor.startsWith("$2b$")
                        || valor.startsWith("$2y$"));
    }
}