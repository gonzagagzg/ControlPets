package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaDAO {

    // =========================================================
    // LISTAR TODAS LAS EMPRESAS
    // =========================================================

    public List<Empresa> listar()
            throws SQLException {

        String sql = """
                SELECT id_empresa,
                       ruc,
                       razon_social,
                       direccion,
                       correo,
                       telefono,
                       activo
                FROM empresa
                WHERE eliminado = FALSE
                ORDER BY razon_social
                """;

        List<Empresa> lista =
                new ArrayList<>();

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Empresa> buscarPorId(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_empresa,
                       ruc,
                       razon_social,
                       direccion,
                       correo,
                       telefono,
                       activo
                FROM empresa
                WHERE id_empresa = ?
                  AND eliminado = FALSE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Empresa insertar(
            Empresa obj
    ) throws SQLException {

        String sql = """
                INSERT INTO empresa
                (
                    ruc,
                    razon_social,
                    direccion,
                    correo,
                    telefono,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_empresa
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    obj.getRuc()
            );

            ps.setString(
                    2,
                    obj.getRazonSocial()
            );

            ps.setString(
                    3,
                    obj.getDireccion()
            );

            ps.setString(
                    4,
                    obj.getCorreo()
            );

            ps.setString(
                    5,
                    obj.getTelefono()
            );

            ps.setBoolean(
                    6,
                    obj.isActivo()
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdEmpresa(
                        rs.getObject(
                                "id_empresa",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR DATOS
    // =========================================================

    public boolean actualizar(
            Empresa obj
    ) throws SQLException {

        String sql = """
                UPDATE empresa
                SET ruc = ?,
                    razon_social = ?,
                    direccion = ?,
                    correo = ?,
                    telefono = ?,
                    activo = ?
                WHERE id_empresa = ?
                  AND eliminado = FALSE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    obj.getRuc()
            );

            ps.setString(
                    2,
                    obj.getRazonSocial()
            );

            ps.setString(
                    3,
                    obj.getDireccion()
            );

            ps.setString(
                    4,
                    obj.getCorreo()
            );

            ps.setString(
                    5,
                    obj.getTelefono()
            );

            ps.setBoolean(
                    6,
                    obj.isActivo()
            );

            ps.setObject(
                    7,
                    obj.getIdEmpresa()
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    public boolean cambiarEstado(
            UUID idEmpresa,
            boolean activo
    ) throws SQLException {

        String sql = """
                UPDATE empresa
                SET activo = ?
                WHERE id_empresa = ?
                  AND eliminado = FALSE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setBoolean(
                    1,
                    activo
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    public boolean softDelete(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE empresa
                SET eliminado = TRUE,
                    activo = FALSE,
                    fecha_eliminacion = CURRENT_TIMESTAMP
                WHERE id_empresa = ?
                  AND eliminado = FALSE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // EXISTENCIA (RUC / CORREO / TELÉFONO)
    // =========================================================

    public boolean existeRuc(
            String ruc
    ) throws SQLException {

        return existePorColumna(
                "ruc",
                ruc
        );
    }

    public boolean existeCorreo(
            String correo
    ) throws SQLException {

        return existePorColumna(
                "correo",
                correo
        );
    }

    public boolean existeTelefono(
            String telefono
    ) throws SQLException {

        return existePorColumna(
                "telefono",
                telefono
        );
    }

    private boolean existePorColumna(
            String columna,
            String valor
    ) throws SQLException {

        String sql =
                "SELECT 1 FROM empresa WHERE " + columna
                        + " = ? AND eliminado = FALSE LIMIT 1";

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    valor
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Empresa mapear(
            ResultSet rs
    ) throws SQLException {

        Empresa obj =
                new Empresa();

        obj.setIdEmpresa(
                rs.getObject(
                        "id_empresa",
                        UUID.class
                )
        );

        obj.setRuc(
                rs.getString("ruc")
        );

        obj.setRazonSocial(
                rs.getString(
                        "razon_social"
                )
        );

        obj.setDireccion(
                rs.getString(
                        "direccion"
                )
        );

        obj.setCorreo(
                rs.getString(
                        "correo"
                )
        );

        obj.setTelefono(
                rs.getString(
                        "telefono"
                )
        );

        obj.setActivo(
                (Boolean)
                        rs.getObject("activo")
        );

        return obj;
    }
}