package com.itq.config;

/**
 * Configuración SMTP del proyecto.
 *
 * Todos los valores se leen del archivo {@code Backend/.env} (cargado por
 * {@link ApplicationInitializer}) o de variables de entorno del sistema.
 * No hay valores por defecto: si falta una variable, la aplicación falla al
 * arrancar con un mensaje que indica cuál configurar (mismo criterio que
 * {@link ConexionBD}).
 */
public final class ConfiguracionCorreo {

    private ConfiguracionCorreo() {
    }

    // =========================================================
    // CONFIGURACIÓN SMTP DEL PROYECTO (inicializada en static block)
    // =========================================================

    private static String SMTP_HOST;
    private static int SMTP_PUERTO;
    private static String SMTP_USUARIO;
    private static String SMTP_CLAVE;
    private static String CORREO_REMITENTE;
    private static String NOMBRE_REMITENTE;
    private static boolean SMTP_STARTTLS;
    private static boolean SMTP_AUTH;
    private static String FRONTEND_URL;

    static {
        ApplicationInitializer.initialize();

        SMTP_HOST = obtenerVariableObligatoria("SMTP_HOST");
        SMTP_PUERTO = obtenerEnteroObligatorio("SMTP_PORT");
        SMTP_USUARIO = obtenerVariableObligatoria("SMTP_USER");
        SMTP_CLAVE = obtenerVariableObligatoria("SMTP_PASSWORD");
        CORREO_REMITENTE = obtenerVariableObligatoria("SMTP_FROM_EMAIL");
        NOMBRE_REMITENTE = obtenerVariableObligatoria("SMTP_FROM_NAME");
        SMTP_STARTTLS = obtenerBooleanoObligatorio("SMTP_STARTTLS");
        SMTP_AUTH = obtenerBooleanoObligatorio("SMTP_AUTH");
        FRONTEND_URL = obtenerVariableObligatoria("FRONTEND_URL");
    }

    // =========================================================
    // VARIABLES DE ENTORNO (obligatorias desde .env)
    // =========================================================

    private static String obtenerVariableObligatoria(
            String nombre
    ) {

        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(nombre);
        }

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Variable de entorno obligatoria no configurada: " + nombre +
                    "\n   → Debe definirse en Backend/.env (o como variable de entorno del sistema)" +
                    "\n   → Ejemplo en .env: " + nombre + "=valor"
            );
        }

        return valor.trim();
    }

    private static int obtenerEnteroObligatorio(
            String nombre
    ) {

        String valor = obtenerVariableObligatoria(nombre);

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La variable de entorno " + nombre +
                    " debe ser un número entero. Valor recibido: " + valor
            );
        }
    }

    private static boolean obtenerBooleanoObligatorio(
            String nombre
    ) {

        String valor = obtenerVariableObligatoria(nombre);

        if (valor.equalsIgnoreCase("true")) {
            return true;
        }

        if (valor.equalsIgnoreCase("false")) {
            return false;
        }

        throw new IllegalStateException(
                "La variable de entorno " + nombre +
                " debe ser 'true' o 'false'. Valor recibido: " + valor
        );
    }

    // =========================================================
    // SMTP
    // =========================================================

    public static String getSmtpHost() {
        return SMTP_HOST;
    }

    public static int getSmtpPuerto() {
        return SMTP_PUERTO;
    }

    public static String getSmtpUsuario() {
        return SMTP_USUARIO;
    }

    public static String getSmtpClave() {
        return SMTP_CLAVE;
    }

    // =========================================================
    // REMITENTE
    // =========================================================

    public static String getCorreoRemitente() {
        return CORREO_REMITENTE;
    }

    public static String getNombreRemitente() {
        return NOMBRE_REMITENTE;
    }

    // =========================================================
    // SEGURIDAD SMTP
    // =========================================================

    public static boolean usarStartTls() {
        return SMTP_STARTTLS;
    }

    public static boolean usarAutenticacion() {
        return SMTP_AUTH;
    }

    // =========================================================
    // FRONTEND
    // =========================================================

    public static String getFrontendUrl() {
        return FRONTEND_URL;
    }

    // =========================================================
    // ENLACE DE RECUPERACIÓN
    // =========================================================

    public static String crearUrlRecuperacion(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            throw new IllegalArgumentException(
                    "El token de recuperación es obligatorio"
            );
        }

        return getFrontendUrl()
                + "/restablecer-password?token="
                + token;
    }

    // =========================================================
    // ENLACE DE CONFIRMACIÓN DE CUENTA
    // =========================================================

    public static String crearUrlConfirmacion(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            throw new IllegalArgumentException(
                    "El token de confirmación es obligatorio"
            );
        }

        return getFrontendUrl()
                + "/confirmar-cuenta?token="
                + token;
    }
}
