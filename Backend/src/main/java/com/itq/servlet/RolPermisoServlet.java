package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.RolPermiso;
import com.itq.service.RolPermisoService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/rol-permisos/*")
public class RolPermisoServlet extends HttpServlet {
    private final RolPermisoService service = new RolPermisoService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getParameter("idRol") == null) { HttpUtil.json(resp, 200, ApiResponse.ok("Listado", service.listar())); return; }
            Integer idRol = Integer.valueOf(req.getParameter("idRol"));
            Integer idPermiso = Integer.valueOf(req.getParameter("idPermiso"));
            var encontrado = service.buscarPorId(idRol, idPermiso);
            if (encontrado.isEmpty()) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro encontrado", encontrado.get()));
        } catch (IllegalArgumentException e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RolPermiso obj = JsonUtil.gson().fromJson(req.getReader(), RolPermiso.class);
            HttpUtil.json(resp, 201, ApiResponse.ok("Registro creado", service.crear(obj)));
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer idRol = Integer.valueOf(req.getParameter("idRol"));
            Integer idPermiso = Integer.valueOf(req.getParameter("idPermiso"));
            RolPermiso obj = JsonUtil.gson().fromJson(req.getReader(), RolPermiso.class);
            obj.setIdRol(idRol);
            obj.setIdPermiso(idPermiso);
            if (!service.actualizar(obj)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro actualizado", obj));
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer idRol = Integer.valueOf(req.getParameter("idRol"));
            Integer idPermiso = Integer.valueOf(req.getParameter("idPermiso"));
            if (!service.eliminar(idRol, idPermiso)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            resp.setStatus(204);
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
    }


}
