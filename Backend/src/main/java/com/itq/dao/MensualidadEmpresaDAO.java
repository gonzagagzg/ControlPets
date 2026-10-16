package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.MensualidadEmpresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MensualidadEmpresaDAO {

    // =========================================================
    // LISTAR TODAS LAS MENSUALIDADES ACTIVAS
    // =========================================================

    public List<MensualidadEmpresa> listar()
            throws SQLException {

        String sql = """
                SELECT
                    id_mensualidad,
                    id_empresa,
                    periodo,
                    valor,
                    fecha_vencimiento,
                    fecha_pago,
                    estado,
                    observacion,
                    activo
                FROM mensualidad_empresa
                WHERE activo = TRUE
                ORDER BY fecha_vencimiento DESC
                """;

        List<MensualidadEmpresa> lista =
                new ArrayList<>();

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(
                        mapear(rs)
                );
            }
        }

        return lista;
    }

    // =========================================================
    // LISTAR MENSUALIDADES ACTIVAS POR EMPRESA
    // =========================================================

    public List<MensualidadEmpresa> listarPorEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT
                    id_mensualidad,
                    id_empresa,
                    periodo,
                    valor,
                    fecha_vencimiento,
                    fecha_pago,
                    estado,
                    observacion,
                    activo
                FROM mensualidad_empresa
                WHERE id_empresa = ?
                  AND activo = TRUE
                ORDER BY fecha_vencimiento DESC
                """;

        List<MensualidadEmpresa> lista =
                new ArrayList<>();

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    lista.add(
                            mapear(rs)
                    );
                }
            }
        }

        return lista;
    }

    // =========================================================
    // BUSCAR POR ID - SOLO REGISTROS ACTIVOS
    // =========================================================

    public Optional<MensualidadEmpresa> buscarPorId(
            UUID idMensualidad
    ) throws SQLException {

        String sql = """
                SELECT
                    id_mensualidad,
                    id_empresa,
                    periodo,
                    valor,
                    fecha_vencimiento,
                    fecha_pago,
                    estado,
                    observacion,
                    activo
                FROM mensualidad_empresa
                WHERE id_mensualidad = ?
                  AND activo = TRUE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idMensualidad
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        ? Optional.of(
                        mapear(rs)
                )
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // BUSCAR POR ID INCLUYENDO INACTIVOS
    // =========================================================
    // Útil para auditoría o validaciones internas.
    // =========================================================

    public Optional<MensualidadEmpresa> buscarPorIdIncluyendoInactivos(
            UUID idMensualidad
    ) throws SQLException {

        String sql = """
                SELECT
                    id_mensualidad,
                    id_empresa,
                    periodo,
                    valor,
                    fecha_vencimiento,
                    fecha_pago,
                    estado,
                    observacion,
                    activo
                FROM mensualidad_empresa
                WHERE id_mensualidad = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idMensualidad
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        ? Optional.of(
                        mapear(rs)
                )
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public MensualidadEmpresa insertar(
            MensualidadEmpresa obj
    ) throws SQLException {

        String sql = """
                INSERT INTO mensualidad_empresa
                (
                    id_empresa,
                    periodo,
                    valor,
                    fecha_vencimiento,
                    fecha_pago,
                    estado,
                    observacion,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                RETURNING id_mensualidad
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    obj.getIdEmpresa()
            );

            ps.setString(
                    2,
                    obj.getPeriodo()
            );

            ps.setBigDecimal(
                    3,
                    obj.getValor()
            );

            ps.setObject(
                    4,
                    obj.getFechaVencimiento()
            );

            ps.setObject(
                    5,
                    obj.getFechaPago()
            );

            ps.setString(
                    6,
                    obj.getEstado()
            );

            ps.setString(
                    7,
                    obj.getObservacion()
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdMensualidad(
                        rs.getObject(
                                "id_mensualidad",
                                UUID.class
                        )
                );

                obj.setActivo(
                        true
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR - SOLO SI ESTÁ ACTIVO
    // =========================================================

    public boolean actualizar(
            MensualidadEmpresa obj
    ) throws SQLException {

        String sql = """
                UPDATE mensualidad_empresa
                SET id_empresa = ?,
                    periodo = ?,
                    valor = ?,
                    fecha_vencimiento = ?,
                    fecha_pago = ?,
                    estado = ?,
                    observacion = ?
                WHERE id_mensualidad = ?
                  AND activo = TRUE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    obj.getIdEmpresa()
            );

            ps.setString(
                    2,
                    obj.getPeriodo()
            );

            ps.setBigDecimal(
                    3,
                    obj.getValor()
            );

            ps.setObject(
                    4,
                    obj.getFechaVencimiento()
            );

            ps.setObject(
                    5,
                    obj.getFechaPago()
            );

            ps.setString(
                    6,
                    obj.getEstado()
            );

            ps.setString(
                    7,
                    obj.getObservacion()
            );

            ps.setObject(
                    8,
                    obj.getIdMensualidad()
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    public boolean eliminarLogico(
            UUID idMensualidad
    ) throws SQLException {

        String sql = """
                UPDATE mensualidad_empresa
                SET activo = FALSE
                WHERE id_mensualidad = ?
                  AND activo = TRUE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idMensualidad
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // REACTIVAR REGISTRO
    // =========================================================

    public boolean reactivar(
            UUID idMensualidad
    ) throws SQLException {

        String sql = """
                UPDATE mensualidad_empresa
                SET activo = TRUE
                WHERE id_mensualidad = ?
                  AND activo = FALSE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idMensualidad
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // VERIFICAR SI EXISTE PERIODO ACTIVO PARA EMPRESA
    // =========================================================

    public boolean existePeriodo(
            UUID idEmpresa,
            String periodo
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM mensualidad_empresa
                WHERE id_empresa = ?
                  AND periodo = ?
                  AND activo = TRUE
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            ps.setString(
                    2,
                    periodo
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next();
            }
        }
    }

    // =========================================================
    // VERIFICAR EMPRESA
    // =========================================================

    public boolean existeEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM empresa
                WHERE id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next();
            }
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private MensualidadEmpresa mapear(
            ResultSet rs
    ) throws SQLException {

        MensualidadEmpresa obj =
                new MensualidadEmpresa();

        obj.setIdMensualidad(
                rs.getObject(
                        "id_mensualidad",
                        UUID.class
                )
        );

        obj.setIdEmpresa(
                rs.getObject(
                        "id_empresa",
                        UUID.class
                )
        );

        obj.setPeriodo(
                rs.getString(
                        "periodo"
                )
        );

        obj.setValor(
                rs.getBigDecimal(
                        "valor"
                )
        );

        Date fechaVencimiento =
                rs.getDate(
                        "fecha_vencimiento"
                );

        if (fechaVencimiento != null) {

            obj.setFechaVencimiento(
                    fechaVencimiento
                            .toLocalDate()
            );
        }

        Date fechaPago =
                rs.getDate(
                        "fecha_pago"
                );

        if (fechaPago != null) {

            obj.setFechaPago(
                    fechaPago
                            .toLocalDate()
            );
        }

        obj.setEstado(
                rs.getString(
                        "estado"
                )
        );

        obj.setObservacion(
                rs.getString(
                        "observacion"
                )
        );

        obj.setActivo(
                rs.getBoolean(
                        "activo"
                )
        );

        return obj;
    }
}