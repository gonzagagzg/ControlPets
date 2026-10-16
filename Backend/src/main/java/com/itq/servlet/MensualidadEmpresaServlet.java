package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.MensualidadEmpresa;
import com.itq.security.Autorizacion;
import com.itq.service.MensualidadEmpresaService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/mensualidades/*")
public class MensualidadEmpresaServlet extends HttpServlet {

    private final MensualidadEmpresaService service =
            new MensualidadEmpresaService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresaSesion =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            // =================================================
            // GET /api/mensualidades
            //
            // SuperUsuario:
            //      ve todas las mensualidades.
            //
            // Administrador Local:
            //      ve solamente las mensualidades
            //      de su propia empresa.
            // =================================================

            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
                        "LISTAR"
                );

                if (superUsuario) {

                    HttpUtil.json(
                            resp,
                            200,
                            ApiResponse.ok(
                                    "Listado",
                                    service.listar()
                            )
                    );

                } else {

                    if (idEmpresaSesion == null) {

                        throw new SecurityException(
                                "El usuario no tiene una empresa asignada"
                        );
                    }

                    HttpUtil.json(
                            resp,
                            200,
                            ApiResponse.ok(
                                    "Mensualidades de la veterinaria",
                                    service.listarPorEmpresa(
                                            idEmpresaSesion
                                    )
                            )
                    );
                }

                return;
            }

            // =================================================
            // GET /api/mensualidades/empresa/{idEmpresa}
            //
            // SOLO SUPERUSUARIO
            //
            // El Administrador Local NO puede enviar
            // manualmente otra empresa en la URL.
            // =================================================

            if (raw.equalsIgnoreCase("empresa")) {

                exigirSuperUsuario(req);

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
                        "LISTAR"
                );

                String idEmpresaRaw =
                        segundoValor(req);

                if (idEmpresaRaw == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "La empresa es obligatoria"
                    );

                    return;
                }

                UUID idEmpresa =
                        UUID.fromString(
                                idEmpresaRaw
                        );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Mensualidades de la empresa",
                                service.listarPorEmpresa(
                                        idEmpresa
                                )
                        )
                );

                return;
            }

            // =================================================
            // GET /api/mensualidades/{id}
            //
            // SuperUsuario:
            //      puede consultar cualquier mensualidad.
            //
            // Administrador Local:
            //      únicamente puede consultar una mensualidad
            //      perteneciente a su propia empresa.
            // =================================================

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "VER"
            );

            UUID idMensualidad =
                    UUID.fromString(raw);

            var encontrado =
                    superUsuario
                            ? service.buscarPorId(
                                    idMensualidad
                            )
                            : service.buscarPorIdYEmpresa(
                                    idMensualidad,
                                    idEmpresaSesion
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

            HttpUtil.errorValidacion(resp, e);

        } catch (SQLException e) {

            HttpUtil.errorBaseDatos(resp, e);

        } catch (IllegalStateException e) {

            HttpUtil.errorInterno(resp, e);
        }
    }

    // =========================================================
    // POST
    // CREAR MENSUALIDAD
    //
    // SOLO SUPERUSUARIO
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "CREAR"
            );

            MensualidadEmpresa obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    MensualidadEmpresa.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la mensualidad son obligatorios"
                );

                return;
            }

            MensualidadEmpresa creada =
                    service.crear(obj);

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
                            creada
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
    // EDITAR / REACTIVAR
    //
    // SOLO SUPERUSUARIO
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

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
                    UUID.fromString(raw);

            String accion =
                    segundoValor(req);

            // =================================================
            // PUT /api/mensualidades/{id}/reactivar
            // =================================================

            if (accion != null &&
                    accion.equalsIgnoreCase(
                            "reactivar"
                    )) {

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
                        "EDITAR"
                );

                if (!service.reactivar(id)) {

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
                                "Registro reactivado",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/mensualidades/{id}
            // =================================================

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "EDITAR"
            );

            MensualidadEmpresa obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    MensualidadEmpresa.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la mensualidad son obligatorios"
                );

                return;
            }

            obj.setIdMensualidad(
                    id
            );

            if (!service.actualizar(
                    obj
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
    // SOFT DELETE
    //
    // SOLO SUPERUSUARIO
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "ELIMINAR"
            );

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
                    UUID.fromString(raw);

            if (!service.eliminar(id)) {

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
                            "Registro desactivado",
                            null
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
        }
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
    // VERIFICAR SUPERUSUARIO
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
    // EXIGIR SUPERUSUARIO
    // =========================================================

    private void exigirSuperUsuario(
            HttpServletRequest req
    ) {

        if (!esSuperUsuario(req)) {

            throw new SecurityException(
                    "Esta operación requiere SuperUsuario"
            );
        }
    }

    // =========================================================
    // PRIMER VALOR DEL PATH
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

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length == 0 ||
                partes[0].isBlank()) {

            return null;
        }

        return partes[0];
    }

    // =========================================================
    // SEGUNDO VALOR DEL PATH
    // =========================================================

    private String segundoValor(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length < 2 ||
                partes[1].isBlank()) {

            return null;
        }

        return partes[1];
    }
}