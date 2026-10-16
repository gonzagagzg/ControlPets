package com.itq.service;

import com.itq.dao.MascotaVacunaDAO;
import com.itq.model.MascotaVacuna;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaVacunaService {
    private final MascotaVacunaDAO dao = new MascotaVacunaDAO();
    public List<MascotaVacuna> listar() throws SQLException { return dao.listar(); }
    public Optional<MascotaVacuna> buscarPorId(UUID idMascotaVacuna) throws SQLException { return dao.buscarPorId(idMascotaVacuna); }
    public MascotaVacuna crear(MascotaVacuna obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(MascotaVacuna obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idMascotaVacuna) throws SQLException { return dao.eliminar(idMascotaVacuna); }
    private void validar(MascotaVacuna obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de vacunación son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdMascota() == null) throw new IllegalArgumentException("La mascota es obligatoria");
        if (obj.getIdVacuna() == null || obj.getIdVacuna() <= 0) throw new IllegalArgumentException("La vacuna es obligatoria");
        if (obj.getFechaAplicacion() == null) throw new IllegalArgumentException("La fecha de aplicación es obligatoria");
        if (obj.getFechaAplicacion().isBefore(LocalDate.now())) throw new IllegalArgumentException("La fecha de aplicación debe ser actual o futura");
    }
}
