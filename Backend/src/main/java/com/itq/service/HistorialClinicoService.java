package com.itq.service;

import com.itq.dao.CitaDAO;
import com.itq.dao.HistorialClinicoDAO;
import com.itq.model.HistorialClinico;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HistorialClinicoService {

    private final HistorialClinicoDAO dao =
            new HistorialClinicoDAO();

    private final CitaDAO citaDAO =
            new CitaDAO();

    public List<HistorialClinico> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<HistorialClinico> buscarPorId(
            UUID idHistorial,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idHistorial);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idHistorial,
                idEmpresa
        );
    }

    public HistorialClinico crear(
            HistorialClinico obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del historial clínico son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Nunca confiamos en idEmpresa enviado
             * por frontend.
             */
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        validarCitaDeEmpresa(
                obj.getIdCita(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            HistorialClinico obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdHistorial() == null) {

            throw new IllegalArgumentException(
                    "Los datos del historial clínico son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarCitaDeEmpresa(
                    obj.getIdCita(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * El historial que se intenta modificar
         * debe pertenecer a la empresa autenticada.
         */
        if (dao.buscarPorIdYEmpresa(
                obj.getIdHistorial(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        /*
         * Y la cita asociada también debe ser
         * de la misma veterinaria.
         */
        validarCitaDeEmpresa(
                obj.getIdCita(),
                idEmpresa
        );

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idHistorial,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idHistorial);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idHistorial,
                idEmpresa
        );
    }

    private void validarCitaDeEmpresa(
            UUID idCita,
            UUID idEmpresa
    ) throws SQLException {

        if (idCita == null) {
            throw new IllegalArgumentException(
                    "La cita es obligatoria"
            );
        }

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (citaDAO.buscarPorIdYEmpresa(
                idCita,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "La cita no pertenece a la empresa indicada"
            );
        }
    }

    private void validar(HistorialClinico obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del historial clínico son obligatorios"
            );
        }

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdCita() == null) {
            throw new IllegalArgumentException(
                    "La cita es obligatoria"
            );
        }

        if (obj.getPesoKg() != null &&
                obj.getPesoKg()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El peso debe ser mayor que cero"
            );
        }

        if (obj.getTemperaturaC() != null &&
                obj.getTemperaturaC()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "La temperatura debe ser mayor que cero"
            );
        }

        if (vacio(obj.getAnamnesis())) {
            throw new IllegalArgumentException(
                    "La anamnesis es obligatoria"
            );
        }

        if (vacio(obj.getDiagnostico())) {
            throw new IllegalArgumentException(
                    "El diagnóstico es obligatorio"
            );
        }

        obj.setAnamnesis(
                obj.getAnamnesis().trim()
        );

        obj.setDiagnostico(
                obj.getDiagnostico().trim()
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