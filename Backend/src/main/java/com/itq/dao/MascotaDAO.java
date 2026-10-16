package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Mascota;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaDAO {

    // =========================================================
    // SUPERUSUARIO - listado global
    // =========================================================

    public List<Mascota> listar() throws SQLException {

        String sql = """
                SELECT id_mascota, id_empresa, id_cliente,
                       id_raza, nombre, fecha_nacimiento
                FROM mascota
                ORDER BY id_mascota
                """;

        List<Mascota> lista = new ArrayList<>();

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
    // USUARIO NORMAL - listado por empresa
    // =========================================================

    public List<Mascota> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT id_mascota, id_empresa, id_cliente,
                       id_raza, nombre, fecha_nacimiento
                FROM mascota
                WHERE id_empresa = ?
                ORDER BY id_mascota
                """;

        List<Mascota> lista = new ArrayList<>();

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

    public Optional<Mascota> buscarPorId(UUID idMascota)
            throws SQLException {

        String sql = """
                SELECT id_mascota, id_empresa, id_cliente,
                       id_raza, nombre, fecha_nacimiento
                FROM mascota
                WHERE id_mascota = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idMascota);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Mascota> buscarPorIdYEmpresa(
            UUID idMascota,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_mascota, id_empresa, id_cliente,
                       id_raza, nombre, fecha_nacimiento
                FROM mascota
                WHERE id_mascota = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idMascota);
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

    public Mascota insertar(Mascota obj)
            throws SQLException {

        String sql = """
                INSERT INTO mascota
                (id_empresa, id_cliente, id_raza,
                 nombre, fecha_nacimiento)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id_mascota
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCliente());
            ps.setObject(3, obj.getIdRaza());
            ps.setString(4, obj.getNombre());
            ps.setObject(5, obj.getFechaNacimiento());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdMascota(
                        rs.getObject("id_mascota", UUID.class)
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(Mascota obj)
            throws SQLException {

        String sql = """
                UPDATE mascota
                SET id_empresa = ?,
                    id_cliente = ?,
                    id_raza = ?,
                    nombre = ?,
                    fecha_nacimiento = ?
                WHERE id_mascota = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCliente());
            ps.setObject(3, obj.getIdRaza());
            ps.setString(4, obj.getNombre());
            ps.setObject(5, obj.getFechaNacimiento());
            ps.setObject(6, obj.getIdMascota());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Mascota obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE mascota
                SET id_cliente = ?,
                    id_raza = ?,
                    nombre = ?,
                    fecha_nacimiento = ?
                WHERE id_mascota = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdCliente());
            ps.setObject(2, obj.getIdRaza());
            ps.setString(3, obj.getNombre());
            ps.setObject(4, obj.getFechaNacimiento());
            ps.setObject(5, obj.getIdMascota());
            ps.setObject(6, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public boolean eliminar(UUID idMascota)
            throws SQLException {

        String sql = """
                DELETE FROM mascota
                WHERE id_mascota = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idMascota);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idMascota,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM mascota
                WHERE id_mascota = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idMascota);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    private Mascota mapear(ResultSet rs)
            throws SQLException {

        Mascota obj = new Mascota();

        obj.setIdMascota(
                rs.getObject("id_mascota", UUID.class)
        );

        obj.setIdEmpresa(
                rs.getObject("id_empresa", UUID.class)
        );

        obj.setIdCliente(
                rs.getObject("id_cliente", UUID.class)
        );

        obj.setIdRaza(
                (Integer) rs.getObject("id_raza")
        );

        obj.setNombre(
                rs.getString("nombre")
        );

        obj.setFechaNacimiento(
                rs.getObject(
                        "fecha_nacimiento",
                        LocalDate.class
                )
        );

        return obj;
    }
}