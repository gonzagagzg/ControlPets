package com.itq.service;

import com.itq.config.ConexionBD;
import com.itq.dto.FacturaEmitirRequest;
import com.itq.model.Factura;
import com.itq.model.FacturaDetalle;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public class FacturacionTransaccionalService {

    public Factura emitir(
            FacturaEmitirRequest request,
            UUID idEmpresa,
            UUID idUsuario,
            boolean superUsuario
    ) throws SQLException {

        // =====================================================
        // VALIDACIONES INICIALES
        // =====================================================

        if (superUsuario) {
            throw new IllegalArgumentException(
                    "El SuperUsuario no puede emitir una factura global. Debe operar dentro de una veterinaria."
            );
        }

        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }

        if (idUsuario == null) {
            throw new SecurityException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la factura son obligatorios"
            );
        }

        if (request.getIdCliente() == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (request.getDetalles() == null ||
                request.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "La factura debe contener al menos un producto"
            );
        }

        // =====================================================
        // UNA SOLA CONEXIÓN PARA TODA LA OPERACIÓN
        // =====================================================

        try (Connection cn =
                     ConexionBD.obtenerConexion()) {

            cn.setAutoCommit(false);

            try {

                // ---------------------------------------------
                // 1. Validar cliente
                // ---------------------------------------------

                validarCliente(
                        cn,
                        request.getIdCliente(),
                        idEmpresa
                );

                // ---------------------------------------------
                // 2. Validar usuario emisor
                // ---------------------------------------------

                validarUsuario(
                        cn,
                        idUsuario,
                        idEmpresa
                );

                // ---------------------------------------------
                // 3. Preparar productos y calcular total
                // ---------------------------------------------

                BigDecimal total =
                        BigDecimal.ZERO;

                for (FacturaDetalle detalle :
                        request.getDetalles()) {

                    prepararDetalle(
                            cn,
                            detalle,
                            idEmpresa
                    );

                    total = total.add(
                            detalle.getSubtotal()
                    );
                }

                // ---------------------------------------------
                // 4. Crear cabecera de factura
                // ---------------------------------------------

                UUID idFactura =
                        insertarFactura(
                                cn,
                                idEmpresa,
                                request.getIdCliente(),
                                idUsuario,
                                total
                        );

                // ---------------------------------------------
                // 5. Detalles + stock + movimiento
                // ---------------------------------------------

                for (FacturaDetalle detalle :
                        request.getDetalles()) {

                    detalle.setIdFactura(
                            idFactura
                    );

                    insertarDetalle(
                            cn,
                            detalle
                    );

                    descontarStock(
                            cn,
                            detalle.getIdProducto(),
                            idEmpresa,
                            detalle.getCantidad()
                    );

                    insertarMovimiento(
                            cn,
                            idEmpresa,
                            detalle.getIdProducto(),
                            idFactura,
                            detalle.getCantidad()
                    );
                }

                // =============================================
                // TODO SALIÓ BIEN
                // =============================================

                cn.commit();

                Factura factura =
                        new Factura();

                factura.setIdFactura(idFactura);
                factura.setIdEmpresa(idEmpresa);
                factura.setIdCliente(
                        request.getIdCliente()
                );
                factura.setIdUsuario(idUsuario);
                factura.setTotal(total);
                factura.setEstado("EMITIDA");
                factura.setFecha(
                        OffsetDateTime.now()
                );

                return factura;

            } catch (Exception e) {

                // =============================================
                // ALGO FALLÓ → DESHACEMOS TODO
                // =============================================

                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(
                            rollbackError
                    );
                }

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "No se pudo emitir la factura",
                        e
                );

            } finally {

                try {
                    cn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================
    // VALIDAR CLIENTE
    // =========================================================

    private void validarCliente(
            Connection cn,
            UUID idCliente,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM cliente
                WHERE id_cliente = ?
                  AND id_empresa = ?
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idCliente);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El cliente no pertenece a la empresa autenticada"
                    );
                }
            }
        }
    }

    // =========================================================
    // VALIDAR USUARIO EMISOR
    // =========================================================

    private void validarUsuario(
            Connection cn,
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM usuario
                WHERE id_usuario = ?
                  AND id_empresa = ?
                  AND activo = TRUE
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idUsuario);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SecurityException(
                            "El usuario emisor no pertenece a la empresa autenticada"
                    );
                }
            }
        }
    }

    // =========================================================
    // PREPARAR DETALLE
    // =========================================================

    private void prepararDetalle(
            Connection cn,
            FacturaDetalle detalle,
            UUID idEmpresa
    ) throws SQLException {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "Existe una línea de factura inválida"
            );
        }

        if (detalle.getIdProducto() == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        if (detalle.getCantidad() == null ||
                detalle.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        /*
         * FOR UPDATE bloquea la fila durante la transacción,
         * evitando que dos ventas consuman simultáneamente
         * el mismo stock.
         */
        String sql = """
                SELECT precio_unitario,
                       id_iva,
                       stock_actual
                FROM producto
                WHERE id_producto = ?
                  AND id_empresa = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    detalle.getIdProducto()
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El producto no pertenece a la empresa autenticada"
                    );
                }

                int stockActual =
                        rs.getInt(
                                "stock_actual"
                        );

                if (stockActual <
                        detalle.getCantidad()) {

                    throw new IllegalArgumentException(
                            "Stock insuficiente para el producto "
                                    + detalle.getIdProducto()
                    );
                }

                BigDecimal precio =
                        rs.getBigDecimal(
                                "precio_unitario"
                        );

                int idIva =
                        rs.getInt(
                                "id_iva"
                        );

                /*
                 * No confiamos en precios,
                 * IVA o subtotal enviados por frontend.
                 */
                detalle.setPrecioUnitario(
                        precio
                );

                detalle.setIdIva(
                        idIva
                );

                detalle.setSubtotal(
                        precio.multiply(
                                BigDecimal.valueOf(
                                        detalle.getCantidad()
                                )
                        )
                );
            }
        }
    }

    // =========================================================
    // INSERTAR CABECERA
    // =========================================================

    private UUID insertarFactura(
            Connection cn,
            UUID idEmpresa,
            UUID idCliente,
            UUID idUsuario,
            BigDecimal total
    ) throws SQLException {

        String sql = """
                INSERT INTO factura
                (
                    id_empresa,
                    id_cliente,
                    id_usuario,
                    total,
                    estado,
                    fecha
                )
                VALUES (?, ?, ?, ?, 'EMITIDA', CURRENT_TIMESTAMP)
                RETURNING id_factura
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idEmpresa);
            ps.setObject(2, idCliente);
            ps.setObject(3, idUsuario);
            ps.setBigDecimal(4, total);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo generar la factura"
                    );
                }

                return rs.getObject(
                        "id_factura",
                        UUID.class
                );
            }
        }
    }

    // =========================================================
    // INSERTAR DETALLE
    // =========================================================

    private void insertarDetalle(
            Connection cn,
            FacturaDetalle detalle
    ) throws SQLException {

        String sql = """
                INSERT INTO factura_detalle
                (
                    id_factura,
                    id_producto,
                    id_iva,
                    cantidad,
                    precio_unitario,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_detalle
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    detalle.getIdFactura()
            );

            ps.setObject(
                    2,
                    detalle.getIdProducto()
            );

            ps.setInt(
                    3,
                    detalle.getIdIva()
            );

            ps.setInt(
                    4,
                    detalle.getCantidad()
            );

            ps.setBigDecimal(
                    5,
                    detalle.getPrecioUnitario()
            );

            ps.setBigDecimal(
                    6,
                    detalle.getSubtotal()
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo guardar el detalle de factura"
                    );
                }

                detalle.setIdDetalle(
                        rs.getObject(
                                "id_detalle",
                                UUID.class
                        )
                );
            }
        }
    }

    // =========================================================
    // DESCONTAR STOCK
    // =========================================================

    private void descontarStock(
            Connection cn,
            UUID idProducto,
            UUID idEmpresa,
            int cantidad
    ) throws SQLException {

        String sql = """
                UPDATE producto
                SET stock_actual =
                    stock_actual - ?
                WHERE id_producto = ?
                  AND id_empresa = ?
                  AND stock_actual >= ?
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setObject(2, idProducto);
            ps.setObject(3, idEmpresa);
            ps.setInt(4, cantidad);

            int filas =
                    ps.executeUpdate();

            if (filas != 1) {
                throw new SQLException(
                        "No fue posible descontar el stock del producto "
                                + idProducto
                );
            }
        }
    }

    // =========================================================
    // MOVIMIENTO DE INVENTARIO
    // =========================================================

    private void insertarMovimiento(
            Connection cn,
            UUID idEmpresa,
            UUID idProducto,
            UUID idFactura,
            int cantidad
    ) throws SQLException {

        String sql = """
                INSERT INTO movimiento_inventario
                (
                    id_empresa,
                    id_producto,
                    id_factura,
                    tipo,
                    cantidad,
                    fecha
                )
                VALUES (?, ?, ?, 'VENTA', ?, CURRENT_TIMESTAMP)
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(1, idEmpresa);
            ps.setObject(2, idProducto);
            ps.setObject(3, idFactura);
            ps.setInt(4, cantidad);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo registrar el movimiento de inventario"
                );
            }
        }
    }
}