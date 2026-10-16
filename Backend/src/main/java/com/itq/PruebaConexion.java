package com.itq;

import com.itq.config.ConexionBD;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {

        String sql = """
                SELECT COUNT(*) AS total_tablas
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                """;

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery(sql)
        ) {
            System.out.println("==============================");
            System.out.println("CONEXIÓN EXITOSA A POSTGRESQL");
            System.out.println("==============================");

        } catch (Exception e) {
            System.err.println("==============================");
            System.err.println("ERROR DE CONEXIÓN");
            System.err.println("==============================");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}