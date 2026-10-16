package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Cliente;
import com.itq.security.Autorizacion;
import com.itq.service.ClienteService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/clientes/*")
public class ClienteServlet extends HttpServlet {

    private final ClienteService service =
            new ClienteService();

    // =========================================================
    // CONFIGURACIÓN PAGINACIÓN
    // =========================================================

    private static final int PAGINA_DEFECTO =
            0;

    private static final int TAMANIO_DEFECTO =
            50;

    // =========================================================
    // GET
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

            // =================================================
            // GET /api/clientes
            //
            // GET /api/clientes?pagina=0&tamanio=20
            // =================================================

            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "CLIENTES",
                        "LISTAR"
                );

                int pagina =
                        obtenerEntero(
                                req,
                                "pagina",
                                PAGINA_DEFECTO
                        );

                int tamanio =
                        obtenerEntero(
                                req,
                                "tamanio",
                                TAMANIO_DEFECTO
                        );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listarPaginado(
                                        idEmpresa,
                                        superUsuario,
                                        pagina,
                                        tamanio
                                )
                        )
                );

                return;
            }

            // =================================================
            // GET /api/clientes/{id}
            // =================================================

            Autorizacion.exigir(
                    req,
                    "CLIENTES",
                    "VER"
            );

            UUID id =
                    UUID.fromString(
                            raw
                    );

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

        } catch (NumberFormatException e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Los parámetros pagina y tamanio deben ser números enteros"
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // POST
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CLIENTES",
                    "CREAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            Cliente obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Cliente.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos del cliente son obligatorios"
                );

                return;
            }

            Cliente creado =
                    service.crear(
                            obj,
                            idEmpresa,
                            superUsuario
                    );

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
                            creado
                    )
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
    // PUT
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CLIENTES",
                    "EDITAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID id =
                    UUID.fromString(
                            raw
                    );

            Cliente obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Cliente.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos del cliente son obligatorios"
                );

                return;
            }

            obj.setIdCliente(
                    id
            );

            if (!service.actualizar(
                    obj,
                    idEmpresa,
                    superUsuario
            )) {

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
                            "Registro actualizado",
                            obj
                    )
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
    // DELETE
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "CLIENTES",
                    "ELIMINAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID id =
                    UUID.fromString(
                            raw
                    );

            if (!service.eliminar(
                    id,
                    idEmpresa,
                    superUsuario
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
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
        }
    }

    // =========================================================
    // PARÁMETRO ENTERO
    // =========================================================

    private int obtenerEntero(
            HttpServletRequest req,
            String nombre,
            int valorDefecto
    ) {

        String valor =
                req.getParameter(
                        nombre
                );

        if (valor == null ||
                valor.isBlank()) {

            return valorDefecto;
        }

        return Integer.parseInt(
                valor
        );
    }

    // =========================================================
    // EMPRESA DEL JWT
    // =========================================================

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute(
                        "idEmpresa"
                );
    }

    // =========================================================
    // SUPERUSUARIO
    // =========================================================

    private boolean esSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute(
                                "rol"
                        );

        return rol != null &&
                rol.equalsIgnoreCase(
                        "SuperUsuario"
                );
    }

    // =========================================================
    // ID DE LA URL
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