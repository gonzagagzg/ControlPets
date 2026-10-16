import { useCallback, useMemo, useState } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Building2, Pencil, Power, PowerOff } from 'lucide-react'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { TablaDatos } from '@/shared/components/TablaDatos'
import { CabeceraPagina, Campo, Cargando, MensajeError, Select } from '@/shared/components/ui'
import { indexarPor } from '@/shared/lib/utils'
import {
  empresaAdminApi,
  empresasApi,
  rolesApi,
  usuarioAdminApi,
  usuariosApi,
} from '@/shared/api/recursos'
import { useSesion } from '@/shared/session/sesion'
import type { Usuario, Uuid } from '@/shared/types/api'
import {
  EstadoUsuario,
  ModalBloqueoUsuario,
  ModalEditarUsuario,
  NotificacionUsuario,
  claseFilaNotificacion,
} from './usuarioComponentes'

/**
 * Roles que un usuario local puede gestionar. Es el reflejo en el cliente de
 * `ROLES_EMPLEADOS` en `UsuarioService`: los roles reservados (SuperUsuario,
 * Administrador Global/Local) solo los puede tocar un SuperUsuario.
 */
const ROLES_EMPLEADOS = new Set([
  'veterinario',
  'recepcionista',
  'asistente clinico',
  'farmaceutico',
])

/** Normaliza el nombre del rol (minúsculas y sin acentos) para compararlo. */
function normalizarRol(nombre: string): string {
  return nombre
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase()
}

/**
 * Etiqueta del estado de la cuenta. `estadoCuenta = 'pendiente'` significa
 * que aún no se confirmó el correo con el que se registró la veterinaria; es
 * independiente del bloqueo administrativo (`activo`).
 */
function etiquetaEstado(usuario: Usuario): string {
  if (usuario.estadoCuenta === 'pendiente') return 'Sin confirmar'
  return usuario.activo ? 'Activo' : 'Inactivo'
}

export function PaginaUsuarios() {
  const rol = useSesion((s) => s.rol)
  const esSuper = rol?.toLowerCase() === 'superusuario'

  return esSuper ? <UsuariosPorVeterinaria /> : <UsuariosDeMiEmpresa />
}

/**
 * SuperUsuario: primero elige una veterinaria y solo entonces ve sus
 * usuarios (GET /api/usuarios/empresa/{idEmpresa}). Desde la columna
 * "Acciones" puede editar cualquier cuenta (todos sus campos) y
 * bloquearla/desbloquearla con el mismo flujo que la página de empresas.
 */
function UsuariosPorVeterinaria() {
  const empresas = empresasApi.useLista()
  const roles = rolesApi.useLista()
  const [idEmpresa, setIdEmpresa] = useState<Uuid | ''>('')
  const [usuarioEditando, setUsuarioEditando] = useState<Usuario | null>(null)
  const [usuarioBloqueando, setUsuarioBloqueando] = useState<Usuario | null>(null)
  const desbloquear = usuarioAdminApi.useDesbloquear()

  const usuarios = empresaAdminApi.useUsuariosDeEmpresa(idEmpresa || undefined)
  const porRol = useMemo(() => indexarPor(roles.data, 'idRol'), [roles.data])

  const columnas = useMemo<ColumnDef<Usuario, unknown>[]>(
    () => [
      { accessorKey: 'usuario', header: 'Usuario' },
      { accessorKey: 'nombres', header: 'Nombres' },
      {
        id: 'rol',
        header: 'Rol',
        accessorFn: (u) => u.nombreRol ?? porRol.get(String(u.idRol))?.nombre ?? '—',
      },
      {
        id: 'activo',
        header: 'Estado',
        accessorFn: etiquetaEstado,
        cell: ({ row }) => <EstadoUsuario usuario={row.original} />,
      },
      {
        id: 'acciones',
        header: 'Acciones',
        enableSorting: false,
        cell: ({ row }) => {
          const u = row.original
          return (
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => setUsuarioEditando(u)}
                className="inline-flex items-center justify-center rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-brand-50 hover:text-brand-600"
                title="Editar usuario"
              >
                <Pencil className="h-4 w-4" />
              </button>
              {u.activo ? (
                <button
                  type="button"
                  onClick={() => setUsuarioBloqueando(u)}
                  className="inline-flex items-center justify-center rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600"
                  title="Bloquear usuario"
                >
                  <PowerOff className="h-4 w-4" />
                </button>
              ) : (
                <button
                  type="button"
                  disabled={desbloquear.isPending}
                  onClick={() => desbloquear.mutate(u.idUsuario)}
                  className="inline-flex items-center justify-center rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-emerald-50 hover:text-emerald-600 disabled:opacity-40"
                  title="Desbloquear usuario"
                >
                  <Power className="h-4 w-4" />
                </button>
              )}
            </div>
          )
        },
      },
      {
        id: 'notificacion',
        header: 'Notificación',
        enableSorting: false,
        accessorFn: (u) => u.notificacion ?? 'ninguna',
        cell: ({ row }) => <NotificacionUsuario usuario={row.original} />,
      },
    ],
    [porRol, desbloquear],
  )

  // Se espera a las veterinarias y a los roles: el selector y la columna "Rol"
  // dependen de ellos (si no, aparecerían vacíos un instante).
  if (empresas.isLoading || roles.isLoading) return <Cargando />

  return (
    <div>
      <CabeceraPagina
        titulo="Usuarios"
        descripcion="Selecciona una veterinaria para ver, editar o bloquear únicamente sus usuarios."
      />

      <div className="mb-5 max-w-sm">
        <Campo etiqueta="Veterinaria">
          <Select value={idEmpresa} onChange={(e) => setIdEmpresa(e.target.value)}>
            <option value="">Seleccionar veterinaria</option>
            {(empresas.data ?? []).map((e) => (
              <option key={e.idEmpresa} value={e.idEmpresa}>
                {e.razonSocial}
              </option>
            ))}
          </Select>
        </Campo>
      </div>

      {!idEmpresa ? (
        <div className="flex items-center gap-2 py-16 text-sm text-slate-400">
          <Building2 className="h-4 w-4" />
          Elige una veterinaria para ver sus usuarios
        </div>
      ) : usuarios.isLoading ? (
        <Cargando />
      ) : usuarios.error ? (
        <MensajeError error={usuarios.error} />
      ) : (
        <TablaDatos
          datos={usuarios.data ?? []}
          columnas={columnas}
          buscarPlaceholder="Buscar por usuario o nombres…"
          claseFila={claseFilaNotificacion}
        />
      )}

      <ModalEditarUsuario usuario={usuarioEditando} onCerrar={() => setUsuarioEditando(null)} />
      <ModalBloqueoUsuario usuario={usuarioBloqueando} onCerrar={() => setUsuarioBloqueando(null)} />
    </div>
  )
}

/** Administrador Local (y demás roles): CRUD completo, acotado a su propia empresa. */
function UsuariosDeMiEmpresa() {
  const idUsuario = useSesion((s) => s.idUsuario)
  const roles = rolesApi.useLista()
  const porRol = useMemo(() => indexarPor(roles.data, 'idRol'), [roles.data])

  // Un Administrador Local puede gestionar a sus empleados, pero no su propia
  // cuenta ni las cuentas con rol reservado: eso queda reservado al
  // SuperUsuario (el backend lo rechaza igualmente con un 403).
  const bloqueoGestion = useCallback(
    (u: Usuario) => {
      if (idUsuario && u.idUsuario === idUsuario) {
        return 'No puedes editar tu propia cuenta'
      }

      const nombre = u.nombreRol ?? porRol.get(String(u.idRol))?.nombre
      if (nombre && !ROLES_EMPLEADOS.has(normalizarRol(nombre))) {
        return 'Solo un SuperUsuario puede editar cuentas con este rol'
      }

      return null
    },
    [idUsuario, porRol],
  )

  const columnas = useMemo<ColumnDef<Usuario, unknown>[]>(
    () => [
      { accessorKey: 'usuario', header: 'Usuario' },
      { accessorKey: 'nombres', header: 'Nombres' },
      {
        id: 'rol',
        header: 'Rol',
        accessorFn: (u) => u.nombreRol ?? porRol.get(String(u.idRol))?.nombre ?? '—',
      },
      {
        id: 'activo',
        header: 'Estado',
        accessorFn: etiquetaEstado,
        cell: ({ row }) => <EstadoUsuario usuario={row.original} />,
      },
    ],
    [porRol],
  )

  return (
    <PaginaCrud<Usuario>
      titulo="Usuarios"
      singular="usuario"
      descripcion="Cuentas de acceso al sistema"
      api={usuariosApi}
      campoId="idUsuario"
      multiEmpresa
      cargando={roles.isLoading}
      bloqueoGestion={bloqueoGestion}
      columnas={columnas}
      campos={[
        {
          nombre: 'usuario',
          etiqueta: 'Usuario',
          tipo: 'texto',
          requerido: true,
          maxLength: 50,
          formato: 'usuario',
          ayuda: 'Sin espacios. Solo letras, números, punto, guion o guion bajo',
        },
        {
          nombre: 'nombres',
          etiqueta: 'Nombres',
          tipo: 'texto',
          requerido: true,
          maxLength: 100,
          formato: 'nombre',
        },
        {
          nombre: 'idRol',
          etiqueta: 'Rol',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (roles.data ?? []).map((r) => ({ valor: r.idRol, etiqueta: r.nombre })),
        },
        { nombre: 'activo', etiqueta: 'Activo', tipo: 'booleano' },
        {
          nombre: 'claveHash',
          etiqueta: 'Contraseña',
          tipo: 'contrasena',
          requerido: true,
          maxLength: 255,
          anchoCompleto: true,
          ayuda: 'La contraseña estara segura y se cifrada.',
        },
      ]}
    />
  )
}
