package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.dto.FacturaEmitirRequest;
import com.itq.model.Factura;
import com.itq.security.Autorizacion;
import com.itq.service.FacturaService;
import com.itq.service.FacturacionTransaccionalService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/facturas/*")
public class FacturaServlet extends HttpServlet {

    private final FacturaService service =
            new FacturaService();

    private final FacturacionTransaccionalService
            facturacionService =
            new FacturacionTransaccionalService();

    // =========================================================
    // CONSULTAR FACTURAS
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            // GET /api/facturas
            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "FACTURAS",
                        "LISTAR"
                );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listar(
                                        idEmpresa,
                                        superUsuario
                                )
                        )
                );

                return;
            }

            // /emitir solamente acepta POST
            if (raw.equalsIgnoreCase("emitir")) {

                HttpUtil.error(
                        resp,
                        405,
                        "Método no permitido"
                );

                return;
            }

            // GET /api/facturas/{id}
            Autorizacion.exigir(
                    req,
                    "FACTURAS",
                    "VER"
            );

            UUID id =
                    UUID.fromString(raw);

            var encontrado =
                    service.buscarPorId(
                            id,
                            idEmpresa,
                            superUsuario
                    );

            if (encontrado.isEmpty()) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Registro encontrado",
                            encontrado.get()
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Identificador inválido"
            );

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // EMITIR FACTURA
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            UUID idUsuario =
                    obtenerIdUsuario(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String path =
                    req.getPathInfo();

            // =================================================
            // POST /api/facturas/emitir
            // =================================================

            if (path != null &&
                    path.equalsIgnoreCase("/emitir")) {

                Autorizacion.exigir(
                        req,
                        "FACTURAS",
                        "EMITIR"
                );

                FacturaEmitirRequest request =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        FacturaEmitirRequest.class
                                );

                Factura factura =
                        facturacionService.emitir(
                                request,
                                idEmpresa,
                                idUsuario,
                                superUsuario
                        );

                HttpUtil.json(
                        resp,
                        201,
                        ApiResponse.ok(
                                "Factura emitida correctamente",
                                factura
                        )
                );

                return;
            }

            // =================================================
            // POST /api/facturas
            // BLOQUEADO
            // =================================================

            HttpUtil.error(
                    resp,
                    405,
                    "Las facturas únicamente pueden generarse mediante /api/facturas/emitir"
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);

        } catch (Exception e) {

            HttpUtil.errorSolicitud(resp, e);
        }
    }

    // =========================================================
    // MODIFICAR FACTURA EMITIDA - BLOQUEADO
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpUtil.error(
                resp,
                405,
                "Las facturas emitidas no pueden modificarse directamente"
        );
    }

    // =========================================================
    // ELIMINAR FACTURA EMITIDA - BLOQUEADO
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpUtil.error(
                resp,
                405,
                "Las facturas emitidas no pueden eliminarse directamente"
        );
    }

    // =========================================================
    // EMPRESA DESDE JWT
    // =========================================================

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idEmpresa");
    }

    // =========================================================
    // USUARIO DESDE JWT
    // =========================================================

    private UUID obtenerIdUsuario(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idUsuario");
    }

    // =========================================================
    // SUPERUSUARIO
    // =========================================================

    private boolean esSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute("rol");

        return rol != null &&
                rol.equalsIgnoreCase(
                        "SuperUsuario"
                );
    }

    // =========================================================
    // ID DESDE URL
    // =========================================================

    private String valorId(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String valor =
                path.substring(1)
                        .split("/")[0];

        return valor.isBlank()
                ? null
                : valor;
    }
}