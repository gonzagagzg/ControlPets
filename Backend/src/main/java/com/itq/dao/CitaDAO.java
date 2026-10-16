package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Cita;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CitaDAO {

    public List<Cita> listar() throws SQLException {

        String sql = """
                SELECT id_cita, id_empresa, id_mascota,
                       id_veterinario, fecha_hora, estado
                FROM cita
                ORDER BY fecha_hora
                """;

        List<Cita> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    public List<Cita> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT id_cita, id_empresa, id_mascota,
                       id_veterinario, fecha_hora, estado
                FROM cita
                WHERE id_empresa = ?
                ORDER BY fecha_hora
                """;

        List<Cita> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    public Optional<Cita> buscarPorId(UUID idCita)
            throws SQLException {

        String sql = """
                SELECT id_cita, id_empresa, id_mascota,
                       id_veterinario, fecha_hora, estado
                FROM cita
                WHERE id_cita = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idCita);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Cita> buscarPorIdYEmpresa(
            UUID idCita,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_cita, id_empresa, id_mascota,
                       id_veterinario, fecha_hora, estado
                FROM cita
                WHERE id_cita = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idCita);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Cita insertar(Cita obj)
            throws SQLException {

        String sql = """
                INSERT INTO cita
                (id_empresa, id_mascota, id_veterinario,
                 fecha_hora, estado)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id_cita
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdMascota());
            ps.setObject(3, obj.getIdVeterinario());
            ps.setObject(4, obj.getFechaHora());
            ps.setString(5, obj.getEstado());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdCita(
                        rs.getObject(
                                "id_cita",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    public boolean actualizar(Cita obj)
            throws SQLException {

        String sql = """
                UPDATE cita
                SET id_empresa = ?,
                    id_mascota = ?,
                    id_veterinario = ?,
                    fecha_hora = ?,
                    estado = ?
                WHERE id_cita = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdMascota());
            ps.setObject(3, obj.getIdVeterinario());
            ps.setObject(4, obj.getFechaHora());
            ps.setString(5, obj.getEstado());
            ps.setObject(6, obj.getIdCita());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Cita obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE cita
                SET id_mascota = ?,
                    id_veterinario = ?,
                    fecha_hora = ?,
                    estado = ?
                WHERE id_cita = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdMascota());
            ps.setObject(2, obj.getIdVeterinario());
            ps.setObject(3, obj.getFechaHora());
            ps.setString(4, obj.getEstado());
            ps.setObject(5, obj.getIdCita());
            ps.setObject(6, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idCita)
            throws SQLException {

        String sql = """
                DELETE FROM cita
                WHERE id_cita = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idCita);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idCita,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM cita
                WHERE id_cita = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idCita);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    private Cita mapear(ResultSet rs)
            throws SQLException {

        Cita obj = new Cita();

        obj.setIdCita(
                rs.getObject("id_cita", UUID.class)
        );

        obj.setIdEmpresa(
                rs.getObject("id_empresa", UUID.class)
        );

        obj.setIdMascota(
                rs.getObject("id_mascota", UUID.class)
        );

        obj.setIdVeterinario(
                rs.getObject("id_veterinario", UUID.class)
        );

        obj.setFechaHora(
                rs.getObject(
                        "fecha_hora",
                        OffsetDateTime.class
                )
        );

        obj.setEstado(
                rs.getString("estado")
        );

        return obj;
    }
}