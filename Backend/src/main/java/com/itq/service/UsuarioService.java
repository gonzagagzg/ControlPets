package com.itq.service;

import com.itq.dao.RolDAO;
import com.itq.dao.UsuarioDAO;
import com.itq.model.Rol;
import com.itq.model.Usuario;
import com.itq.util.PasswordUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class UsuarioService {

    private final UsuarioDAO dao =
            new UsuarioDAO();

    private final RolDAO rolDao =
            new RolDAO();

    private static final Pattern PATRON_CORREO =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    /*
     * Roles que un usuario local puede asignar
     * a empleados de su propia veterinaria.
     *
     * 3 = Veterinario
     * 4 = Recepcionista
     * 5 = Asistente Clínico
     * 9 = Farmacéutico
     */
    private static final Set<Integer> ROLES_EMPLEADOS =
            Set.of(3, 4, 5, 9);

    /*
     * Tipos válidos de bloqueo de usuario.
     */
    private static final Set<String> TIPOS_BLOQUEO =
            Set.of("pago", "tecnico");

    // =========================================================
    // LISTAR
    // =========================================================

    /**
     * Rellena `nombreRol` en cada usuario.
     *
     * GET /api/roles solo devuelve los roles operativos a quien no es
     * SuperUsuario (ver RolServlet), así que la columna "Rol" no puede
     * resolverse en el cliente para cuentas con rol reservado (p. ej. el propio
     * Administrador Local). El nombre viaja con el usuario en su lugar.
     */
    private List<Usuario> conNombreRol(
            List<Usuario> usuarios
    ) throws SQLException {

        if (usuarios == null || usuarios.isEmpty()) {
            return usuarios;
        }

        Map<Integer, String> nombres =
                new HashMap<>();

        for (Rol rol : rolDao.listar()) {

            nombres.put(
                    rol.getIdRol(),
                    rol.getNombre()
            );
        }

        for (Usuario usuario : usuarios) {

            usuario.setNombreRol(
                    nombres.get(
                            usuario.getIdRol()
                    )
            );
        }

        return usuarios;
    }

    public List<Usuario> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {

            return conNombreRol(
                    dao.listar()
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return conNombreRol(
                dao.listarPorEmpresa(
                        idEmpresa
                )
        );
    }

    // =========================================================
    // SUPERUSUARIO - LISTAR USUARIOS DE UNA EMPRESA
    // =========================================================

    public List<Usuario> listarPorEmpresaSuperUsuario(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (!superUsuario) {

            throw new SecurityException(
                    "Esta operación requiere SuperUsuario"
            );
        }

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return conNombreRol(
                dao.listarPorEmpresa(
                        idEmpresa
                )
        );
    }
    // =========================================================
    // ADMINISTRADOR LOCAL - LISTAR USUARIOS VETERINARIO
    // =========================================================

    public List<Usuario> listarVeterinariosDisponibles(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            throw new SecurityException(
                    "Esta operación está destinada al Administrador Local"
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return conNombreRol(
                dao.listarPorEmpresaYRol(
                        idEmpresa,
                        3
                )
        );
    }
    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Usuario> buscarPorId(
            UUID idUsuario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (superUsuario) {

            return dao.buscarPorId(
                    idUsuario
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return dao.buscarPorIdYEmpresa(
                idUsuario,
                idEmpresa
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Usuario crear(
            Usuario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios"
            );
        }

        /*
         * Un usuario local NO puede decidir
         * a qué empresa pertenece el empleado.
         */
        if (!superUsuario) {

            validarEmpresaSesion(
                    idEmpresa
            );

            obj.setIdEmpresa(
                    idEmpresa
            );

            /*
             * Un usuario local solamente puede
             * asignar roles de empleados.
             */
            validarRolEmpleado(
                    obj.getIdRol()
            );
        }

        validarBase(
                obj
        );

        /*
         * Validar que el correo no esté registrado
         * en otro usuario.
         */
        if (obj.getCorreo() != null &&
                dao.existeCorreo(
                        obj.getCorreo()
                )) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con este correo electrónico"
            );
        }

        if (vacio(
                obj.getClaveHash()
        )) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        /*
         * La contraseña recibida se transforma
         * a BCrypt antes de guardarse.
         */
        obj.setClaveHash(
                PasswordUtil.hash(
                        obj.getClaveHash()
                                .trim()
                )
        );

        Usuario creado =
                dao.insertar(
                        obj
                );

        /*
         * Nunca se devuelve el hash.
         */
        creado.setClaveHash(
                null
        );

        return creado;
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            Usuario obj,
            UUID idEmpresa,
            boolean superUsuario,
            UUID idUsuarioSesion
    ) throws SQLException {

        if (obj == null ||
                obj.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios"
            );
        }

        /*
         * Un usuario local no puede editar su propia cuenta:
         * solo un SuperUsuario puede gestionar cualquier cuenta.
         */
        if (!superUsuario &&
                idUsuarioSesion != null &&
                obj.getIdUsuario()
                        .equals(
                                idUsuarioSesion
                        )) {

            throw new SecurityException(
                    "No puedes editar tu propia cuenta. Solicítalo a un SuperUsuario"
            );
        }

        Optional<Usuario> existenteOpt =
                dao.buscarPorIdConHash(
                        obj.getIdUsuario()
                );

        if (existenteOpt.isEmpty()) {

            return false;
        }

        Usuario existente =
                existenteOpt.get();

        /*
         * Un usuario local solamente puede modificar
         * empleados pertenecientes a su veterinaria.
         */
        if (!superUsuario) {

            validarEmpresaSesion(
                    idEmpresa
            );

            if (!idEmpresa.equals(
                    existente.getIdEmpresa()
            )) {

                return false;
            }

            /*
             * La empresa se toma del JWT.
             */
            obj.setIdEmpresa(
                    idEmpresa
            );

            /*
             * Tampoco puede escalar privilegios.
             */
            validarRolEmpleado(
                    obj.getIdRol()
            );
        }

        validarBase(
                obj
        );

        /*
         * Validar que el correo no pertenezca
         * a otro usuario distinto.
         */
        if (obj.getCorreo() != null &&
                dao.existeCorreoEnOtroUsuario(
                        obj.getCorreo(),
                        obj.getIdUsuario()
                )) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con este correo electrónico"
            );
        }

        /*
         * Si no llega contraseña nueva,
         * conservamos el hash actual.
         */
        if (vacio(
                obj.getClaveHash()
        )) {

            obj.setClaveHash(
                    existente.getClaveHash()
            );

        } else {

            obj.setClaveHash(
                    PasswordUtil.hash(
                            obj.getClaveHash()
                                    .trim()
                    )
            );
        }

        if (superUsuario) {

            return dao.actualizar(
                    obj
            );
        }

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    /*
     * NOTA:
     * Este módulo todavía utiliza eliminación física.
     * La migración general a soft delete quedó pendiente
     * para la revisión global del proyecto.
     */
    // =========================================================

    public boolean eliminar(
            UUID idUsuario,
            UUID idEmpresa,
            boolean superUsuario,
            UUID idUsuarioSesion
    ) throws SQLException {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (superUsuario) {

            return dao.eliminar(
                    idUsuario
            );
        }

        /*
         * Un usuario local no puede eliminar su propia cuenta:
         * solo un SuperUsuario puede gestionar cualquier cuenta.
         */
        if (idUsuarioSesion != null &&
                idUsuario.equals(
                        idUsuarioSesion
                )) {

            throw new SecurityException(
                    "No puedes eliminar tu propia cuenta. Solicítalo a un SuperUsuario"
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        Optional<Usuario> existente =
                dao.buscarPorIdYEmpresa(
                        idUsuario,
                        idEmpresa
                );

        if (existente.isEmpty()) {

            return false;
        }

        /*
         * Un usuario local tampoco puede eliminar
         * una cuenta con rol reservado.
         */
        validarRolEmpleado(
                existente.get()
                        .getIdRol()
        );

        return dao.eliminarPorEmpresa(
                idUsuario,
                idEmpresa
        );
    }

    // =========================================================
    // BLOQUEAR
    // =========================================================

    public boolean bloquear(
            UUID idUsuario,
            String tipoBloqueo,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (!superUsuario) {

            throw new SecurityException(
                    "Solo un SuperUsuario puede bloquear usuarios"
            );
        }

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (tipoBloqueo == null ||
                !TIPOS_BLOQUEO.contains(tipoBloqueo)) {

            throw new IllegalArgumentException(
                    "El tipo de bloqueo debe ser 'pago' o 'tecnico'"
            );
        }

        return dao.actualizarBloqueo(
                idUsuario,
                false,
                tipoBloqueo
        );
    }

    // =========================================================
    // DESBLOQUEAR
    // =========================================================

    public boolean desbloquear(
            UUID idUsuario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (!superUsuario) {

            throw new SecurityException(
                    "Solo un SuperUsuario puede desbloquear usuarios"
            );
        }

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        return dao.actualizarBloqueo(
                idUsuario,
                true,
                null
        );
    }

    // =========================================================
    // VALIDACIÓN BASE
    // =========================================================

    private void validarBase(
            Usuario obj
    ) {

        if (obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdRol() == null ||
                obj.getIdRol() <= 0) {

            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        if (vacio(
                obj.getUsuario()
        )) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio"
            );
        }

        if (vacio(
                obj.getNombres()
        )) {

            throw new IllegalArgumentException(
                    "Los nombres son obligatorios"
            );
        }

        if (obj.isActivo() == null) {

            throw new IllegalArgumentException(
                    "El estado activo es obligatorio"
            );
        }

        obj.setUsuario(
                obj.getUsuario()
                        .trim()
        );

        obj.setNombres(
                obj.getNombres()
                        .trim()
        );

        /*
         * El correo es opcional para no romper
         * usuarios antiguos, pero si llega,
         * debe tener un formato válido.
         */
        if (obj.getCorreo() != null &&
                !obj.getCorreo()
                        .trim()
                        .isEmpty()) {

            String correo =
                    obj.getCorreo()
                            .trim()
                            .toLowerCase();

            if (!PATRON_CORREO
                    .matcher(correo)
                    .matches()) {

                throw new IllegalArgumentException(
                        "El correo electrónico no es válido"
                );
            }

            obj.setCorreo(
                    correo
            );

        } else {

            obj.setCorreo(
                    null
            );
        }
    }

    // =========================================================
    // VALIDAR ROL EMPLEADO
    // =========================================================

    private void validarRolEmpleado(
            Integer idRol
    ) {

        if (idRol == null) {

            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        if (!ROLES_EMPLEADOS.contains(
                idRol
        )) {

            throw new SecurityException(
                    "No tiene autorización para asignar este rol"
            );
        }
    }

    // =========================================================
    // EMPRESA DEL JWT
    // =========================================================

    private void validarEmpresaSesion(
            UUID idEmpresa
    ) {

        if (idEmpresa == null) {

            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    // =========================================================
    // UTILIDAD
    // =========================================================

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}