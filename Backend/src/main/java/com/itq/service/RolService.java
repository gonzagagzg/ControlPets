package com.itq.service;

import com.itq.dao.RolDAO;
import com.itq.model.Rol;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class    RolService {
    private final RolDAO dao = new RolDAO();
    public List<Rol> listar() throws SQLException { return dao.listar(); }
    public List<Rol> listarOperativos() throws SQLException { return dao.listarOperativos();}
    public Optional<Rol> buscarPorId(Integer idRol) throws SQLException { return dao.buscarPorId(idRol); }
    public Rol crear(Rol obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Rol obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idRol) throws SQLException { return dao.eliminar(idRol); }
    private void validar(Rol obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del rol son obligatorios");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre del rol es obligatorio");
        obj.setNombre(obj.getNombre().trim().toUpperCase());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
