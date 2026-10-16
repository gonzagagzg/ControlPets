package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.RecetaDetalle;
import com.itq.service.RecetaDetalleService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/receta-detalles/*")
public class RecetaDetalleServlet extends HttpServlet {
    private final RecetaDetalleService service = new RecetaDetalleService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID idEmpresa = obtenerIdEmpresa(req);
            boolean superUsuario = esSuperUsuario(req);

            String raw = valorId(req);
            if (raw == null) {
                HttpUtil.json(resp, 200, ApiResponse.ok("Listado", service.listar(idEmpresa, superUsuario)));
                return;
            }

            UUID id = UUID.fromString(raw);
            var encontrado = service.buscarPorId(id, idEmpresa, superUsuario);
            if (encontrado.isEmpty()) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro encontrado", encontrado.get()));
        } catch (SecurityException e) { HttpUtil.error(resp, 403, e.getMessage()); }
          catch (IllegalArgumentException e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (IllegalStateException e) { HttpUtil.errorInterno(resp, e); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID idEmpresa = obtenerIdEmpresa(req);
            boolean superUsuario = esSuperUsuario(req);

            RecetaDetalle obj = JsonUtil.gson().fromJson(req.getReader(), RecetaDetalle.class);
            HttpUtil.json(resp, 201, ApiResponse.ok("Registro creado", service.crear(obj, idEmpresa, superUsuario)));
        } catch (SecurityException e) { HttpUtil.error(resp, 403, e.getMessage()); }
          catch (IllegalArgumentException e) { HttpUtil.errorValidacion(resp, e); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (IllegalStateException e) { HttpUtil.errorInterno(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID idEmpresa = obtenerIdEmpresa(req);
            boolean superUsuario = esSuperUsuario(req);

            String raw = valorId(req);
            if (raw == null) { HttpUtil.error(resp, 400, "Identificador obligatorio"); return; }

            UUID id = UUID.fromString(raw);
            RecetaDetalle obj = JsonUtil.gson().fromJson(req.getReader(), RecetaDetalle.class);
            if (obj == null) { HttpUtil.error(resp, 400, "Los datos del detalle de receta son obligatorios"); return; }
            obj.setIdDetalleReceta(id);

            if (!service.actualizar(obj, idEmpresa, superUsuario)) {
                HttpUtil.error(resp, 404, "Registro no encontrado");
                return;
            }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro actualizado", obj));
        } catch (SecurityException e) { HttpUtil.error(resp, 403, e.getMessage()); }
          catch (IllegalArgumentException e) { HttpUtil.errorValidacion(resp, e); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (IllegalStateException e) { HttpUtil.errorInterno(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID idEmpresa = obtenerIdEmpresa(req);
            boolean superUsuario = esSuperUsuario(req);

            String raw = valorId(req);
            if (raw == null) { HttpUtil.error(resp, 400, "Identificador obligatorio"); return; }

            UUID id = UUID.fromString(raw);
            if (!service.eliminar(id, idEmpresa, superUsuario)) {
                HttpUtil.error(resp, 404, "Registro no encontrado");
                return;
            }
            resp.setStatus(204);
        } catch (SecurityException e) { HttpUtil.error(resp, 403, e.getMessage()); }
          catch (IllegalArgumentException e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (IllegalStateException e) { HttpUtil.errorInterno(resp, e); }
    }

    private UUID obtenerIdEmpresa(HttpServletRequest req) {
        return (UUID) req.getAttribute("idEmpresa");
    }

    private boolean esSuperUsuario(HttpServletRequest req) {
        String rol = (String) req.getAttribute("rol");
        return rol != null && rol.equalsIgnoreCase("SuperUsuario");
    }

    private String valorId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/") || path.isBlank()) return null;
        return path.substring(1).split("/")[0];
    }
}
