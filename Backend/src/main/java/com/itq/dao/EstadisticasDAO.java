package com.itq.dao;

import com.itq.dto.EstadisticasPlataforma;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Consultas agregadas de solo lectura para el panel estadístico del
 * SuperUsuario. No recibe parámetros del usuario: los nombres de tabla y la
 * matriz se fijan aquí.
 */
public class EstadisticasDAO {

    /**
     * Empresa Matriz: hogar técnico del SuperUsuario. Se excluye de los
     * conteos porque no es una veterinaria del negocio.
     */
    private static final String ID_EMPRESA_MATRIZ =
            "00000000-0000-0000-0000-000000000000";

    private static final UUID UUID_MATRIZ =
            UUID.fromString(ID_EMPRESA_MATRIZ);

    // =========================================================
    // INDICADORES GLOBALES
    // =========================================================

    public EstadisticasPlataforma.Totales totales(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT
                    (SELECT COUNT(*)
                       FROM empresa
                      WHERE eliminado = FALSE
                        AND id_empresa <> ?) AS empresas,
                    (SELECT COUNT(*)
                       FROM empresa
                      WHERE eliminado = FALSE
                        AND activo = TRUE
                        AND id_empresa <> ?) AS empresas_activas,
                    (SELECT COUNT(*)
                       FROM usuario
                      WHERE id_empresa <> ?) AS usuarios,
                    (SELECT COUNT(*)
                       FROM usuario
                      WHERE id_empresa <> ?
                        AND estado_cuenta = 'pendiente') AS usuarios_pendientes,
                    (SELECT COUNT(*) FROM cliente) AS clientes,
                    (SELECT COUNT(*) FROM mascota) AS mascotas,
                    (SELECT COUNT(*) FROM cita) AS citas,
                    (SELECT COUNT(*)
                       FROM cita
                      WHERE UPPER(estado) = 'PENDIENTE') AS citas_pendientes,
                    (SELECT COUNT(*) FROM historial_clinico) AS historiales,
                    (SELECT COUNT(*) FROM producto) AS productos,
                    (SELECT COUNT(*)
                       FROM producto
                      WHERE stock_actual <= stock_minimo) AS productos_bajo_stock,
                    (SELECT COUNT(*)
                       FROM factura
                      WHERE UPPER(estado) = 'EMITIDA') AS facturas,
                    (SELECT COALESCE(SUM(total), 0)
                       FROM factura
                      WHERE UPPER(estado) = 'EMITIDA') AS total_facturado
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setObject(i, UUID_MATRIZ);
            }

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                EstadisticasPlataforma.Totales t =
                        new EstadisticasPlataforma.Totales();

                t.setEmpresas(rs.getLong("empresas"));
                t.setEmpresasActivas(rs.getLong("empresas_activas"));
                t.setEmpresasInactivas(
                        t.getEmpresas() - t.getEmpresasActivas()
                );
                t.setUsuarios(rs.getLong("usuarios"));
                t.setUsuariosPendientes(
                        rs.getLong("usuarios_pendientes")
                );
                t.setClientes(rs.getLong("clientes"));
                t.setMascotas(rs.getLong("mascotas"));
                t.setCitas(rs.getLong("citas"));
                t.setCitasPendientes(
                        rs.getLong("citas_pendientes")
                );
                t.setHistoriales(rs.getLong("historiales"));
                t.setProductos(rs.getLong("productos"));
                t.setProductosBajoStock(
                        rs.getLong("productos_bajo_stock")
                );
                t.setFacturas(rs.getLong("facturas"));

                BigDecimal facturado =
                        rs.getBigDecimal("total_facturado");

                t.setTotalFacturado(facturado);
                t.setTicketPromedio(ticketPromedio(
                        facturado,
                        t.getFacturas()
                ));

                return t;
            }
        }
    }

    private BigDecimal ticketPromedio(
            BigDecimal facturado,
            long facturas
    ) {

        if (facturas <= 0 ||
                facturado == null) {

            return BigDecimal.ZERO;
        }

        return facturado.divide(
                BigDecimal.valueOf(facturas),
                2,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // SERIE MENSUAL (ÚLTIMOS N MESES, INCLUYENDO EL ACTUAL)
    // =========================================================

    public List<EstadisticasPlataforma.PuntoSerie> serieMensual(
            Connection cn,
            int meses
    ) throws SQLException {

        String sql = """
                WITH meses AS (
                    SELECT (
                        date_trunc('month', CURRENT_DATE)
                        - make_interval(months => g)
                    )::date AS mes
                    FROM generate_series(0, CAST(? AS integer)) AS g
                )
                SELECT to_char(m.mes, 'YYYY-MM') AS periodo,
                       COALESCE(SUM(f.total), 0) AS facturado,
                       COUNT(DISTINCT f.id_factura) AS facturas,
                       COUNT(DISTINCT c.id_cita) AS citas,
                       COUNT(DISTINCT mi.id_movimiento) AS movimientos
                FROM meses m
                LEFT JOIN factura f
                       ON date_trunc('month', f.fecha)::date = m.mes
                      AND UPPER(f.estado) = 'EMITIDA'
                LEFT JOIN cita c
                       ON date_trunc('month', c.fecha_hora)::date = m.mes
                LEFT JOIN movimiento_inventario mi
                       ON date_trunc('month', mi.fecha)::date = m.mes
                GROUP BY m.mes
                ORDER BY m.mes
                """;

        List<EstadisticasPlataforma.PuntoSerie> lista =
                new ArrayList<>();

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(0, meses - 1));

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    EstadisticasPlataforma.PuntoSerie punto =
                            new EstadisticasPlataforma.PuntoSerie();

                    punto.setPeriodo(
                            rs.getString("periodo")
                    );
                    punto.setFacturado(
                            rs.getBigDecimal("facturado")
                    );
                    punto.setFacturas(
                            rs.getLong("facturas")
                    );
                    punto.setCitas(
                            rs.getLong("citas")
                    );
                    punto.setMovimientos(
                            rs.getLong("movimientos")
                    );

                    lista.add(punto);
                }
            }
        }

        return lista;
    }

    // =========================================================
    // DESGLOSES
    // =========================================================

    public List<EstadisticasPlataforma.Conteo> citasPorEstado(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT COALESCE(INITCAP(LOWER(estado)), 'Sin estado') AS etiqueta,
                       COUNT(*) AS valor
                FROM cita
                GROUP BY etiqueta
                ORDER BY valor DESC, etiqueta
                """;

        return conteos(cn, sql, null);
    }

    public List<EstadisticasPlataforma.Conteo> usuariosPorRol(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT r.nombre AS etiqueta,
                       COUNT(u.id_usuario) AS valor
                FROM rol r
                LEFT JOIN usuario u
                       ON u.id_rol = r.id_rol
                      AND u.id_empresa <> ?
                GROUP BY r.nombre
                HAVING COUNT(u.id_usuario) > 0
                ORDER BY valor DESC, r.nombre
                """;

        return conteos(cn, sql, UUID_MATRIZ);
    }

    public List<EstadisticasPlataforma.Conteo> mascotasPorEspecie(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT e.nombre AS etiqueta,
                       COUNT(*) AS valor
                FROM mascota m
                JOIN raza rz
                  ON rz.id_raza = m.id_raza
                JOIN especie e
                  ON e.id_especie = rz.id_especie
                GROUP BY e.nombre
                ORDER BY valor DESC, e.nombre
                """;

        return conteos(cn, sql, null);
    }

    public List<EstadisticasPlataforma.Conteo> empresasPorEstado(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT CASE
                           WHEN activo THEN 'Activas'
                           ELSE 'Inactivas'
                       END AS etiqueta,
                       COUNT(*) AS valor
                FROM empresa
                WHERE eliminado = FALSE
                  AND id_empresa <> ?
                GROUP BY activo
                ORDER BY activo DESC
                """;

        return conteos(cn, sql, UUID_MATRIZ);
    }

    // =========================================================
    // RANKING DE VETERINARIAS
    // =========================================================

    public List<EstadisticasPlataforma.EmpresaRanking> topEmpresas(
            Connection cn,
            int limite
    ) throws SQLException {

        String sql = """
                SELECT e.id_empresa,
                       e.razon_social,
                       e.activo,
                       COALESCE(SUM(f.total), 0) AS facturado,
                       COUNT(DISTINCT f.id_factura) AS facturas,
                       (SELECT COUNT(*)
                          FROM cliente c
                         WHERE c.id_empresa = e.id_empresa) AS clientes,
                       (SELECT COUNT(*)
                          FROM mascota m
                         WHERE m.id_empresa = e.id_empresa) AS mascotas
                FROM empresa e
                LEFT JOIN factura f
                       ON f.id_empresa = e.id_empresa
                      AND UPPER(f.estado) = 'EMITIDA'
                WHERE e.eliminado = FALSE
                  AND e.id_empresa <> ?
                GROUP BY e.id_empresa, e.razon_social, e.activo
                ORDER BY facturado DESC, clientes DESC, e.razon_social
                LIMIT ?
                """;

        List<EstadisticasPlataforma.EmpresaRanking> lista =
                new ArrayList<>();

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, UUID_MATRIZ);
            ps.setInt(2, limite);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    EstadisticasPlataforma.EmpresaRanking fila =
                            new EstadisticasPlataforma.EmpresaRanking();

                    fila.setIdEmpresa(
                            rs.getString("id_empresa")
                    );
                    fila.setRazonSocial(
                            rs.getString("razon_social")
                    );
                    fila.setActivo(
                            rs.getBoolean("activo")
                    );
                    fila.setFacturado(
                            rs.getBigDecimal("facturado")
                    );
                    fila.setFacturas(
                            rs.getLong("facturas")
                    );
                    fila.setClientes(
                            rs.getLong("clientes")
                    );
                    fila.setMascotas(
                            rs.getLong("mascotas")
                    );

                    lista.add(fila);
                }
            }
        }

        return lista;
    }

    // =========================================================
    // AUXILIAR
    // =========================================================

    private List<EstadisticasPlataforma.Conteo> conteos(
            Connection cn,
            String sql,
            Object parametro
    ) throws SQLException {

        List<EstadisticasPlataforma.Conteo> lista =
                new ArrayList<>();

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            if (parametro != null) {
                ps.setObject(1, parametro);
            }

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    lista.add(
                            new EstadisticasPlataforma.Conteo(
                                    rs.getString("etiqueta"),
                                    rs.getLong("valor")
                            )
                    );
                }
            }
        }

        return lista;
    }
}
