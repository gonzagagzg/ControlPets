package com.itq.service;

import com.itq.dao.PermisoDAO;
import com.itq.model.Permiso;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PermisoService {
    private final PermisoDAO dao = new PermisoDAO();
    public List<Permiso> listar() throws SQLException { return dao.listar(); }
    public Optional<Permiso> buscarPorId(Integer idPermiso) throws SQLException { return dao.buscarPorId(idPermiso); }
    public Permiso crear(Permiso obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Permiso obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idPermiso) throws SQLException { return dao.eliminar(idPermiso); }
    private void validar(Permiso obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del permiso son obligatorios");
        if (vacio(obj.getModulo())) throw new IllegalArgumentException("El módulo es obligatorio");
        if (vacio(obj.getAccion())) throw new IllegalArgumentException("La acción es obligatoria");
        obj.setModulo(obj.getModulo().trim().toUpperCase());
        obj.setAccion(obj.getAccion().trim().toUpperCase());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
