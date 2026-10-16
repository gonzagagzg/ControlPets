package com.itq.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConexionBD {

    // =========================================================
    // DATOS DE CONEXIÓN (inicializados en static block)
    // =========================================================

    private static String HOST;
    private static String PUERTO;
    private static String NOMBRE_BD;
    private static String URL;
    private static String USUARIO;
    private static String CONTRASENA;

    // =========================================================
    // POOL ÚNICO DE CONEXIONES
    // =========================================================

    private static final HikariDataSource DATA_SOURCE;

    static {
        ApplicationInitializer.initialize();

        HOST = obtenerVariableObligatoria("VET_DB_HOST");
        PUERTO = obtenerVariableObligatoria("VET_DB_PORT");
        NOMBRE_BD = obtenerVariableObligatoria("VET_DB_NAME");
        URL = "jdbc:postgresql://" + HOST + ":" + PUERTO + "/" + NOMBRE_BD;
        USUARIO = obtenerVariableObligatoria("VET_DB_USER");
        CONTRASENA = obtenerVariableObligatoria("VET_DB_PASSWORD");

        try {

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(URL);
            config.setUsername(USUARIO);
            config.setPassword(CONTRASENA);
            config.setDriverClassName("org.postgresql.Driver");

            config.setPoolName("VETERINARIAITQ-Pool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(1_800_000);
            config.setConnectionTestQuery("SELECT 1");
            config.setAutoCommit(true);

            DATA_SOURCE = new HikariDataSource(config);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "No se pudo inicializar el pool de conexiones: " + e.getMessage()
            );
        }
    }

    private ConexionBD() {
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

    // =========================================================
    // OBTENER CONEXIÓN
    // =========================================================

    public static Connection obtenerConexion()
            throws SQLException {

        return DATA_SOURCE.getConnection();
    }

    // =========================================================
    // CERRAR POOL
    // =========================================================

    public static void cerrarPool() {

        if (DATA_SOURCE != null &&
                !DATA_SOURCE.isClosed()) {

            DATA_SOURCE.close();
        }
    }
}
