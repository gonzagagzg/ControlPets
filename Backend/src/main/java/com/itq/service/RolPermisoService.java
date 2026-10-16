package com.itq.service;

import com.itq.dao.RolPermisoDAO;
import com.itq.model.RolPermiso;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RolPermisoService {
    private final RolPermisoDAO dao = new RolPermisoDAO();
    public List<RolPermiso> listar() throws SQLException { return dao.listar(); }
    public Optional<RolPermiso> buscarPorId(Integer idRol, Integer idPermiso) throws SQLException { return dao.buscarPorId(idRol, idPermiso); }
    public RolPermiso crear(RolPermiso obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(RolPermiso obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idRol, Integer idPermiso) throws SQLException { return dao.eliminar(idRol, idPermiso); }
    private void validar(RolPermiso obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del rol y permiso son obligatorios");
        if (obj.getIdRol() == null || obj.getIdRol() <= 0) throw new IllegalArgumentException("El rol es obligatorio");
        if (obj.getIdPermiso() == null || obj.getIdPermiso() <= 0) throw new IllegalArgumentException("El permiso es obligatorio");
    }
}
