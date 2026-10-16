package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClienteDAO {

    // =========================================================
    // SUPERUSUARIO - LISTADO COMPLETO
    // =========================================================

    public List<Cliente> listar()
            throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                ORDER BY id_cliente
                """;

        List<Cliente> lista =
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
    // SUPERUSUARIO - LISTADO PAGINADO
    // =========================================================

    public List<Cliente> listarPaginado(
            int limite,
            int offset
    ) throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                ORDER BY id_cliente
                LIMIT ?
                OFFSET ?
                """;

        List<Cliente> lista =
                new ArrayList<>();

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setInt(1, limite);
            ps.setInt(2, offset);

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    // =========================================================
    // TOTAL REGISTROS
    // =========================================================

    public long contar()
            throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM cliente
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            rs.next();

            return rs.getLong(
                    "total"
            );
        }
    }

    // =========================================================
    // SUPERUSUARIO - BUSCAR POR ID
    // =========================================================

    public Optional<Cliente> buscarPorId(
            UUID idCliente
    ) throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                WHERE id_cliente = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idCliente
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // EMPRESA - LISTADO COMPLETO
    // =========================================================

    public List<Cliente> listarPorEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                WHERE id_empresa = ?
                ORDER BY id_cliente
                """;

        List<Cliente> lista =
                new ArrayList<>();

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

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    // =========================================================
    // EMPRESA - LISTADO PAGINADO
    // =========================================================

    public List<Cliente> listarPorEmpresaPaginado(
            UUID idEmpresa,
            int limite,
            int offset
    ) throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                WHERE id_empresa = ?
                ORDER BY id_cliente
                LIMIT ?
                OFFSET ?
                """;

        List<Cliente> lista =
                new ArrayList<>();

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

            ps.setInt(
                    2,
                    limite
            );

            ps.setInt(
                    3,
                    offset
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    // =========================================================
    // EMPRESA - TOTAL REGISTROS
    // =========================================================

    public long contarPorEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM cliente
                WHERE id_empresa = ?
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

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                rs.next();

                return rs.getLong(
                        "total"
                );
            }
        }
    }

    // =========================================================
    // EMPRESA - BUSCAR POR ID
    // =========================================================

    public Optional<Cliente> buscarPorIdYEmpresa(
            UUID idCliente,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_cliente,
                       id_empresa,
                       tipo_documento,
                       identificacion,
                       nombres,
                       direccion,
                       correo,
                       telefono
                FROM cliente
                WHERE id_cliente = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idCliente
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Cliente insertar(
            Cliente obj
    ) throws SQLException {

        String sql = """
                INSERT INTO cliente
                (
                    id_empresa,
                    tipo_documento,
                    identificacion,
                    nombres,
                    direccion,
                    correo,
                    telefono
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id_cliente
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    obj.getIdEmpresa()
            );

            ps.setString(
                    2,
                    obj.getTipoDocumento()
            );

            ps.setString(
                    3,
                    obj.getIdentificacion()
            );

            ps.setString(
                    4,
                    obj.getNombres()
            );

            ps.setString(
                    5,
                    obj.getDireccion()
            );

            ps.setString(
                    6,
                    obj.getCorreo()
            );

            ps.setString(
                    7,
                    obj.getTelefono()
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdCliente(
                        rs.getObject(
                                "id_cliente",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR - SUPERUSUARIO
    // =========================================================

    public boolean actualizar(
            Cliente obj
    ) throws SQLException {

        String sql = """
                UPDATE cliente
                SET id_empresa = ?,
                    tipo_documento = ?,
                    identificacion = ?,
                    nombres = ?,
                    direccion = ?,
                    correo = ?,
                    telefono = ?
                WHERE id_cliente = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    obj.getIdEmpresa()
            );

            ps.setString(
                    2,
                    obj.getTipoDocumento()
            );

            ps.setString(
                    3,
                    obj.getIdentificacion()
            );

            ps.setString(
                    4,
                    obj.getNombres()
            );

            ps.setString(
                    5,
                    obj.getDireccion()
            );

            ps.setString(
                    6,
                    obj.getCorreo()
            );

            ps.setString(
                    7,
                    obj.getTelefono()
            );

            ps.setObject(
                    8,
                    obj.getIdCliente()
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ACTUALIZAR - EMPRESA
    // =========================================================

    public boolean actualizarPorEmpresa(
            Cliente obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE cliente
                SET tipo_documento = ?,
                    identificacion = ?,
                    nombres = ?,
                    direccion = ?,
                    correo = ?,
                    telefono = ?
                WHERE id_cliente = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    obj.getTipoDocumento()
            );

            ps.setString(
                    2,
                    obj.getIdentificacion()
            );

            ps.setString(
                    3,
                    obj.getNombres()
            );

            ps.setString(
                    4,
                    obj.getDireccion()
            );

            ps.setString(
                    5,
                    obj.getCorreo()
            );

            ps.setString(
                    6,
                    obj.getTelefono()
            );

            ps.setObject(
                    7,
                    obj.getIdCliente()
            );

            ps.setObject(
                    8,
                    idEmpresa
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR - SUPERUSUARIO
    // =========================================================

    public boolean eliminar(
            UUID idCliente
    ) throws SQLException {

        String sql = """
                DELETE FROM cliente
                WHERE id_cliente = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idCliente
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR - EMPRESA
    // =========================================================

    public boolean eliminarPorEmpresa(
            UUID idCliente,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM cliente
                WHERE id_cliente = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idCliente
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Cliente mapear(
            ResultSet rs
    ) throws SQLException {

        Cliente obj =
                new Cliente();

        obj.setIdCliente(
                rs.getObject(
                        "id_cliente",
                        UUID.class
                )
        );

        obj.setIdEmpresa(
                rs.getObject(
                        "id_empresa",
                        UUID.class
                )
        );

        obj.setTipoDocumento(
                rs.getString(
                        "tipo_documento"
                )
        );

        obj.setIdentificacion(
                rs.getString(
                        "identificacion"
                )
        );

        obj.setNombres(
                rs.getString(
                        "nombres"
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

        return obj;
    }
}