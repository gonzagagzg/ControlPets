package com.itq.service;

import com.itq.dao.MensualidadEmpresaDAO;
import com.itq.model.MensualidadEmpresa;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MensualidadEmpresaService {

    private final MensualidadEmpresaDAO dao =
            new MensualidadEmpresaDAO();

    // =========================================================
    // LISTAR REGISTROS ACTIVOS
    // =========================================================

    public List<MensualidadEmpresa> listar()
            throws SQLException {

        return dao.listar();
    }

    // =========================================================
    // LISTAR POR EMPRESA
    // =========================================================

    public List<MensualidadEmpresa> listarPorEmpresa(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (!dao.existeEmpresa(idEmpresa)) {

            throw new IllegalArgumentException(
                    "La empresa indicada no existe"
            );
        }

        return dao.listarPorEmpresa(
                idEmpresa
        );
    }

    // =========================================================
// BUSCAR POR ID DENTRO DE UNA EMPRESA
// =========================================================

public Optional<MensualidadEmpresa> buscarPorIdYEmpresa(
        UUID idMensualidad,
        UUID idEmpresa
) throws SQLException {

    if (idMensualidad == null) {
        throw new IllegalArgumentException(
                "La mensualidad es obligatoria"
        );
    }

    if (idEmpresa == null) {
        throw new IllegalArgumentException(
                "La empresa es obligatoria"
        );
    }

    Optional<MensualidadEmpresa> encontrada =
            dao.buscarPorId(
                    idMensualidad
            );

    if (encontrada.isEmpty()) {
        return Optional.empty();
    }

    if (!idEmpresa.equals(
            encontrada.get().getIdEmpresa()
    )) {
        return Optional.empty();
    }

    return encontrada;
}

    // =========================================================
    // BUSCAR POR ID
    // Solo devuelve registros activos
    // =========================================================

    public Optional<MensualidadEmpresa> buscarPorId(
            UUID idMensualidad
    ) throws SQLException {

        if (idMensualidad == null) {

            throw new IllegalArgumentException(
                    "La mensualidad es obligatoria"
            );
        }

        return dao.buscarPorId(
                idMensualidad
        );
    }

    // =========================================================
    // BUSCAR INCLUYENDO INACTIVOS
    // Uso administrativo / auditoría
    // =========================================================

    public Optional<MensualidadEmpresa>
    buscarPorIdIncluyendoInactivos(
            UUID idMensualidad
    ) throws SQLException {

        if (idMensualidad == null) {

            throw new IllegalArgumentException(
                    "La mensualidad es obligatoria"
            );
        }

        return dao.buscarPorIdIncluyendoInactivos(
                idMensualidad
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public MensualidadEmpresa crear(
            MensualidadEmpresa obj
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos de la mensualidad son obligatorios"
            );
        }

        validar(obj);

        // -----------------------------------------------------
        // VERIFICAR EMPRESA
        // -----------------------------------------------------

        if (!dao.existeEmpresa(
                obj.getIdEmpresa()
        )) {

            throw new IllegalArgumentException(
                    "La empresa indicada no existe"
            );
        }

        // -----------------------------------------------------
        // EVITAR DOS MENSUALIDADES ACTIVAS
        // DEL MISMO PERIODO PARA LA MISMA EMPRESA
        // -----------------------------------------------------

        if (dao.existePeriodo(
                obj.getIdEmpresa(),
                obj.getPeriodo()
        )) {

            throw new IllegalArgumentException(
                    "Ya existe una mensualidad activa para esta empresa y periodo"
            );
        }

        /*
         * El estado activo no lo decide el frontend.
         * Todo registro nuevo nace activo.
         */
        obj.setActivo(true);

        return dao.insertar(
                obj
        );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            MensualidadEmpresa obj
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos de la mensualidad son obligatorios"
            );
        }

        if (obj.getIdMensualidad() == null) {

            throw new IllegalArgumentException(
                    "La mensualidad es obligatoria"
            );
        }

        // -----------------------------------------------------
        // SOLO SE PUEDEN EDITAR REGISTROS ACTIVOS
        // -----------------------------------------------------

        Optional<MensualidadEmpresa> existente =
                dao.buscarPorId(
                        obj.getIdMensualidad()
                );

        if (existente.isEmpty()) {

            return false;
        }

        validar(obj);

        if (!dao.existeEmpresa(
                obj.getIdEmpresa()
        )) {

            throw new IllegalArgumentException(
                    "La empresa indicada no existe"
            );
        }

        /*
         * El frontend tampoco puede cambiar
         * directamente el estado activo.
         *
         * Para eso existen eliminar() y reactivar().
         */
        obj.setActivo(true);

        return dao.actualizar(
                obj
        );
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    public boolean eliminar(
            UUID idMensualidad
    ) throws SQLException {

        if (idMensualidad == null) {

            throw new IllegalArgumentException(
                    "La mensualidad es obligatoria"
            );
        }

        /*
         * No se ejecuta DELETE FROM.
         *
         * DAO:
         * UPDATE mensualidad_empresa
         * SET activo = FALSE
         */
        return dao.eliminarLogico(
                idMensualidad
        );
    }

    // =========================================================
    // REACTIVAR
    // =========================================================

    public boolean reactivar(
            UUID idMensualidad
    ) throws SQLException {

        if (idMensualidad == null) {

            throw new IllegalArgumentException(
                    "La mensualidad es obligatoria"
            );
        }

        Optional<MensualidadEmpresa> existente =
                dao.buscarPorIdIncluyendoInactivos(
                        idMensualidad
                );

        if (existente.isEmpty()) {

            return false;
        }

        MensualidadEmpresa mensualidad =
                existente.get();

        if (Boolean.TRUE.equals(
                mensualidad.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "La mensualidad ya se encuentra activa"
            );
        }

        /*
         * Antes de reactivar verificamos que
         * no exista otra mensualidad activa
         * para la misma empresa y periodo.
         */
        if (dao.existePeriodo(
                mensualidad.getIdEmpresa(),
                mensualidad.getPeriodo()
        )) {

            throw new IllegalArgumentException(
                    "Ya existe otra mensualidad activa para esta empresa y periodo"
            );
        }

        return dao.reactivar(
                idMensualidad
        );
    }

    // =========================================================
    // VALIDACIÓN GENERAL
    // =========================================================

    private void validar(
            MensualidadEmpresa obj
    ) {

        // -----------------------------------------------------
        // EMPRESA
        // -----------------------------------------------------

        if (obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        // -----------------------------------------------------
        // PERIODO
        // -----------------------------------------------------

        if (vacio(
                obj.getPeriodo()
        )) {

            throw new IllegalArgumentException(
                    "El periodo es obligatorio"
            );
        }

        String periodo =
                obj.getPeriodo()
                        .trim();

        /*
         * Formato:
         *
         * AAAA-MM
         *
         * Ejemplo:
         * 2026-08
         */
        if (!periodo.matches(
                "\\d{4}-(0[1-9]|1[0-2])"
        )) {

            throw new IllegalArgumentException(
                    "El periodo debe tener formato AAAA-MM"
            );
        }

        obj.setPeriodo(
                periodo
        );

        // -----------------------------------------------------
        // VALOR
        // -----------------------------------------------------

        if (obj.getValor() == null) {

            throw new IllegalArgumentException(
                    "El valor es obligatorio"
            );
        }

        if (obj.getValor()
                .compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new IllegalArgumentException(
                    "El valor debe ser mayor que cero"
            );
        }

        // -----------------------------------------------------
        // FECHA VENCIMIENTO
        // -----------------------------------------------------

        if (obj.getFechaVencimiento() == null) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento es obligatoria"
            );
        }

        // -----------------------------------------------------
        // ESTADO
        // -----------------------------------------------------

        if (vacio(
                obj.getEstado()
        )) {

            obj.setEstado(
                    "PENDIENTE"
            );
        }

        String estado =
                obj.getEstado()
                        .trim()
                        .toUpperCase();

        if (!estado.equals("PENDIENTE") &&
                !estado.equals("PAGADA") &&
                !estado.equals("VENCIDA")) {

            throw new IllegalArgumentException(
                    "El estado debe ser PENDIENTE, PAGADA o VENCIDA"
            );
        }

        obj.setEstado(
                estado
        );

        // -----------------------------------------------------
        // FECHA DE PAGO
        // -----------------------------------------------------

        if (estado.equals("PAGADA")) {

            /*
             * Si se marca como pagada y no se indicó
             * fecha de pago, utilizamos la fecha actual.
             */
            if (obj.getFechaPago() == null) {

                obj.setFechaPago(
                        LocalDate.now()
                );
            }

        } else {

            /*
             * Una mensualidad pendiente o vencida
             * no debe conservar fecha de pago.
             */
            obj.setFechaPago(
                    null
            );
        }

        // -----------------------------------------------------
        // OBSERVACIÓN
        // -----------------------------------------------------

        if (obj.getObservacion() != null) {

            String observacion =
                    obj.getObservacion()
                            .trim();

            if (observacion.isEmpty()) {

                obj.setObservacion(
                        null
                );

            } else {

                if (observacion.length() > 255) {

                    throw new IllegalArgumentException(
                            "La observación no puede superar 255 caracteres"
                    );
                }

                obj.setObservacion(
                        observacion
                );
            }
        }
    }

    // =========================================================
    // UTILIDAD
    // =========================================================

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}