package com.itq.service;

import com.itq.dao.HistorialClinicoDAO;
import com.itq.dao.RecetaDAO;
import com.itq.model.Receta;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaService {

    private final RecetaDAO dao =
            new RecetaDAO();

    private final HistorialClinicoDAO historialDAO =
            new HistorialClinicoDAO();

    public List<Receta> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Receta> buscarPorId(
            UUID idReceta,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idReceta);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(idReceta, idEmpresa);
    }

    public Receta crear(
            Receta obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la receta son obligatorios"
            );
        }

        if (!superUsuario) {
            validarEmpresaSesion(idEmpresa);
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        validarHistorialDeEmpresa(
                obj.getIdHistorial(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Receta obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null || obj.getIdReceta() == null) {
            throw new IllegalArgumentException(
                    "Los datos de la receta son obligatorios"
            );
        }

        if (superUsuario) {
            validar(obj);

            validarHistorialDeEmpresa(
                    obj.getIdHistorial(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        if (dao.buscarPorIdYEmpresa(obj.getIdReceta(), idEmpresa).isEmpty()) {
            return false;
        }

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        validarHistorialDeEmpresa(obj.getIdHistorial(), idEmpresa);

        return dao.actualizarPorEmpresa(obj, idEmpresa);
    }

    public boolean eliminar(
            UUID idReceta,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idReceta);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(idReceta, idEmpresa);
    }

    private void validarHistorialDeEmpresa(
            UUID idHistorial,
            UUID idEmpresa
    ) throws SQLException {

        if (idHistorial == null) {
            throw new IllegalArgumentException(
                    "El historial clínico es obligatorio"
            );
        }

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (historialDAO
                .buscarPorIdYEmpresa(idHistorial, idEmpresa)
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "El historial clínico no pertenece a la empresa indicada"
            );
        }
    }

    private void validar(Receta obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la receta son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdHistorial() == null) throw new IllegalArgumentException("El historial clínico es obligatorio");
        // Las indicaciones son opcionales: la receta se emite primero (vacía) y
        // se completa desde la consulta al agregar líneas de medicamento.
        if (!vacio(obj.getIndicacionesGenerales())) {
            obj.setIndicacionesGenerales(obj.getIndicacionesGenerales().trim());
        }
    }

    private void validarEmpresaSesion(UUID idEmpresa) {
        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
