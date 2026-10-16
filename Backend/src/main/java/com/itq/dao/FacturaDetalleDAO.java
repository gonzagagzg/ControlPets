package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.FacturaDetalle;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaDetalleDAO {
    public List<FacturaDetalle> listar() throws SQLException {
        String sql = "SELECT id_detalle, id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal FROM factura_detalle ORDER BY id_detalle";
        List<FacturaDetalle> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<FacturaDetalle> buscarPorId(UUID idDetalle) throws SQLException {
        String sql = "SELECT id_detalle, id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal FROM factura_detalle WHERE id_detalle = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public FacturaDetalle insertar(FacturaDetalle obj) throws SQLException {
        String sql = "INSERT INTO factura_detalle (id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_detalle";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdFactura());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getIdIva());
            ps.setObject(4, obj.getCantidad());
            ps.setObject(5, obj.getPrecioUnitario());
            ps.setObject(6, obj.getSubtotal());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdDetalle(rs.getObject("id_detalle", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(FacturaDetalle obj) throws SQLException {
        String sql = "UPDATE factura_detalle SET id_factura = ?, id_producto = ?, id_iva = ?, cantidad = ?, precio_unitario = ?, subtotal = ? WHERE id_detalle = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdFactura());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getIdIva());
            ps.setObject(4, obj.getCantidad());
            ps.setObject(5, obj.getPrecioUnitario());
            ps.setObject(6, obj.getSubtotal());
            ps.setObject(7, obj.getIdDetalle());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idDetalle) throws SQLException {
        String sql = "DELETE FROM factura_detalle WHERE id_detalle = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalle);
            return ps.executeUpdate() > 0;
        }
    }

    private FacturaDetalle mapear(ResultSet rs) throws SQLException {
        FacturaDetalle obj = new FacturaDetalle();
        obj.setIdDetalle(rs.getObject("id_detalle", UUID.class));
        obj.setIdFactura(rs.getObject("id_factura", UUID.class));
        obj.setIdProducto(rs.getObject("id_producto", UUID.class));
        obj.setIdIva((Integer) rs.getObject("id_iva"));
        obj.setCantidad((Integer) rs.getObject("cantidad"));
        obj.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        obj.setSubtotal(rs.getBigDecimal("subtotal"));
        return obj;
    }
}
