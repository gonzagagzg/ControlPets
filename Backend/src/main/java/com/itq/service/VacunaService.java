package com.itq.service;

import com.itq.dao.VacunaDAO;
import com.itq.model.Vacuna;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VacunaService {
    private final VacunaDAO dao = new VacunaDAO();
    public List<Vacuna> listar() throws SQLException { return dao.listar(); }
    public Optional<Vacuna> buscarPorId(Integer idVacuna) throws SQLException { return dao.buscarPorId(idVacuna); }
    public Vacuna crear(Vacuna obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Vacuna obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idVacuna) throws SQLException { return dao.eliminar(idVacuna); }
    private void validar(Vacuna obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la vacuna son obligatorios");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre de la vacuna es obligatorio");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
