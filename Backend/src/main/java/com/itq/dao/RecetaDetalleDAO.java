package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.RecetaDetalle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaDetalleDAO {
    public List<RecetaDetalle> listar() throws SQLException {
        String sql = "SELECT id_detalle_receta, id_receta, id_producto, dosis, frecuencia, duracion_dias FROM receta_detalle ORDER BY id_detalle_receta";
        List<RecetaDetalle> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * receta_detalle no guarda id_empresa: la pertenencia se resuelve por la
     * receta padre, por eso el listado se filtra con un JOIN.
     */
    public List<RecetaDetalle> listarPorEmpresa(UUID idEmpresa) throws SQLException {
        String sql = "SELECT rd.id_detalle_receta, rd.id_receta, rd.id_producto, rd.dosis, rd.frecuencia, rd.duracion_dias "
                + "FROM receta_detalle rd INNER JOIN receta r ON r.id_receta = rd.id_receta "
                + "WHERE r.id_empresa = ? ORDER BY rd.id_detalle_receta";
        List<RecetaDetalle> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<RecetaDetalle> buscarPorId(UUID idDetalleReceta) throws SQLException {
        String sql = "SELECT id_detalle_receta, id_receta, id_producto, dosis, frecuencia, duracion_dias FROM receta_detalle WHERE id_detalle_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalleReceta);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Optional<RecetaDetalle> buscarPorIdYEmpresa(UUID idDetalleReceta, UUID idEmpresa) throws SQLException {
        String sql = "SELECT rd.id_detalle_receta, rd.id_receta, rd.id_producto, rd.dosis, rd.frecuencia, rd.duracion_dias "
                + "FROM receta_detalle rd INNER JOIN receta r ON r.id_receta = rd.id_receta "
                + "WHERE rd.id_detalle_receta = ? AND r.id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalleReceta);
            ps.setObject(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public RecetaDetalle insertar(RecetaDetalle obj) throws SQLException {
        String sql = "INSERT INTO receta_detalle (id_receta, id_producto, dosis, frecuencia, duracion_dias) VALUES (?, ?, ?, ?, ?) RETURNING id_detalle_receta";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdReceta());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getDosis());
            ps.setObject(4, obj.getFrecuencia());
            ps.setObject(5, obj.getDuracionDias());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdDetalleReceta(rs.getObject("id_detalle_receta", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(RecetaDetalle obj) throws SQLException {
        String sql = "UPDATE receta_detalle SET id_receta = ?, id_producto = ?, dosis = ?, frecuencia = ?, duracion_dias = ? WHERE id_detalle_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdReceta());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getDosis());
            ps.setObject(4, obj.getFrecuencia());
            ps.setObject(5, obj.getDuracionDias());
            ps.setObject(6, obj.getIdDetalleReceta());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(RecetaDetalle obj, UUID idEmpresa) throws SQLException {
        String sql = "UPDATE receta_detalle SET id_receta = ?, id_producto = ?, dosis = ?, frecuencia = ?, duracion_dias = ? "
                + "WHERE id_detalle_receta = ? AND id_receta IN (SELECT id_receta FROM receta WHERE id_empresa = ?)";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdReceta());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getDosis());
            ps.setObject(4, obj.getFrecuencia());
            ps.setObject(5, obj.getDuracionDias());
            ps.setObject(6, obj.getIdDetalleReceta());
            ps.setObject(7, idEmpresa);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idDetalleReceta) throws SQLException {
        String sql = "DELETE FROM receta_detalle WHERE id_detalle_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalleReceta);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(UUID idDetalleReceta, UUID idEmpresa) throws SQLException {
        String sql = "DELETE FROM receta_detalle WHERE id_detalle_receta = ? "
                + "AND id_receta IN (SELECT id_receta FROM receta WHERE id_empresa = ?)";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idDetalleReceta);
            ps.setObject(2, idEmpresa);
            return ps.executeUpdate() > 0;
        }
    }

    private RecetaDetalle mapear(ResultSet rs) throws SQLException {
        RecetaDetalle obj = new RecetaDetalle();
        obj.setIdDetalleReceta(rs.getObject("id_detalle_receta", UUID.class));
        obj.setIdReceta(rs.getObject("id_receta", UUID.class));
        obj.setIdProducto(rs.getObject("id_producto", UUID.class));
        obj.setDosis(rs.getString("dosis"));
        obj.setFrecuencia(rs.getString("frecuencia"));
        obj.setDuracionDias((Integer) rs.getObject("duracion_dias"));
        return obj;
    }
}
