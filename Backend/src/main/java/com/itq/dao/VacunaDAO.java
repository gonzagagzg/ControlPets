package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Vacuna;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VacunaDAO {
    public List<Vacuna> listar() throws SQLException {
        String sql = "SELECT id_vacuna, nombre FROM vacuna ORDER BY id_vacuna";
        List<Vacuna> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Vacuna> buscarPorId(Integer idVacuna) throws SQLException {
        String sql = "SELECT id_vacuna, nombre FROM vacuna WHERE id_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idVacuna);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Vacuna insertar(Vacuna obj) throws SQLException {
        String sql = "INSERT INTO vacuna (nombre) VALUES (?) RETURNING id_vacuna";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdVacuna((Integer) rs.getObject("id_vacuna"));
            }
        }
        return obj;
    }

    public boolean actualizar(Vacuna obj) throws SQLException {
        String sql = "UPDATE vacuna SET nombre = ? WHERE id_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getNombre());
            ps.setObject(2, obj.getIdVacuna());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idVacuna) throws SQLException {
        String sql = "DELETE FROM vacuna WHERE id_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idVacuna);
            return ps.executeUpdate() > 0;
        }
    }

    private Vacuna mapear(ResultSet rs) throws SQLException {
        Vacuna obj = new Vacuna();
        obj.setIdVacuna((Integer) rs.getObject("id_vacuna"));
        obj.setNombre(rs.getString("nombre"));
        return obj;
    }
}
