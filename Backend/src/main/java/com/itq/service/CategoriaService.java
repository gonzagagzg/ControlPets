package com.itq.service;

import com.itq.dao.CategoriaDAO;
import com.itq.model.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoriaService {

    private final CategoriaDAO dao =
            new CategoriaDAO();

    public List<Categoria> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Categoria> buscarPorId(
            Integer idCategoria,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idCategoria);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idCategoria,
                idEmpresa
        );
    }

    public Categoria crear(
            Categoria obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la categoría son obligatorios"
            );
        }

        if (!superUsuario) {
            validarEmpresaSesion(idEmpresa);

            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Categoria obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdCategoria() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la categoría son obligatorios"
            );
        }

        if (superUsuario) {
            validar(obj);
            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        if (dao.buscarPorIdYEmpresa(
                obj.getIdCategoria(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            Integer idCategoria,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idCategoria);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idCategoria,
                idEmpresa
        );
    }

    private void validar(Categoria obj) {

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getNombre() == null ||
                obj.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio"
            );
        }

        obj.setNombre(obj.getNombre().trim());
    }

    private void validarEmpresaSesion(UUID idEmpresa) {

        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }
}