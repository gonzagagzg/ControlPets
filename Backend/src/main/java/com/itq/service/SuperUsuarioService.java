package com.itq.service;

import com.itq.config.ConexionBD;
import com.itq.dto.ResumenEmpresa;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SuperUsuarioService {

    public Optional<ResumenEmpresa> obtenerResumen(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        try (Connection cn =
                     ConexionBD.obtenerConexion()) {

            ResumenEmpresa resumen =
                    cargarEmpresa(
                            cn,
                            idEmpresa
                    );

            if (resumen == null) {
                return Optional.empty();
            }

            resumen.setTotalUsuarios(
                    contar(
                            cn,
                            "usuario",
                            idEmpresa
                    )
            );

            resumen.setTotalVeterinarios(
                    contar(
                            cn,
                            "veterinario",
                            idEmpresa
                    )
            );

            resumen.setTotalClientes(
                    contar(
                            cn,
                            "cliente",
                            idEmpresa
                    )
            );

            resumen.setTotalMascotas(
                    contar(
                            cn,
                            "mascota",
                            idEmpresa
                    )
            );

            resumen.setTotalCitas(
                    contar(
                            cn,
                            "cita",
                            idEmpresa
                    )
            );

            resumen.setTotalHistoriales(
                    contar(
                            cn,
                            "historial_clinico",
                            idEmpresa
                    )
            );

            resumen.setTotalProductos(
                    contar(
                            cn,
                            "producto",
                            idEmpresa
                    )
            );

            resumen.setTotalRecetas(
                    contar(
                            cn,
                            "receta",
                            idEmpresa
                    )
            );

            resumen.setTotalFacturas(
                    contar(
                            cn,
                            "factura",
                            idEmpresa
                    )
            );

            resumen.setTotalMovimientosInventario(
                    contar(
                            cn,
                            "movimiento_inventario",
                            idEmpresa
                    )
            );

            resumen.setTotalFacturado(
                    totalFacturado(
                            cn,
                            idEmpresa
                    )
            );

            resumen.setProductosBajoStock(
                    contarProductosBajoStock(
                            cn,
                            idEmpresa
                    )
            );

            return Optional.of(resumen);
        }
    }

    private ResumenEmpresa cargarEmpresa(
            Connection cn,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_empresa,
                       ruc,
                       razon_social,
                       direccion,
                       activo
                FROM empresa
                WHERE id_empresa = ?
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idEmpresa);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                ResumenEmpresa resumen =
                        new ResumenEmpresa();

                resumen.setIdEmpresa(
                        rs.getObject(
                                "id_empresa",
                                UUID.class
                        )
                );

                resumen.setRuc(
                        rs.getString("ruc")
                );

                resumen.setRazonSocial(
                        rs.getString(
                                "razon_social"
                        )
                );

                resumen.setDireccion(
                        rs.getString(
                                "direccion"
                        )
                );

                resumen.setActivo(
                        rs.getBoolean("activo")
                );

                return resumen;
            }
        }
    }

    private long contar(
            Connection cn,
            String tabla,
            UUID idEmpresa
    ) throws SQLException {

        /*
         * El nombre de tabla NO viene del usuario.
         * Solo se llama internamente con valores
         * definidos en este servicio.
         */
        String sql =
                "SELECT COUNT(*) AS total " +
                        "FROM " + tabla +
                        " WHERE id_empresa = ?";

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idEmpresa);

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                return rs.getLong("total");
            }
        }
    }

    private BigDecimal totalFacturado(
            Connection cn,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT COALESCE(
                           SUM(total),
                           0
                       ) AS total
                FROM factura
                WHERE id_empresa = ?
                  AND UPPER(estado) = 'EMITIDA'
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                return rs.getBigDecimal(
                        "total"
                );
            }
        }
    }

    private long contarProductosBajoStock(
            Connection cn,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*) AS total
                FROM producto
                WHERE id_empresa = ?
                  AND stock_actual <= stock_minimo
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                return rs.getLong(
                        "total"
                );
            }
        }
    }
}