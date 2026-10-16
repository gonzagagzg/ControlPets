package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Veterinario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioDAO {

    // =========================================================
    // SUPERUSUARIO - LISTADO GLOBAL
    // =========================================================

    public List<Veterinario> listar() throws SQLException {

        String sql = """
                SELECT v.id_veterinario,
                       v.id_usuario,
                       v.id_empresa,
                       v.codigo_veterinario,
                       v.especialidad,
                       u.usuario,
                       u.nombres
                FROM veterinario v
                INNER JOIN usuario u ON u.id_usuario = v.id_usuario
                ORDER BY v.id_veterinario
                """;

        List<Veterinario> lista = new ArrayList<>();

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

    // =========================================================
    // USUARIO NORMAL - LISTADO POR EMPRESA
    // =========================================================

    public List<Veterinario> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT v.id_veterinario,
                       v.id_usuario,
                       v.id_empresa,
                       v.codigo_veterinario,
                       v.especialidad,
                       u.usuario,
                       u.nombres
                FROM veterinario v
                INNER JOIN usuario u ON u.id_usuario = v.id_usuario
                WHERE v.id_empresa = ?
                ORDER BY v.id_veterinario
                """;

        List<Veterinario> lista = new ArrayList<>();

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

    // =========================================================
    // BÚSQUEDA
    // =========================================================

    public Optional<Veterinario> buscarPorId(UUID idVeterinario)
            throws SQLException {

        String sql = """
                SELECT v.id_veterinario,
                       v.id_usuario,
                       v.id_empresa,
                       v.codigo_veterinario,
                       v.especialidad,
                       u.usuario,
                       u.nombres
                FROM veterinario v
                INNER JOIN usuario u ON u.id_usuario = v.id_usuario
                WHERE v.id_veterinario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idVeterinario);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Veterinario> buscarPorIdYEmpresa(
            UUID idVeterinario,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT v.id_veterinario,
                       v.id_usuario,
                       v.id_empresa,
                       v.codigo_veterinario,
                       v.especialidad,
                       u.usuario,
                       u.nombres
                FROM veterinario v
                INNER JOIN usuario u ON u.id_usuario = v.id_usuario
                WHERE v.id_veterinario = ?
                  AND v.id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idVeterinario);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Veterinario insertar(Veterinario obj)
            throws SQLException {

        String sql = """
                INSERT INTO veterinario
                (id_usuario, id_empresa, codigo_veterinario, especialidad)
                VALUES (?, ?, ?, ?)
                RETURNING id_veterinario
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdUsuario());
            ps.setObject(2, obj.getIdEmpresa());
            ps.setString(3, obj.getCodigoVeterinario());
            ps.setString(4, obj.getEspecialidad());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdVeterinario(
                        rs.getObject(
                                "id_veterinario",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(Veterinario obj)
            throws SQLException {

        String sql = """
                UPDATE veterinario
                SET id_usuario = ?,
                    id_empresa = ?,
                    codigo_veterinario = ?,
                    especialidad = ?
                WHERE id_veterinario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdUsuario());
            ps.setObject(2, obj.getIdEmpresa());
            ps.setString(3, obj.getCodigoVeterinario());
            ps.setString(4, obj.getEspecialidad());
            ps.setObject(5, obj.getIdVeterinario());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Veterinario obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE veterinario
                SET id_usuario = ?,
                    codigo_veterinario = ?,
                    especialidad = ?
                WHERE id_veterinario = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdUsuario());
            ps.setString(2, obj.getCodigoVeterinario());
            ps.setString(3, obj.getEspecialidad());
            ps.setObject(4, obj.getIdVeterinario());
            ps.setObject(5, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public boolean eliminar(UUID idVeterinario)
            throws SQLException {

        String sql = """
                DELETE FROM veterinario
                WHERE id_veterinario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idVeterinario);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idVeterinario,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM veterinario
                WHERE id_veterinario = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idVeterinario);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Veterinario mapear(ResultSet rs)
            throws SQLException {

        Veterinario obj = new Veterinario();

        obj.setIdVeterinario(
                rs.getObject(
                        "id_veterinario",
                        UUID.class
                )
        );

        obj.setIdUsuario(
                rs.getObject(
                        "id_usuario",
                        UUID.class
                )
        );

        obj.setIdEmpresa(
                rs.getObject(
                        "id_empresa",
                        UUID.class
                )
        );

        obj.setCodigoVeterinario(
                rs.getString("codigo_veterinario")
        );

        obj.setEspecialidad(
                rs.getString("especialidad")
        );

        obj.setUsuario(
                rs.getString("usuario")
        );

        obj.setNombres(
                rs.getString("nombres")
        );

        return obj;
    }
}