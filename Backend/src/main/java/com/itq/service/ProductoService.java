package com.itq.service;

import com.itq.dao.CategoriaDAO;
import com.itq.dao.ProductoDAO;
import com.itq.model.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductoService {

    private final ProductoDAO dao =
            new ProductoDAO();

    private final CategoriaDAO categoriaDAO =
            new CategoriaDAO();

    public List<Producto> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Producto> buscarPorId(
            UUID idProducto,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idProducto);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idProducto,
                idEmpresa
        );
    }

    public Producto crear(
            Producto obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Para usuarios normales ignoramos
             * cualquier idEmpresa enviado desde frontend.
             *
             * La empresa válida siempre sale del JWT.
             */
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        // La caducidad de un producto nuevo debe ser posterior a hoy (no se
        // permite hoy ni días pasados). Al actualizar no se aplica, para no
        // bloquear registros antiguos cuya fecha ya pasó.
        if (obj.getFechaCaducidad() != null &&
                !obj.getFechaCaducidad().isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de caducidad debe ser posterior a hoy"
            );
        }

        /*
         * La categoría utilizada por el producto
         * debe pertenecer a la misma veterinaria.
         */
        validarCategoriaDeEmpresa(
                obj.getIdCategoria(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Producto obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdProducto() == null) {

            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        /*
         * El SuperUsuario puede trabajar globalmente,
         * pero igualmente verificamos coherencia entre
         * producto, empresa y categoría.
         */
        if (superUsuario) {

            validar(obj);

            validarCategoriaDeEmpresa(
                    obj.getIdCategoria(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * Primero comprobamos que el producto que se
         * intenta modificar pertenece a la empresa
         * autenticada.
         */
        if (dao.buscarPorIdYEmpresa(
                obj.getIdProducto(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        /*
         * Ignoramos cualquier cambio de empresa enviado
         * desde frontend.
         */
        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        /*
         * También impedimos usar una categoría
         * perteneciente a otra veterinaria.
         */
        validarCategoriaDeEmpresa(
                obj.getIdCategoria(),
                idEmpresa
        );

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idProducto,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (idProducto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        if (superUsuario) {
            return dao.eliminar(idProducto);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idProducto,
                idEmpresa
        );
    }

    // =========================================================
    // VALIDACIONES DEL PRODUCTO
    // =========================================================

    private void validar(Producto obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdCategoria() <= 0) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria"
            );
        }

        if (obj.getIdIva() <= 0) {
            throw new IllegalArgumentException(
                    "El IVA es obligatorio"
            );
        }

        if (vacio(obj.getNombre())) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (obj.getPrecioUnitario() == null ||
                obj.getPrecioUnitario()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo"
            );
        }

        if (obj.getStockActual() < 0) {
            throw new IllegalArgumentException(
                    "El stock actual no puede ser negativo"
            );
        }

        if (obj.getStockMinimo() < 0) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo"
            );
        }

        obj.setNombre(
                obj.getNombre().trim()
        );
    }

    // =========================================================
    // VALIDACIÓN MULTIEMPRESA DE LA CATEGORÍA
    // =========================================================

    private void validarCategoriaDeEmpresa(
            Integer idCategoria,
            UUID idEmpresa
    ) throws SQLException {

        if (idCategoria == null ||
                idCategoria <= 0) {

            throw new IllegalArgumentException(
                    "La categoría es obligatoria"
            );
        }

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (categoriaDAO.buscarPorIdYEmpresa(
                idCategoria,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "La categoría no pertenece a la empresa indicada"
            );
        }
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