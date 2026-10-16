import { useEffect, useState, type FormEvent } from 'react'
import {
  Badge,
  Boton,
  Campo,
  Input,
  InputContrasena,
  MensajeError,
  Modal,
  Select,
} from '@/shared/components/ui'
import { empresasApi, rolesApi, usuarioAdminApi } from '@/shared/api/recursos'
import type { Usuario, Uuid } from '@/shared/types/api'
import {
  PATRON_CORREO,
  PATRON_NOMBRE,
  PATRON_USUARIO,
  limpiarNombre,
  limpiarUsuario,
} from '@/shared/lib/validacion'

/**
 * Estado de la cuenta de un usuario: confirmación del correo, bloqueo
 * administrativo (activo/inactivo) y su motivo (pago/técnico).
 *
 * Vive aparte de las páginas porque lo comparten la vista de usuarios por
 * veterinaria (SuperUsuario) y el modal de usuarios de una empresa.
 */
export function EstadoUsuario({ usuario }: { usuario: Usuario }) {
  if (usuario.estadoCuenta === 'pendiente') return <Badge tono="alerta">Sin confirmar</Badge>
  if (usuario.activo) return <Badge tono="exito">Activo</Badge>
  if (usuario.tipobloqueo === 'pago') return <Badge tono="peligro">Bloqueado Pago</Badge>
  if (usuario.tipobloqueo === 'tecnico') return <Badge tono="alerta">Bloqueado Técnico</Badge>
  return <Badge tono="peligro">Inactivo</Badge>
}

/**
 * Notificación pendiente del usuario: 'Pago', 'Técnico' o 'Ninguna'.
 * Compartido por la vista de usuarios por veterinaria (SuperUsuario) y el
 * modal de usuarios de una empresa, que además colorean la fila según este
 * valor (ver `claseFilaNotificacion`).
 */
export function NotificacionUsuario({ usuario }: { usuario: Usuario }) {
  if (usuario.notificacion === 'pago') return 'Pago'
  if (usuario.notificacion === 'tecnico') return 'Técnico'
  return 'Ninguna'
}

/**
 * Clase de fondo de una fila según su notificación, para destacarla igual que
 * en la página de empresas: rojo si es de pago, ámbar si es técnica.
 */
export function claseFilaNotificacion(usuario: Usuario): string | undefined {
  if (usuario.notificacion === 'pago') return 'bg-red-50'
  if (usuario.notificacion === 'tecnico') return 'bg-amber-50'
  return undefined
}

/**
 * Modal para bloquear un usuario (solo SuperUsuario). Permite elegir el
 * motivo del bloqueo: falta de pago o técnico.
 */
export function ModalBloqueoUsuario({
  usuario,
  onCerrar,
}: {
  usuario: Usuario | null
  onCerrar: () => void
}) {
  const bloquear = usuarioAdminApi.useBloquear()
  const [tipo, setTipo] = useState<'pago' | 'tecnico'>('pago')

  function cerrarYLimpiar() {
    setTipo('pago')
    bloquear.reset()
    onCerrar()
  }

  return (
    <Modal
      abierto={!!usuario}
      titulo="Tipo de desactivación"
      descripcion={usuario ? `Se bloqueará el usuario "${usuario.usuario}"` : undefined}
      onCerrar={cerrarYLimpiar}
    >
      <div className="space-y-3">
        <p className="text-sm text-slate-600">Seleccione el motivo del bloqueo:</p>
        {(
          [
            { valor: 'pago', etiqueta: 'Por falta de pago', descripcion: 'El usuario no ha regularizado el pago' },
            { valor: 'tecnico', etiqueta: 'Técnico', descripcion: 'Bloqueo por motivos técnicos del sistema' },
          ] as const
        ).map(({ valor, etiqueta, descripcion }) => (
          <label
            key={valor}
            className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-colors ${
              tipo === valor
                ? 'border-brand-600 bg-brand-50'
                : 'border-slate-200 hover:border-slate-300'
            }`}
          >
            <input
              type="radio"
              name="tipoBloqueo"
              value={valor}
              checked={tipo === valor}
              onChange={() => setTipo(valor)}
              className="mt-0.5 accent-brand-600"
            />
            <span>
              <span className="block text-sm font-semibold text-slate-800">{etiqueta}</span>
              <span className="block text-xs text-slate-500">{descripcion}</span>
            </span>
          </label>
        ))}

        {bloquear.error && (
          <div className="mt-3">
            <MensajeError error={bloquear.error} />
          </div>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton
            type="button"
            variante="peligro"
            cargando={bloquear.isPending}
            disabled={!usuario}
            onClick={() =>
              usuario &&
              bloquear.mutate({ idUsuario: usuario.idUsuario, tipoBloqueo: tipo }, {
                onSuccess: cerrarYLimpiar,
              })
            }
          >
            Bloquear usuario
          </Boton>
        </div>
      </div>
    </Modal>
  )
}

/**
 * Modal para editar todos los campos de un usuario (solo SuperUsuario).
 * La contraseña es opcional: si se deja vacía se conserva la actual
 * (el backend reutiliza el hash existente).
 */
export function ModalEditarUsuario({
  usuario,
  onCerrar,
}: {
  usuario: Usuario | null
  onCerrar: () => void
}) {
  const actualizar = usuarioAdminApi.useActualizar()
  const empresas = empresasApi.useLista()
  const roles = rolesApi.useLista()

  const [idEmpresa, setIdEmpresa] = useState<Uuid>('')
  const [nombreUsuario, setNombreUsuario] = useState('')
  const [nombres, setNombres] = useState('')
  const [idRol, setIdRol] = useState<number | ''>('')
  const [correo, setCorreo] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [activo, setActivo] = useState(true)

  // Sembrar el formulario cada vez que se abre con un usuario distinto.
  useEffect(() => {
    if (!usuario) return
    setIdEmpresa(usuario.idEmpresa)
    setNombreUsuario(usuario.usuario)
    setNombres(usuario.nombres)
    setIdRol(usuario.idRol)
    setCorreo(usuario.correo ?? '')
    setContrasena('')
    setActivo(usuario.activo)
    actualizar.reset()
    // `actualizar` es estable entre renders; no lo incluimos para no re-sembrar.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario])

  function cerrarYLimpiar() {
    actualizar.reset()
    onCerrar()
  }

  const usuarioInvalido = nombreUsuario.length > 0 && !PATRON_USUARIO.test(nombreUsuario)
  const nombresInvalidos = nombres.length > 0 && !PATRON_NOMBRE.test(nombres)
  const correoInvalido = correo.trim().length > 0 && !PATRON_CORREO.test(correo.trim())
  const contrasenaCorta = contrasena.length > 0 && contrasena.length < 6
  const formularioInvalido =
    !idEmpresa ||
    !nombreUsuario.trim() ||
    !nombres.trim() ||
    idRol === '' ||
    usuarioInvalido ||
    nombresInvalidos ||
    correoInvalido ||
    contrasenaCorta

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!usuario) return

    const datos: Partial<Usuario> = {
      idEmpresa,
      idRol: Number(idRol),
      usuario: nombreUsuario.trim(),
      nombres: nombres.trim(),
      correo: correo.trim() || null,
      activo,
      // Conservamos el motivo del bloqueo mientras siga inactivo; al activar
      // la cuenta se limpia (equivale a desbloquear).
      tipobloqueo: activo ? null : usuario.tipobloqueo,
      notificacion: usuario.notificacion ?? null,
    }

    if (contrasena.length > 0) datos.claveHash = contrasena

    actualizar.mutate(
      { idUsuario: usuario.idUsuario, datos },
      { onSuccess: cerrarYLimpiar },
    )
  }

  return (
    <Modal
      abierto={!!usuario}
      titulo="Editar usuario"
      descripcion={usuario ? `Cuenta "${usuario.usuario}"` : undefined}
      onCerrar={cerrarYLimpiar}
      ancho="max-w-xl"
    >
      <form className="space-y-4" onSubmit={enviar}>
        <Campo etiqueta="Veterinaria" requerido>
          <Select
            value={idEmpresa}
            onChange={(e) => setIdEmpresa(e.target.value)}
            required
          >
            <option value="">Seleccionar</option>
            {(empresas.data ?? []).map((emp) => (
              <option key={emp.idEmpresa} value={emp.idEmpresa}>
                {emp.razonSocial}
              </option>
            ))}
          </Select>
        </Campo>

        <div className="grid gap-4 sm:grid-cols-2">
          <Campo
            etiqueta="Usuario"
            requerido
            ayuda="Sin espacios: letras, números, punto, guion o guion bajo"
            error={usuarioInvalido ? 'Solo letras, números, punto, guion o guion bajo' : undefined}
          >
            <Input
              value={nombreUsuario}
              onChange={(e) => setNombreUsuario(limpiarUsuario(e.target.value))}
              maxLength={50}
              required
            />
          </Campo>

          <Campo
            etiqueta="Nombres"
            requerido
            ayuda="Solo letras y espacios"
            error={nombresInvalidos ? 'Solo se permiten letras y espacios, sin números' : undefined}
          >
            <Input
              value={nombres}
              onChange={(e) => setNombres(limpiarNombre(e.target.value))}
              maxLength={100}
              required
            />
          </Campo>

          <Campo etiqueta="Rol" requerido>
            <Select
              value={idRol}
              onChange={(e) => setIdRol(e.target.value === '' ? '' : Number(e.target.value))}
              required
            >
              <option value="">Seleccionar</option>
              {(roles.data ?? []).map((r) => (
                <option key={r.idRol} value={r.idRol}>
                  {r.nombre}
                </option>
              ))}
            </Select>
          </Campo>

          <Campo etiqueta="Activo" requerido ayuda="Determina si la cuenta puede iniciar sesión">
            <Select
              value={activo ? 'true' : 'false'}
              onChange={(e) => setActivo(e.target.value === 'true')}
            >
              <option value="true">Sí</option>
              <option value="false">No</option>
            </Select>
          </Campo>
        </div>

        <Campo
          etiqueta="Correo"
          ayuda="Opcional. Se usa para el enlace de confirmación de la cuenta"
          error={correoInvalido ? 'Ingresa un correo electrónico válido' : undefined}
        >
          <Input
            type="email"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            maxLength={100}
            placeholder="usuario@gmail.com"
          />
        </Campo>

        <Campo
          etiqueta="Nueva contraseña"
          ayuda="Déjala vacía para conservar la contraseña actual (mínimo 6 caracteres)"
          error={contrasenaCorta ? 'La contraseña debe tener al menos 6 caracteres' : undefined}
        >
          <InputContrasena
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
            maxLength={255}
            autoComplete="new-password"
          />
        </Campo>

        {usuario?.estadoCuenta === 'pendiente' && (
          <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700 ring-1 ring-amber-200">
            Esta cuenta aún no ha confirmado su correo; seguirá «Sin confirmar» hasta que
            abra el enlace enviado.
          </p>
        )}

        {actualizar.error && <MensajeError error={actualizar.error} />}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton type="submit" cargando={actualizar.isPending} disabled={formularioInvalido}>
            Guardar cambios
          </Boton>
        </div>
      </form>
    </Modal>
  )
}
