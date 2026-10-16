package com.itq.service;

import com.itq.dao.UsuarioDAO;
import com.itq.dao.VeterinarioDAO;
import com.itq.model.Usuario;
import com.itq.model.Veterinario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioService {

    private final VeterinarioDAO dao =
            new VeterinarioDAO();

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    private static final int ROL_VETERINARIO = 3;

    public List<Veterinario> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Veterinario> buscarPorId(
            UUID idVeterinario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idVeterinario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idVeterinario,
                idEmpresa
        );
    }

    public Veterinario crear(
            Veterinario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Un usuario local no puede decidir
             * la empresa del veterinario.
             */
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        /*
         * El usuario asociado debe:
         * 1. existir,
         * 2. pertenecer a la misma empresa,
         * 3. tener rol Veterinario.
         */
        validarUsuarioVeterinarioDeEmpresa(
                obj.getIdUsuario(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Veterinario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdVeterinario() == null) {

            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarUsuarioVeterinarioDeEmpresa(
                    obj.getIdUsuario(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * Comprobamos primero que el veterinario
         * pertenezca realmente a la empresa del JWT.
         */
        if (dao.buscarPorIdYEmpresa(
                obj.getIdVeterinario(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        /*
         * El usuario seleccionado debe pertenecer
         * a la misma empresa y tener rol Veterinario.
         */
        validarUsuarioVeterinarioDeEmpresa(
                obj.getIdUsuario(),
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
            UUID idVeterinario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idVeterinario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idVeterinario,
                idEmpresa
        );
    }

    private void validarUsuarioVeterinarioDeEmpresa(
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        Optional<Usuario> usuarioOpt =
                usuarioDAO.buscarPorIdYEmpresa(
                        idUsuario,
                        idEmpresa
                );

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no pertenece a la empresa indicada"
            );
        }

        Usuario usuario =
                usuarioOpt.get();

        if (usuario.getIdRol() == null ||
                usuario.getIdRol() != ROL_VETERINARIO) {

            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene rol Veterinario"
            );
        }
    }

    private void validar(Veterinario obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (obj.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (vacio(obj.getCodigoVeterinario())) {
            throw new IllegalArgumentException(
                    "El código del veterinario es obligatorio"
            );
        }

        if (vacio(obj.getEspecialidad())) {
            throw new IllegalArgumentException(
                    "La especialidad es obligatoria"
            );
        }

        obj.setCodigoVeterinario(
                obj.getCodigoVeterinario()
                        .trim()
                        .toUpperCase()
        );

        obj.setEspecialidad(
                obj.getEspecialidad()
                        .trim()
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