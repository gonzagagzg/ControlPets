package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Producto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductoDAO {

    // =========================================================
    // SUPERUSUARIO - LISTADO GLOBAL
    // =========================================================

    public List<Producto> listar() throws SQLException {

        String sql = """
                SELECT id_producto, id_empresa, id_categoria, id_iva,
                       nombre, precio_unitario, stock_actual,
                       stock_minimo, fecha_caducidad
                FROM producto
                ORDER BY nombre
                """;

        List<Producto> lista = new ArrayList<>();

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

    // =========================================================
    // USUARIO NORMAL - LISTADO POR EMPRESA
    // =========================================================

    public List<Producto> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT id_producto, id_empresa, id_categoria, id_iva,
                       nombre, precio_unitario, stock_actual,
                       stock_minimo, fecha_caducidad
                FROM producto
                WHERE id_empresa = ?
                ORDER BY nombre
                """;

        List<Producto> lista = new ArrayList<>();

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

    // =========================================================
    // BÚSQUEDA
    // =========================================================

    public Optional<Producto> buscarPorId(UUID idProducto)
            throws SQLException {

        String sql = """
                SELECT id_producto, id_empresa, id_categoria, id_iva,
                       nombre, precio_unitario, stock_actual,
                       stock_minimo, fecha_caducidad
                FROM producto
                WHERE id_producto = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Producto> buscarPorIdYEmpresa(
            UUID idProducto,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_producto, id_empresa, id_categoria, id_iva,
                       nombre, precio_unitario, stock_actual,
                       stock_minimo, fecha_caducidad
                FROM producto
                WHERE id_producto = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, idProducto);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Producto insertar(Producto obj)
            throws SQLException {

        String sql = """
                INSERT INTO producto
                (
                    id_empresa,
                    id_categoria,
                    id_iva,
                    nombre,
                    precio_unitario,
                    stock_actual,
                    stock_minimo,
                    fecha_caducidad
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_producto
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setInt(2, obj.getIdCategoria());
            ps.setInt(3, obj.getIdIva());
            ps.setString(4, obj.getNombre());
            ps.setBigDecimal(5, obj.getPrecioUnitario());
            ps.setInt(6, obj.getStockActual());
            ps.setInt(7, obj.getStockMinimo());

            if (obj.getFechaCaducidad() != null) {
                ps.setObject(8, obj.getFechaCaducidad());
            } else {
                ps.setNull(8, Types.DATE);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdProducto(
                        rs.getObject(
                                "id_producto",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(Producto obj)
            throws SQLException {

        String sql = """
                UPDATE producto
                SET id_empresa = ?,
                    id_categoria = ?,
                    id_iva = ?,
                    nombre = ?,
                    precio_unitario = ?,
                    stock_actual = ?,
                    stock_minimo = ?,
                    fecha_caducidad = ?
                WHERE id_producto = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setInt(2, obj.getIdCategoria());
            ps.setInt(3, obj.getIdIva());
            ps.setString(4, obj.getNombre());
            ps.setBigDecimal(5, obj.getPrecioUnitario());
            ps.setInt(6, obj.getStockActual());
            ps.setInt(7, obj.getStockMinimo());
            ps.setObject(8, obj.getFechaCaducidad());
            ps.setObject(9, obj.getIdProducto());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Producto obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE producto
                SET id_categoria = ?,
                    id_iva = ?,
                    nombre = ?,
                    precio_unitario = ?,
                    stock_actual = ?,
                    stock_minimo = ?,
                    fecha_caducidad = ?
                WHERE id_producto = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, obj.getIdCategoria());
            ps.setInt(2, obj.getIdIva());
            ps.setString(3, obj.getNombre());
            ps.setBigDecimal(4, obj.getPrecioUnitario());
            ps.setInt(5, obj.getStockActual());
            ps.setInt(6, obj.getStockMinimo());
            ps.setObject(7, obj.getFechaCaducidad());
            ps.setObject(8, obj.getIdProducto());
            ps.setObject(9, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public boolean eliminar(UUID idProducto)
            throws SQLException {

        String sql = """
                DELETE FROM producto
                WHERE id_producto = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, idProducto);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idProducto,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM producto
                WHERE id_producto = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setObject(1, idProducto);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Producto mapear(ResultSet rs)
            throws SQLException {

        Producto obj = new Producto();

        obj.setIdProducto(
                rs.getObject("id_producto", UUID.class)
        );

        obj.setIdEmpresa(
                rs.getObject("id_empresa", UUID.class)
        );

        obj.setIdCategoria(
                rs.getInt("id_categoria")
        );

        obj.setIdIva(
                rs.getInt("id_iva")
        );

        obj.setNombre(
                rs.getString("nombre")
        );

        obj.setPrecioUnitario(
                rs.getBigDecimal("precio_unitario")
        );

        obj.setStockActual(
                rs.getInt("stock_actual")
        );

        obj.setStockMinimo(
                rs.getInt("stock_minimo")
        );

        obj.setFechaCaducidad(
                rs.getObject(
                        "fecha_caducidad",
                        LocalDate.class
                )
        );

        return obj;
    }
}