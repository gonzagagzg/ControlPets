package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Permiso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermisoDAO {
    public List<Permiso> listar() throws SQLException {
        String sql = "SELECT id_permiso, modulo, accion FROM permiso ORDER BY id_permiso";
        List<Permiso> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Permiso> buscarPorId(Integer idPermiso) throws SQLException {
        String sql = "SELECT id_permiso, modulo, accion FROM permiso WHERE id_permiso = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idPermiso);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Permiso insertar(Permiso obj) throws SQLException {
        String sql = "INSERT INTO permiso (modulo, accion) VALUES (?, ?) RETURNING id_permiso";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getModulo());
            ps.setObject(2, obj.getAccion());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdPermiso((Integer) rs.getObject("id_permiso"));
            }
        }
        return obj;
    }

    public boolean actualizar(Permiso obj) throws SQLException {
        String sql = "UPDATE permiso SET modulo = ?, accion = ? WHERE id_permiso = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getModulo());
            ps.setObject(2, obj.getAccion());
            ps.setObject(3, obj.getIdPermiso());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idPermiso) throws SQLException {
        String sql = "DELETE FROM permiso WHERE id_permiso = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idPermiso);
            return ps.executeUpdate() > 0;
        }
    }

    private Permiso mapear(ResultSet rs) throws SQLException {
        Permiso obj = new Permiso();
        obj.setIdPermiso((Integer) rs.getObject("id_permiso"));
        obj.setModulo(rs.getString("modulo"));
        obj.setAccion(rs.getString("accion"));
        return obj;
    }
}
