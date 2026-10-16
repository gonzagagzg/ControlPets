package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Raza;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RazaDAO {
    public List<Raza> listar() throws SQLException {
        String sql = "SELECT id_raza, id_especie, nombre FROM raza ORDER BY id_raza";
        List<Raza> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Raza> buscarPorId(Integer idRaza) throws SQLException {
        String sql = "SELECT id_raza, id_especie, nombre FROM raza WHERE id_raza = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRaza);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Raza insertar(Raza obj) throws SQLException {
        String sql = "INSERT INTO raza (id_especie, nombre) VALUES (?, ?) RETURNING id_raza";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEspecie());
            ps.setObject(2, obj.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdRaza((Integer) rs.getObject("id_raza"));
            }
        }
        return obj;
    }

    public boolean actualizar(Raza obj) throws SQLException {
        String sql = "UPDATE raza SET id_especie = ?, nombre = ? WHERE id_raza = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEspecie());
            ps.setObject(2, obj.getNombre());
            ps.setObject(3, obj.getIdRaza());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idRaza) throws SQLException {
        String sql = "DELETE FROM raza WHERE id_raza = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idRaza);
            return ps.executeUpdate() > 0;
        }
    }

    private Raza mapear(ResultSet rs) throws SQLException {
        Raza obj = new Raza();
        obj.setIdRaza((Integer) rs.getObject("id_raza"));
        obj.setIdEspecie((Integer) rs.getObject("id_especie"));
        obj.setNombre(rs.getString("nombre"));
        return obj;
    }
}
