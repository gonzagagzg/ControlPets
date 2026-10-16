package com.itq.service;

import com.itq.dao.ClienteDAO;
import com.itq.dao.FacturaDAO;
import com.itq.dao.UsuarioDAO;
import com.itq.model.Factura;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaService {

    private final FacturaDAO dao =
            new FacturaDAO();

    private final ClienteDAO clienteDAO =
            new ClienteDAO();

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    public List<Factura> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Factura> buscarPorId(
            UUID idFactura,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idFactura);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idFactura,
                idEmpresa
        );
    }

    public Factura crear(
            Factura obj,
            UUID idEmpresa,
            UUID idUsuario,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la factura son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * La veterinaria y el usuario emisor
             * vienen del JWT.
             */
            obj.setIdEmpresa(idEmpresa);
            obj.setIdUsuario(idUsuario);
        }

        if (obj.getFecha() == null) {
            obj.setFecha(
                    OffsetDateTime.now()
            );
        }

        if (vacio(obj.getEstado())) {
            obj.setEstado("EMITIDA");
        }

        validar(obj);

        validarClienteDeEmpresa(
                obj.getIdCliente(),
                obj.getIdEmpresa()
        );

        validarUsuarioDeEmpresa(
                obj.getIdUsuario(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Factura obj,
            UUID idEmpresa,
            UUID idUsuario,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdFactura() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la factura son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarClienteDeEmpresa(
                    obj.getIdCliente(),
                    obj.getIdEmpresa()
            );

            validarUsuarioDeEmpresa(
                    obj.getIdUsuario(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        if (dao.buscarPorIdYEmpresa(
                obj.getIdFactura(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        /*
         * No permitimos cambiar ni empresa
         * ni usuario emisor desde frontend.
         */
        obj.setIdEmpresa(idEmpresa);
        obj.setIdUsuario(idUsuario);

        validar(obj);

        validarClienteDeEmpresa(
                obj.getIdCliente(),
                idEmpresa
        );

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idFactura,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idFactura);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idFactura,
                idEmpresa
        );
    }

    private void validarClienteDeEmpresa(
            UUID idCliente,
            UUID idEmpresa
    ) throws SQLException {

        if (clienteDAO.buscarPorIdYEmpresa(
                idCliente,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "El cliente no pertenece a la empresa indicada"
            );
        }
    }

    private void validarUsuarioDeEmpresa(
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        if (usuarioDAO.buscarPorIdYEmpresa(
                idUsuario,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "El usuario no pertenece a la empresa indicada"
            );
        }
    }

    private void validar(Factura obj) {

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdCliente() == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (obj.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (obj.getTotal() == null ||
                obj.getTotal()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El total no puede ser negativo"
            );
        }

        if (vacio(obj.getEstado())) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio"
            );
        }

        if (obj.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria"
            );
        }

        obj.setEstado(
                obj.getEstado()
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