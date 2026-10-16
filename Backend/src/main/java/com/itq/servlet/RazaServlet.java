package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Raza;
import com.itq.service.RazaService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/razas/*")
public class RazaServlet extends HttpServlet {
    private final RazaService service = new RazaService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String raw = valorId(req);
            if (raw == null) { HttpUtil.json(resp, 200, ApiResponse.ok("Listado", service.listar())); return; }
            Integer id = Integer.valueOf(raw);
            var encontrado = service.buscarPorId(id);
            if (encontrado.isEmpty()) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro encontrado", encontrado.get()));
        } catch (IllegalArgumentException e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
          catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Raza obj = JsonUtil.gson().fromJson(req.getReader(), Raza.class);
            HttpUtil.json(resp, 201, ApiResponse.ok("Registro creado", service.crear(obj)));
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = Integer.valueOf(valorId(req));
            Raza obj = JsonUtil.gson().fromJson(req.getReader(), Raza.class);
            obj.setIdRaza(id);
            if (!service.actualizar(obj)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro actualizado", obj));
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.errorSolicitud(resp, e); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = Integer.valueOf(valorId(req));
            if (!service.eliminar(id)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            resp.setStatus(204);
        } catch (SQLException e) { HttpUtil.errorBaseDatos(resp, e); }
          catch (Exception e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
    }

    private String valorId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/") || path.isBlank()) return null;
        return path.substring(1).split("/")[0];
    }
}
