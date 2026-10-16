package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.RolPermiso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolPermisoDAO {
    public List<RolPermiso> listar() throws SQLException {
        String sql = "SELECT id_rol, id_permiso FROM rol_permiso ORDER BY id_rol, id_permiso";
        List<RolPermiso> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<RolPermiso> buscarPorId(Integer idRol, Integer idPermiso) throws SQLException {
        String sql = "SELECT id_rol, id_permiso FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRol);
            ps.setObject(2, idPermiso);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public RolPermiso insertar(RolPermiso obj) throws SQLException {
        String sql = "INSERT INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdRol());
            ps.setObject(2, obj.getIdPermiso());
            ps.executeUpdate();
        }
        return obj;
    }

    public boolean actualizar(RolPermiso obj) throws SQLException {
        return buscarPorId(obj.getIdRol(), obj.getIdPermiso()).isPresent();
    }

    public boolean eliminar(Integer idRol, Integer idPermiso) throws SQLException {
        String sql = "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRol);
            ps.setObject(2, idPermiso);
            return ps.executeUpdate() > 0;
        }
    }

    private RolPermiso mapear(ResultSet rs) throws SQLException {
        RolPermiso obj = new RolPermiso();
        obj.setIdRol((Integer) rs.getObject("id_rol"));
        obj.setIdPermiso((Integer) rs.getObject("id_permiso"));
        return obj;
    }
}
