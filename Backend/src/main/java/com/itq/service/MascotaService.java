package com.itq.service;

import com.itq.dao.ClienteDAO;
import com.itq.dao.MascotaDAO;
import com.itq.model.Mascota;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaService {

    private final MascotaDAO dao =
            new MascotaDAO();

    private final ClienteDAO clienteDAO =
            new ClienteDAO();

    public List<Mascota> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Mascota> buscarPorId(
            UUID idMascota,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idMascota);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idMascota,
                idEmpresa
        );
    }

    public Mascota crear(
            Mascota obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la mascota son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Ignoramos completamente cualquier
             * idEmpresa recibido desde frontend.
             */
            obj.setIdEmpresa(idEmpresa);

            validarClienteDeEmpresa(
                    obj.getIdCliente(),
                    idEmpresa
            );
        }

        validar(obj);

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Mascota obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdMascota() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la mascota son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * Aseguramos que la mascota exista
         * dentro de la empresa autenticada.
         */
        if (dao.buscarPorIdYEmpresa(
                obj.getIdMascota(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        /*
         * El cliente nuevo también debe
         * pertenecer a la misma empresa.
         */
        validarClienteDeEmpresa(
                obj.getIdCliente(),
                idEmpresa
        );

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idMascota,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idMascota);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idMascota,
                idEmpresa
        );
    }

    private void validarClienteDeEmpresa(
            UUID idCliente,
            UUID idEmpresa
    ) throws SQLException {

        if (idCliente == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        boolean existe =
                clienteDAO.buscarPorIdYEmpresa(
                        idCliente,
                        idEmpresa
                ).isPresent();

        if (!existe) {
            throw new IllegalArgumentException(
                    "El cliente no pertenece a la empresa autenticada"
            );
        }
    }

    private void validar(Mascota obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la mascota son obligatorios"
            );
        }

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

        if (obj.getIdRaza() == null ||
                obj.getIdRaza() <= 0) {

            throw new IllegalArgumentException(
                    "La raza es obligatoria"
            );
        }

        if (vacio(obj.getNombre())) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (obj.getFechaNacimiento() != null &&
                obj.getFechaNacimiento()
                        .isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura"
            );
        }

        obj.setNombre(
                obj.getNombre().trim()
        );
    }

    private void validarEmpresaSesion(UUID idEmpresa) {

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