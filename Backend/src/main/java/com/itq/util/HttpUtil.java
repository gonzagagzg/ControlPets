package com.itq.util;

import com.itq.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para construir respuestas HTTP en formato JSON.
 *
 * <p>Regla del proyecto: los mensajes que se envían al usuario final deben ser
 * simples y entendibles. Cuando ocurre un error técnico (base de datos, JSON
 * malformado, fallo inesperado), el detalle completo se registra como log en el
 * servidor y al usuario se le devuelve un mensaje genérico.
 */
public final class HttpUtil {

    private HttpUtil() {}

    /** Logger único del backend; Tomcat lo escribe en sus archivos de log. */
    private static final Logger LOGGER =
            Logger.getLogger("com.itq");

    public static final String MENSAJE_ERROR_INTERNO =
            "Ocurrió un error inesperado. Intenta nuevamente o contacta al administrador.";

    public static final String MENSAJE_NO_DISPONIBLE =
            "El servicio no está disponible en este momento. Intenta nuevamente en unos minutos.";

    public static final String MENSAJE_DATOS_INVALIDOS =
            "La información enviada no es válida. Revisa los datos e inténtalo de nuevo.";

    public static void json(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.gson().toJson(data));
    }

    public static void error(HttpServletResponse response, int status, String mensaje) throws IOException {
        json(response, status, ApiResponse.error(mensaje));
    }

    // =========================================================
    // ERRORES CON MENSAJE APTO PARA EL USUARIO
    // =========================================================

    /**
     * Error de validación de negocio (400). El mensaje de la excepción se
     * muestra al usuario solo si es apto para él; si parece técnico, se
     * registra en el log y se reemplaza por un mensaje genérico.
     */
    public static void errorValidacion(HttpServletResponse response, IllegalArgumentException e) throws IOException {
        String mensaje = e.getMessage();

        if (mensaje == null || mensaje.isBlank() || pareceTecnico(mensaje)) {
            registrar(Level.WARNING, "Solicitud con datos inválidos", e);
            error(response, 400, MENSAJE_DATOS_INVALIDOS);
            return;
        }

        error(response, 400, mensaje);
    }

    /**
     * Cuerpo de la petición que no se pudo interpretar (400): JSON malformado o
     * datos con formato incorrecto. El detalle técnico queda en el log.
     */
    public static void errorSolicitud(HttpServletResponse response, Exception e) throws IOException {
        registrar(Level.WARNING, "Solicitud no procesable", e);
        error(response, 400, MENSAJE_DATOS_INVALIDOS);
    }

    /**
     * Error de base de datos. Se registra el detalle técnico y se traduce el
     * SQLState a un mensaje entendible para el usuario.
     */
    public static void errorBaseDatos(HttpServletResponse response, SQLException e) throws IOException {
        registrar(Level.SEVERE, "Error de base de datos", e);
        error(response, SqlErrorUtil.estadoHttp(e), mensajeBaseDatos(e));
    }

    /** Error inesperado del servidor (500). */
    public static void errorInterno(HttpServletResponse response, Throwable e) throws IOException {
        registrar(Level.SEVERE, "Error interno del servidor", e);
        error(response, 500, MENSAJE_ERROR_INTERNO);
    }

    /** Servicio temporalmente no disponible (503). */
    public static void errorNoDisponible(HttpServletResponse response, Throwable e) throws IOException {
        registrar(Level.SEVERE, "Servicio no disponible", e);
        error(response, 503, MENSAJE_NO_DISPONIBLE);
    }

    // =========================================================
    // INTERNOS
    // =========================================================

    private static void registrar(Level nivel, String contexto, Throwable e) {
        LOGGER.log(nivel, contexto, e);
    }

    /** Traduce el SQLState de PostgreSQL a un mensaje apto para el usuario. */
    private static String mensajeBaseDatos(SQLException e) {
        String estado = e.getSQLState();

        if (estado == null) {
            return MENSAJE_ERROR_INTERNO;
        }

        return switch (estado) {
            case "23505" -> "Ya existe un registro con esos datos.";
            case "23503" -> "No se puede completar la operación porque el registro está relacionado con otros datos.";
            case "23502" -> "Faltan datos obligatorios para completar la operación.";
            case "23514" -> "Alguno de los valores ingresados no cumple con las reglas permitidas.";
            case "22P02" -> MENSAJE_DATOS_INVALIDOS;
            default -> estado.startsWith("22") ? MENSAJE_DATOS_INVALIDOS : MENSAJE_ERROR_INTERNO;
        };
    }

    /**
     * Heurística para detectar mensajes técnicos que no deben mostrarse al
     * usuario final (excepciones de Java, errores de parseo, SQL, etc.).
     */
    private static boolean pareceTecnico(String mensaje) {
        String m = mensaje.toLowerCase();

        return m.contains("exception")
                || m.contains("invalid uuid")
                || m.contains("for input string")
                || m.contains("cannot deserialize")
                || m.contains("json")
                || m.contains("sqlstate")
                || m.contains("duplicate key")
                || m.contains("violates")
                || m.contains("syntax error")
                || m.contains("null value in column")
                || m.startsWith("java.")
                || m.startsWith("org.")
                || m.startsWith("com.")
                || m.contains("at com.itq");
    }
}
