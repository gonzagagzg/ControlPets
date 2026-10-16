package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.MascotaVacuna;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaVacunaDAO {
    public List<MascotaVacuna> listar() throws SQLException {
        String sql = "SELECT id_mascota_vacuna, id_empresa, id_mascota, id_vacuna, fecha_aplicacion FROM mascota_vacuna ORDER BY id_mascota_vacuna";
        List<MascotaVacuna> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<MascotaVacuna> buscarPorId(UUID idMascotaVacuna) throws SQLException {
        String sql = "SELECT id_mascota_vacuna, id_empresa, id_mascota, id_vacuna, fecha_aplicacion FROM mascota_vacuna WHERE id_mascota_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idMascotaVacuna);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public MascotaVacuna insertar(MascotaVacuna obj) throws SQLException {
        if (obj.getFechaAplicacion().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException("La fecha de aplicación debe ser actual o futura");

        }


        String sql = "INSERT INTO mascota_vacuna (id_empresa, id_mascota, id_vacuna, fecha_aplicacion) VALUES (?, ?, ?, ?) RETURNING id_mascota_vacuna";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdMascota());
            ps.setObject(3, obj.getIdVacuna());
            ps.setObject(4, obj.getFechaAplicacion());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdMascotaVacuna(rs.getObject("id_mascota_vacuna", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(MascotaVacuna obj) throws SQLException {
        if (obj.getFechaAplicacion().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException("La fecha de aplicación debe ser actual o futura");

        }

        String sql = "UPDATE mascota_vacuna SET id_empresa = ?, id_mascota = ?, id_vacuna = ?, fecha_aplicacion = ? WHERE id_mascota_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdMascota());
            ps.setObject(3, obj.getIdVacuna());
            ps.setObject(4, obj.getFechaAplicacion());
            ps.setObject(5, obj.getIdMascotaVacuna());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idMascotaVacuna) throws SQLException {
        String sql = "DELETE FROM mascota_vacuna WHERE id_mascota_vacuna = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idMascotaVacuna);
            return ps.executeUpdate() > 0;
        }
    }

    private MascotaVacuna mapear(ResultSet rs) throws SQLException {
        MascotaVacuna obj = new MascotaVacuna();
        obj.setIdMascotaVacuna(rs.getObject("id_mascota_vacuna", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdMascota(rs.getObject("id_mascota", UUID.class));
        obj.setIdVacuna((Integer) rs.getObject("id_vacuna"));
        obj.setFechaAplicacion(rs.getObject("fecha_aplicacion", LocalDate.class));
        return obj;
    }
}
