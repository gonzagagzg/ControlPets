package com.itq.service;

import com.itq.dao.CitaDAO;
import com.itq.dao.MascotaDAO;
import com.itq.dao.VeterinarioDAO;
import com.itq.model.Cita;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CitaService {

    private final CitaDAO dao =
            new CitaDAO();

    private final MascotaDAO mascotaDAO =
            new MascotaDAO();

    private final VeterinarioDAO veterinarioDAO =
            new VeterinarioDAO();

    public List<Cita> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Cita> buscarPorId(
            UUID idCita,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idCita);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idCita,
                idEmpresa
        );
    }

    public Cita crear(
            Cita obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la cita son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            // La empresa siempre sale del JWT.
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        // No se agendan citas en días anteriores a hoy. Se compara la fecha del
        // instante recibido (el cliente lo envía en UTC) contra el día UTC actual;
        // así nunca se rechaza una cita del día local del usuario.
        if (obj.getFechaHora()
                .toLocalDate()
                .isBefore(LocalDate.now(ZoneOffset.UTC))) {

            throw new IllegalArgumentException(
                    "No puedes agendar una cita en una fecha anterior a hoy"
            );
        }

        validarRelacionesEmpresa(
                obj.getIdMascota(),
                obj.getIdVeterinario(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Cita obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdCita() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la cita son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarNoRetrocedeFecha(
                    obj,
                    dao.buscarPorId(
                            obj.getIdCita()
                    ).orElse(null)
            );

            validarRelacionesEmpresa(
                    obj.getIdMascota(),
                    obj.getIdVeterinario(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        Optional<Cita> original =
                dao.buscarPorIdYEmpresa(
                        obj.getIdCita(),
                        idEmpresa
                );

        if (original.isEmpty()) {

            return false;
        }

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        validarNoRetrocedeFecha(
                obj,
                original.get()
        );

        validarRelacionesEmpresa(
                obj.getIdMascota(),
                obj.getIdVeterinario(),
                idEmpresa
        );

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idCita,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idCita);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idCita,
                idEmpresa
        );
    }

    private void validarRelacionesEmpresa(
            UUID idMascota,
            UUID idVeterinario,
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (mascotaDAO.buscarPorIdYEmpresa(
                idMascota,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "La mascota no pertenece a la empresa indicada"
            );
        }

        if (veterinarioDAO.buscarPorIdYEmpresa(
                idVeterinario,
                idEmpresa
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "El veterinario no pertenece a la empresa indicada"
            );
        }
    }

    /**
     * Al editar, la cita no puede moverse a un día anterior al que ya tenía
     * agendado. La comparación es por día en UTC (el cliente envía el instante
     * en UTC): así no se rechaza por diferencias de segundos ni de zona horaria.
     */
    private void validarNoRetrocedeFecha(
            Cita nuevo,
            Cita original
    ) {

        if (original == null ||
                original.getFechaHora() == null ||
                nuevo.getFechaHora() == null) {

            return;
        }

        if (nuevo.getFechaHora()
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDate()
                .isBefore(
                        original.getFechaHora()
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toLocalDate()
                )) {

            throw new IllegalArgumentException(
                    "No puedes mover la cita a una fecha anterior a la que estaba agendada"
            );
        }
    }

    private void validar(Cita obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos de la cita son obligatorios"
            );
        }

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdMascota() == null) {
            throw new IllegalArgumentException(
                    "La mascota es obligatoria"
            );
        }

        if (obj.getIdVeterinario() == null) {
            throw new IllegalArgumentException(
                    "El veterinario es obligatorio"
            );
        }

        if (obj.getFechaHora() == null) {
            throw new IllegalArgumentException(
                    "La fecha y hora son obligatorias"
            );
        }

        if (vacio(obj.getEstado())) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio"
            );
        }

        obj.setEstado(
                obj.getEstado()
                        .trim()
                        .toUpperCase()
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