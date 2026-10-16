import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import { crearHooksCrud } from './crud'
import type {
  CambiarClaveAdminRequest,
  Categoria,
  Cita,
  Cliente,
  Empresa,
  Especie,
  Factura,
  FacturaDetalle,
  HistorialClinico,
  Mascota,
  MascotaVacuna,
  MensualidadEmpresa,
  MovimientoInventario,
  Permiso,
  Producto,
  Raza,
  Receta,
  RecetaDetalle,
  ResumenEmpresa,
  Rol,
  SriIva,
  Usuario,
  Uuid,
  Vacuna,
  ValidacionRuc,
  Veterinario,
} from '@/shared/types/api'

/**
 * Punto único donde se declara cada recurso de la API.
 * La ruta corresponde exactamente al @WebServlet del backend.
 *
 * Nota: `rol-permisos` NO se declara aquí porque usa llave compuesta por
 * query string en vez de path; vive en features/admin/api/rolPermisos.ts.
 */

export const especiesApi = crearHooksCrud<Especie>('especies', 'idEspecie')
export const razasApi = crearHooksCrud<Raza>('razas', 'idRaza')
export const vacunasApi = crearHooksCrud<Vacuna>('vacunas', 'idVacuna')
export const categoriasApi = crearHooksCrud<Categoria>('categorias', 'idCategoria')
export const sriIvaApi = crearHooksCrud<SriIva>('sri-iva', 'idIva')

export const rolesApi = crearHooksCrud<Rol>('roles', 'idRol')
export const permisosApi = crearHooksCrud<Permiso>('permisos', 'idPermiso')
export const empresasApi = crearHooksCrud<Empresa>('empresas', 'idEmpresa')
export const usuariosApi = crearHooksCrud<Usuario>('usuarios', 'idUsuario')
export const veterinariosApi = crearHooksCrud<Veterinario>('veterinarios', 'idVeterinario')

export const clientesApi = crearHooksCrud<Cliente>('clientes', 'idCliente')
export const mascotasApi = crearHooksCrud<Mascota>('mascotas', 'idMascota')
export const mascotaVacunasApi = crearHooksCrud<MascotaVacuna>('mascota-vacunas', 'idMascotaVacuna')
export const citasApi = crearHooksCrud<Cita>('citas', 'idCita')
export const historialesApi = crearHooksCrud<HistorialClinico>('historiales-clinicos', 'idHistorial')
export const recetasApi = crearHooksCrud<Receta>('recetas', 'idReceta')
export const recetaDetallesApi = crearHooksCrud<RecetaDetalle>('receta-detalles', 'idDetalleReceta')

export const productosApi = crearHooksCrud<Producto>('productos', 'idProducto')
export const facturasApi = crearHooksCrud<Factura>('facturas', 'idFactura')
export const facturaDetallesApi = crearHooksCrud<FacturaDetalle>('factura-detalles', 'idDetalle')
export const movimientosApi = crearHooksCrud<MovimientoInventario>(
  'movimientos-inventario',
  'idMovimiento',
)

/**
 * Extensiones del módulo Empresas reservadas a SuperUsuario.
 * `empresasApi` (arriba) ya cubre GET/POST/PUT genéricos de /api/empresas;
 * aquí solo lo que no encaja en el CRUD genérico: activar/desactivar y el
 * resumen de uso por empresa (EmpresaServlet en el backend).
 */
export const empresaAdminApi = {
  useResumen: (idEmpresa: Uuid | undefined) =>
    useQuery<ResumenEmpresa>({
      queryKey: ['empresas', 'resumen', idEmpresa],
      queryFn: () => api.get<ResumenEmpresa>(`/api/empresas/${idEmpresa}/resumen`),
      enabled: !!idEmpresa,
    }),

  useActivar: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, Uuid>({
      mutationFn: (idEmpresa) => api.put<void>(`/api/empresas/${idEmpresa}/activar`, undefined),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },

  useDesactivar: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, Uuid>({
      mutationFn: (idEmpresa) => api.put<void>(`/api/empresas/${idEmpresa}/desactivar`, undefined),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },

  // GET /api/usuarios/empresa/{idEmpresa} (UsuarioServlet) — solo SuperUsuario,
  // ya que el CRUD genérico de `usuariosApi` filtra siempre por la empresa
  // del propio token y no sirve para inspeccionar una veterinaria ajena.
  useUsuariosDeEmpresa: (idEmpresa: Uuid | undefined) =>
    useQuery<Usuario[]>({
      queryKey: ['empresas', idEmpresa, 'usuarios'],
      queryFn: () => api.get<Usuario[]>(`/api/usuarios/empresa/${idEmpresa}`),
      enabled: !!idEmpresa,
    }),

  // GET /api/empresas/validar-ruc?ruc=... — comprueba formato, existencia
  // real en el SRI y disponibilidad (que no esté ya registrado).
  useValidarRuc: (ruc: string | undefined) =>
    useQuery<ValidacionRuc>({
      queryKey: ['empresas', 'validar-ruc', ruc],
      queryFn: () => api.get<ValidacionRuc>('/api/empresas/validar-ruc', { ruc }),
      enabled: !!ruc && ruc.length === 13,
      retry: false,
    }),

  // PUT /api/empresas/{idEmpresa}/contrasena-admin — SuperUsuario cambia la
  // contraseña del Administrador Local de la veterinaria.
  useCambiarClaveAdmin: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, { idEmpresa: Uuid; datos: CambiarClaveAdminRequest }>({
      mutationFn: ({ idEmpresa, datos }) =>
        api.put<void>(`/api/empresas/${idEmpresa}/contrasena-admin`, datos),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },
}

/**
 * GET /api/usuarios/veterinarios-disponibles (UsuarioServlet) — usuarios con
 * rol Veterinario de la propia empresa (resuelta desde el JWT) que todavía
 * no tienen un registro en `veterinario`. Reemplaza el filtrado manual en
 * el cliente que antes hacía PaginaVeterinarios.
 */
export function useVeterinariosDisponibles() {
  return useQuery<Usuario[]>({
    queryKey: ['usuarios', 'veterinarios-disponibles'],
    queryFn: () => api.get<Usuario[]>('/api/usuarios/veterinarios-disponibles'),
  })
}

/**
 * Mensualidades/pagos de la aplicación (mensualidad_empresa). El backend
 * expone rutas distintas para Administrador Local (siempre su propia
 * empresa, resuelta desde el JWT) y SuperUsuario (todas, por empresa).
 */
export const mensualidadesApi = {
  // GET /api/mensualidades — Administrador Local: solo las de su empresa.
  useMisMensualidades: () =>
    useQuery<MensualidadEmpresa[]>({
      queryKey: ['mensualidades', 'propias'],
      queryFn: () => api.get<MensualidadEmpresa[]>('/api/mensualidades'),
    }),
}

/**
 * Bloqueo/desbloqueo de usuarios individuales (solo SuperUsuario).
 * PUT /api/usuarios/{id}/bloquear  — { tipoBloqueo: 'pago' | 'tecnico' }
 * PUT /api/usuarios/{id}/desbloquear — sin body
 */
export const usuarioAdminApi = {
  useBloquear: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, { idUsuario: Uuid; tipoBloqueo: 'pago' | 'tecnico' }>({
      mutationFn: ({ idUsuario, tipoBloqueo }) =>
        api.put<void>(`/api/usuarios/${idUsuario}/bloquear`, { tipoBloqueo }),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },

  useDesbloquear: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, Uuid>({
      mutationFn: (idUsuario) => api.put<void>(`/api/usuarios/${idUsuario}/desbloquear`, undefined),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },

  // PUT /api/usuarios/{id} — el SuperUsuario puede editar cualquier cuenta
  // (incluidos roles reservados y su empresa). Invalida también ['empresas']
  // porque el listado de usuarios por veterinaria vive bajo esa llave.
  useActualizar: () => {
    const qc = useQueryClient()
    return useMutation<Usuario, Error, { idUsuario: Uuid; datos: Partial<Usuario> }>({
      mutationFn: ({ idUsuario, datos }) =>
        api.put<Usuario>(`/api/usuarios/${idUsuario}`, { ...datos, idUsuario }),
      onSuccess: () => {
        qc.invalidateQueries({ queryKey: ['empresas'] })
        qc.invalidateQueries({ queryKey: ['usuarios'] })
      },
    })
  },
}
