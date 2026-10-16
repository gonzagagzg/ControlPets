package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.SriIva;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SriIvaDAO {
    public List<SriIva> listar() throws SQLException {
        String sql = "SELECT id_iva, porcentaje, codigo_sri FROM sri_iva ORDER BY id_iva";
        List<SriIva> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<SriIva> buscarPorId(Integer idIva) throws SQLException {
        String sql = "SELECT id_iva, porcentaje, codigo_sri FROM sri_iva WHERE id_iva = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idIva);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public SriIva insertar(SriIva obj) throws SQLException {
        String sql = "INSERT INTO sri_iva (porcentaje, codigo_sri) VALUES (?, ?) RETURNING id_iva";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getPorcentaje());
            ps.setObject(2, obj.getCodigoSri());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdIva((Integer) rs.getObject("id_iva"));
            }
        }
        return obj;
    }

    public boolean actualizar(SriIva obj) throws SQLException {
        String sql = "UPDATE sri_iva SET porcentaje = ?, codigo_sri = ? WHERE id_iva = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getPorcentaje());
            ps.setObject(2, obj.getCodigoSri());
            ps.setObject(3, obj.getIdIva());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idIva) throws SQLException {
        String sql = "DELETE FROM sri_iva WHERE id_iva = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idIva);
            return ps.executeUpdate() > 0;
        }
    }

    private SriIva mapear(ResultSet rs) throws SQLException {
        SriIva obj = new SriIva();
        obj.setIdIva((Integer) rs.getObject("id_iva"));
        obj.setPorcentaje(rs.getBigDecimal("porcentaje"));
        obj.setCodigoSri(rs.getString("codigo_sri"));
        return obj;
    }
}
