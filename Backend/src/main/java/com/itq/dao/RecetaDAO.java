package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Receta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaDAO {
    private static final String COLUMNAS = "id_receta, id_empresa, id_historial, indicaciones_generales";

    public List<Receta> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM receta ORDER BY id_receta";
        List<Receta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Receta> listarPorEmpresa(UUID idEmpresa) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM receta WHERE id_empresa = ? ORDER BY id_receta";
        List<Receta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Receta> buscarPorId(UUID idReceta) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM receta WHERE id_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idReceta);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Optional<Receta> buscarPorIdYEmpresa(UUID idReceta, UUID idEmpresa) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM receta WHERE id_receta = ? AND id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idReceta);
            ps.setObject(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Receta insertar(Receta obj) throws SQLException {
        String sql = "INSERT INTO receta (id_empresa, id_historial, indicaciones_generales) VALUES (?, ?, ?) RETURNING id_receta";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdHistorial());
            ps.setObject(3, obj.getIndicacionesGenerales());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdReceta(rs.getObject("id_receta", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Receta obj) throws SQLException {
        String sql = "UPDATE receta SET id_empresa = ?, id_historial = ?, indicaciones_generales = ? WHERE id_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdHistorial());
            ps.setObject(3, obj.getIndicacionesGenerales());
            ps.setObject(4, obj.getIdReceta());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(Receta obj, UUID idEmpresa) throws SQLException {
        String sql = "UPDATE receta SET id_historial = ?, indicaciones_generales = ? WHERE id_receta = ? AND id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdHistorial());
            ps.setObject(2, obj.getIndicacionesGenerales());
            ps.setObject(3, obj.getIdReceta());
            ps.setObject(4, idEmpresa);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idReceta) throws SQLException {
        String sql = "DELETE FROM receta WHERE id_receta = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idReceta);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(UUID idReceta, UUID idEmpresa) throws SQLException {
        String sql = "DELETE FROM receta WHERE id_receta = ? AND id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idReceta);
            ps.setObject(2, idEmpresa);
            return ps.executeUpdate() > 0;
        }
    }

    private Receta mapear(ResultSet rs) throws SQLException {
        Receta obj = new Receta();
        obj.setIdReceta(rs.getObject("id_receta", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdHistorial(rs.getObject("id_historial", UUID.class));
        obj.setIndicacionesGenerales(rs.getString("indicaciones_generales"));
        return obj;
    }
}
