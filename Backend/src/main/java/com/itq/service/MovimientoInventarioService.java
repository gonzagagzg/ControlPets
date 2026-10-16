package com.itq.service;

import com.itq.dao.FacturaDAO;
import com.itq.dao.MovimientoInventarioDAO;
import com.itq.dao.ProductoDAO;
import com.itq.model.MovimientoInventario;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MovimientoInventarioService {

    private final MovimientoInventarioDAO dao =
            new MovimientoInventarioDAO();

    private final ProductoDAO productoDAO =
            new ProductoDAO();

    private final FacturaDAO facturaDAO =
            new FacturaDAO();

    public List<MovimientoInventario> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<MovimientoInventario> buscarPorId(
            UUID idMovimiento,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idMovimiento);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idMovimiento,
                idEmpresa
        );
    }

    public MovimientoInventario crear(
            MovimientoInventario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del movimiento son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Para un usuario normal la empresa
             * siempre sale del JWT.
             */
            obj.setIdEmpresa(idEmpresa);
        }

        /*
         * Si no envían fecha, utilizamos la fecha
         * actual. La tabla también tiene DEFAULT,
         * pero como nuestro INSERT siempre incluye
         * la columna, la definimos aquí.
         */
        if (obj.getFecha() == null) {
            obj.setFecha(
                    OffsetDateTime.now()
            );
        }

        validar(obj);

        validarProductoDeEmpresa(
                obj.getIdProducto(),
                obj.getIdEmpresa()
        );

        validarFacturaDeEmpresa(
                obj.getIdFactura(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            MovimientoInventario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdMovimiento() == null) {

            throw new IllegalArgumentException(
                    "Los datos del movimiento son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarProductoDeEmpresa(
                    obj.getIdProducto(),
                    obj.getIdEmpresa()
            );

            validarFacturaDeEmpresa(
                    obj.getIdFactura(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        if (dao.buscarPorIdYEmpresa(
                obj.getIdMovimiento(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        validarProductoDeEmpresa(
                obj.getIdProducto(),
                idEmpresa
        );

        validarFacturaDeEmpresa(
                obj.getIdFactura(),
                idEmpresa
        );

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idMovimiento,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idMovimiento);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idMovimiento,
                idEmpresa
        );
    }

    private void validarProductoDeEmpresa(
            UUID idProducto,
            UUID idEmpresa
    ) throws SQLException {

        if (idProducto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        if (productoDAO.buscarPorIdYEmpresa(
                idProducto,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "El producto no pertenece a la empresa indicada"
            );
        }
    }

    private void validarFacturaDeEmpresa(
            UUID idFactura,
            UUID idEmpresa
    ) throws SQLException {

        /*
         * idFactura es opcional.
         * Un movimiento manual o un ingreso
         * de proveedor puede no tener factura.
         */
        if (idFactura == null) {
            return;
        }

        if (facturaDAO.buscarPorIdYEmpresa(
                idFactura,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "La factura no pertenece a la empresa indicada"
            );
        }
    }

    private void validar(
            MovimientoInventario obj
    ) {

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdProducto() == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        if (vacio(obj.getTipo())) {
            throw new IllegalArgumentException(
                    "El tipo de movimiento es obligatorio"
            );
        }

        if (obj.getCantidad() == null ||
                obj.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        if (obj.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria"
            );
        }

        obj.setTipo(
                obj.getTipo()
                        .trim()
                        .toUpperCase()
        );
    }

    private void validarEmpresaSesion(
            UUID idEmpresa
    ) {

        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    private boolean vacio(String valor) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}