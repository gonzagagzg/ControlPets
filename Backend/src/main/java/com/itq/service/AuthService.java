package com.itq.service;

import com.itq.dao.EmpresaDAO;
import com.itq.dao.RolDAO;
import com.itq.dao.UsuarioDAO;
import com.itq.dto.LoginResponse;
import com.itq.model.Empresa;
import com.itq.model.Rol;
import com.itq.model.Usuario;
import com.itq.util.JwtUtil;
import com.itq.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    public LoginResponse login(String usuario, String clave)
            throws SQLException {

        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        Usuario encontrado = usuarioDAO
                .buscarPorUsuarioParaLogin(usuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "Usuario o contraseña incorrectos"
                        )
                );

        if (!Boolean.TRUE.equals(encontrado.isActivo())) {
            String tipo = encontrado.getTipobloqueo();
            if ("pago".equals(tipo)) {
                throw new SecurityException(
                        "El usuario se encuentra bloqueado por falta de pago"
                );
            }
            if ("tecnico".equals(tipo)) {
                throw new SecurityException(
                        "El usuario se encuentra bloqueado por motivo técnico"
                );
            }
            throw new SecurityException(
                    "El usuario se encuentra inactivo"
            );
        }

        if (!PasswordUtil.verificar(
                clave,
                encontrado.getClaveHash()
        )) {
            throw new SecurityException(
                    "Usuario o contraseña incorrectos"
            );
        }

        /*
         * La cuenta debe tener el correo confirmado (estado_cuenta =
         * 'activo'). Las cuentas creadas sin correo ya nacen activas; solo
         * quedan 'pendiente' mientras no se abra el enlace del correo.
         */
        if ("pendiente".equalsIgnoreCase(
                encontrado.getEstadoCuenta()
        )) {

            throw new SecurityException(
                    "Debes confirmar tu correo electrónico antes de iniciar sesión"
            );
        }

        Rol rol = rolDAO
                .buscarPorId(encontrado.getIdRol())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El usuario no tiene un rol válido"
                        )
                );

        /*
         * El superusuario global puede tener una empresa
         * técnica/matriz. Los demás usuarios deben pertenecer
         * a una empresa activa.
         */
        if (encontrado.getIdEmpresa() != null) {

            Empresa empresa = empresaDAO
                    .buscarPorId(encontrado.getIdEmpresa())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "La empresa del usuario no existe"
                            )
                    );

            if (!Boolean.TRUE.equals(empresa.isActivo())) {
                throw new SecurityException(
                        "La empresa se encuentra inactiva"
                );
            }
        }

        String token = JwtUtil.generarToken(
                encontrado.getIdUsuario(),
                encontrado.getIdEmpresa(),
                encontrado.getIdRol(),
                rol.getNombre()
        );

        return new LoginResponse(
                token,
                encontrado.getIdUsuario(),
                encontrado.getIdEmpresa(),
                encontrado.getIdRol(),
                rol.getNombre(),
                encontrado.getNombres(),
                encontrado.getUsuario()
        );
    }

    // =========================================================
    // NOTIFICAR BLOQUEO (usuario bloqueado avisa al administrador)
    // =========================================================

    public void notificarBloqueo(String usuario)
            throws SQLException {

        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        Usuario encontrado = usuarioDAO
                .buscarPorUsuarioParaLogin(usuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "Usuario no encontrado"
                        )
                );

        if (Boolean.TRUE.equals(encontrado.isActivo())) {
            throw new SecurityException(
                    "El usuario no está bloqueado"
            );
        }

        String tipo = encontrado.getTipobloqueo();
        if (tipo == null) {
            throw new SecurityException(
                    "El usuario no tiene un tipo de bloqueo asignado"
            );
        }

        /*
         * La notificación guarda el mismo tipo con el que fue
         * bloqueado el usuario (pago o tecnico).
         */
        usuarioDAO.actualizarNotificacion(
                encontrado.getIdUsuario(),
                tipo
        );
    }
}