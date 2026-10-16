package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolDAO {
    public List<Rol> listar() throws SQLException {

        String sql = "SELECT id_rol, nombre FROM rol ORDER BY id_rol";
        List<Rol> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Rol> listarOperativos() throws SQLException {
        String sql = """
                SELECT id_rol, nombre
                FROM rol
                WHERE nombre NOT IN (
                'Administrador Global',
                'Administrador Local',
                'SuperUsuario'
                
                )
                ORDER BY id_rol
                
                """;

        List<Rol> lista = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Rol> buscarPorId(Integer idRol) throws SQLException {
        String sql = "SELECT id_rol, nombre FROM rol WHERE id_rol = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRol);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Rol insertar(Rol obj) throws SQLException {
        String sql = "INSERT INTO rol (nombre) VALUES (?) RETURNING id_rol";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdRol((Integer) rs.getObject("id_rol"));
            }
        }
        return obj;
    }

    public boolean actualizar(Rol obj) throws SQLException {
        String sql = "UPDATE rol SET nombre = ? WHERE id_rol = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            ps.setObject(2, obj.getIdRol());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idRol) throws SQLException {
        String sql = "DELETE FROM rol WHERE id_rol = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRol);
            return ps.executeUpdate() > 0;
        }
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        Rol obj = new Rol();
        obj.setIdRol((Integer) rs.getObject("id_rol"));
        obj.setNombre(rs.getString("nombre"));
        return obj;
    }
}
