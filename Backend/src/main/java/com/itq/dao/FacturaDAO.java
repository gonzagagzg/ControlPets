package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Factura;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaDAO {

    public List<Factura> listar() throws SQLException {

        String sql = """
                SELECT id_factura, id_empresa, id_cliente,
                       id_usuario, total, estado, fecha
                FROM factura
                ORDER BY fecha DESC
                """;

        List<Factura> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    public List<Factura> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT id_factura, id_empresa, id_cliente,
                       id_usuario, total, estado, fecha
                FROM factura
                WHERE id_empresa = ?
                ORDER BY fecha DESC
                """;

        List<Factura> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    public Optional<Factura> buscarPorId(UUID idFactura)
            throws SQLException {

        String sql = """
                SELECT id_factura, id_empresa, id_cliente,
                       id_usuario, total, estado, fecha
                FROM factura
                WHERE id_factura = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idFactura);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Factura> buscarPorIdYEmpresa(
            UUID idFactura,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_factura, id_empresa, id_cliente,
                       id_usuario, total, estado, fecha
                FROM factura
                WHERE id_factura = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idFactura);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Factura insertar(Factura obj)
            throws SQLException {

        String sql = """
                INSERT INTO factura
                (
                    id_empresa,
                    id_cliente,
                    id_usuario,
                    total,
                    estado,
                    fecha
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_factura
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCliente());
            ps.setObject(3, obj.getIdUsuario());
            ps.setBigDecimal(4, obj.getTotal());
            ps.setString(5, obj.getEstado());
            ps.setObject(6, obj.getFecha());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdFactura(
                        rs.getObject(
                                "id_factura",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    public boolean actualizar(Factura obj)
            throws SQLException {

        String sql = """
                UPDATE factura
                SET id_empresa = ?,
                    id_cliente = ?,
                    id_usuario = ?,
                    total = ?,
                    estado = ?,
                    fecha = ?
                WHERE id_factura = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCliente());
            ps.setObject(3, obj.getIdUsuario());
            ps.setBigDecimal(4, obj.getTotal());
            ps.setString(5, obj.getEstado());
            ps.setObject(6, obj.getFecha());
            ps.setObject(7, obj.getIdFactura());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Factura obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE factura
                SET id_cliente = ?,
                    id_usuario = ?,
                    total = ?,
                    estado = ?,
                    fecha = ?
                WHERE id_factura = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdCliente());
            ps.setObject(2, obj.getIdUsuario());
            ps.setBigDecimal(3, obj.getTotal());
            ps.setString(4, obj.getEstado());
            ps.setObject(5, obj.getFecha());
            ps.setObject(6, obj.getIdFactura());
            ps.setObject(7, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idFactura)
            throws SQLException {

        String sql = """
                DELETE FROM factura
                WHERE id_factura = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idFactura);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idFactura,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM factura
                WHERE id_factura = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idFactura);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    private Factura mapear(ResultSet rs)
            throws SQLException {

        Factura obj = new Factura();

        obj.setIdFactura(
                rs.getObject("id_factura", UUID.class)
        );

        obj.setIdEmpresa(
                rs.getObject("id_empresa", UUID.class)
        );

        obj.setIdCliente(
                rs.getObject("id_cliente", UUID.class)
        );

        obj.setIdUsuario(
                rs.getObject("id_usuario", UUID.class)
        );

        obj.setTotal(
                rs.getBigDecimal("total")
        );

        obj.setEstado(
                rs.getString("estado")
        );

        obj.setFecha(
                rs.getObject(
                        "fecha",
                        OffsetDateTime.class
                )
        );

        return obj;
    }
}