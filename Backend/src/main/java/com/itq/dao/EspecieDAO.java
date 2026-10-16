package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Especie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EspecieDAO {
    public List<Especie> listar() throws SQLException {
        String sql = "SELECT id_especie, nombre FROM especie ORDER BY id_especie";
        List<Especie> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Especie> buscarPorId(Integer idEspecie) throws SQLException {
        String sql = "SELECT id_especie, nombre FROM especie WHERE id_especie = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEspecie);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Especie insertar(Especie obj) throws SQLException {
        String sql = "INSERT INTO especie (nombre) VALUES (?) RETURNING id_especie";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdEspecie((Integer) rs.getObject("id_especie"));
            }
        }
        return obj;
    }

    public boolean actualizar(Especie obj) throws SQLException {
        String sql = "UPDATE especie SET nombre = ? WHERE id_especie = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            ps.setObject(2, obj.getIdEspecie());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idEspecie) throws SQLException {
        String sql = "DELETE FROM especie WHERE id_especie = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEspecie);
            return ps.executeUpdate() > 0;
        }
    }

    private Especie mapear(ResultSet rs) throws SQLException {
        Especie obj = new Especie();
        obj.setIdEspecie((Integer) rs.getObject("id_especie"));
        obj.setNombre(rs.getString("nombre"));
        return obj;
    }
}
