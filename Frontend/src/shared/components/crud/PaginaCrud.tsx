import { useMemo, useState } from 'react'
import { Info, Pencil, Plus, Trash2 } from 'lucide-react'
import type { ColumnDef } from '@tanstack/react-table'
import { TablaDatos } from '../TablaDatos'
import { Boton, CabeceraPagina, Cargando, MensajeError, Modal } from '../ui'
import { FormularioCrud } from './FormularioCrud'
import { esSuperUsuario, filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { ConfiguracionCrud } from './tipos'

/**
 * Página CRUD completa a partir de una configuración declarativa.
 *
 * Cubre listado + búsqueda + alta + edición + baja con confirmación.
 * Los módulos con lógica de negocio real (citas, consulta clínica,
 * facturación) NO usan esto: tienen su propia pantalla.
 */
export function PaginaCrud<T extends object>({
  titulo,
  singular,
  descripcion,
  api,
  campoId,
  campos,
  columnas,
  multiEmpresa,
  valoresBase,
  cargando,
  bloqueoGestion,
}: ConfiguracionCrud<T>) {
  const idEmpresa = useSesion((s) => s.idEmpresa)
  const rol = useSesion((s) => s.rol)
  // El SuperUsuario no tiene una empresa propia: su token trae un UUID
  // placeholder ("00000000-...") en vez de null (ver AuthService). Si se
  // tratara como una empresa real aquí, se filtrarían de la lista TODOS los
  // registros reales (ninguno pertenece a ese id) y se le asignaría ese
  // tenant falso a cualquier alta — el backend ya le devuelve/permite ver
  // todo sin filtrar (esSuperUsuario en cada Servlet), así que en el
  // cliente simplemente no se aplica ni el filtro ni la inyección.
  const esSuper = esSuperUsuario(rol)

  const [modalAbierto, setModalAbierto] = useState(false)
  const [registroEditado, setRegistroEditado] = useState<T | null>(null)
  const [registroAEliminar, setRegistroAEliminar] = useState<T | null>(null)

  const lista = api.useLista()
  const crear = api.useCrear()
  const actualizar = api.useActualizar()
  const eliminar = api.useEliminar()

  const datos = useMemo(() => {
    const registros = (lista.data ?? []) as T[]
    return multiEmpresa && !esSuper
      ? (filtrarPorEmpresa(registros as { idEmpresa?: string }[], idEmpresa) as T[])
      : registros
  }, [lista.data, multiEmpresa, idEmpresa, esSuper])

  const columnasConAcciones = useMemo<ColumnDef<T, unknown>[]>(
    () => [
      ...columnas,
      {
        id: 'acciones',
        header: '',
        enableSorting: false,
        cell: ({ row }) => {
          // Si la fila no es gestionable, las acciones se muestran
          // deshabilitadas (con el motivo en el tooltip) en vez de un botón
          // que el backend rechazará con un 403.
          const motivo = bloqueoGestion ? bloqueoGestion(row.original) : null
          const bloqueado = motivo !== null

          return (
            <div className="flex justify-end gap-1">
              <button
                onClick={() => {
                  setRegistroEditado(row.original)
                  setModalAbierto(true)
                }}
                disabled={bloqueado}
                title={motivo ?? undefined}
                className={
                  bloqueado
                    ? 'cursor-not-allowed rounded-lg p-1.5 text-slate-300'
                    : 'rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 hover:text-brand-700'
                }
                aria-label={`Editar ${singular}`}
              >
                <Pencil className="h-4 w-4" />
              </button>
              <button
                onClick={() => setRegistroAEliminar(row.original)}
                disabled={bloqueado}
                title={motivo ?? undefined}
                className={
                  bloqueado
                    ? 'cursor-not-allowed rounded-lg p-1.5 text-slate-300'
                    : 'rounded-lg p-1.5 text-slate-500 hover:bg-red-50 hover:text-red-600'
                }
                aria-label={`Eliminar ${singular}`}
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          )
        },
      } as ColumnDef<T, unknown>,
    ],
    [columnas, singular, bloqueoGestion],
  )

  function abrirCreacion() {
    setRegistroEditado(null)
    crear.reset()
    actualizar.reset()
    setModalAbierto(true)
  }

  function cerrarModal() {
    setModalAbierto(false)
    setRegistroEditado(null)
  }

  function enviar(datosFormulario: Partial<T>) {
    if (registroEditado) {
      actualizar.mutate(
        {
          id: registroEditado[campoId] as string | number,
          // Se conserva el registro original para no perder campos que el
          // formulario no expone (p. ej. claveHash o idEmpresa).
          datos: { ...registroEditado, ...datosFormulario },
        },
        { onSuccess: cerrarModal },
      )
    } else {
      const payload: Record<string, unknown> = { ...valoresBase, ...datosFormulario }
      // Si es SuperUsuario no se envía idEmpresa: el backend lo exige
      // explícito y estas pantallas no tienen selector de empresa, así que
      // es preferible que rechace la alta con un mensaje claro a que quede
      // asignada silenciosamente al tenant placeholder del SuperUsuario.
      if (multiEmpresa && idEmpresa && !esSuper) payload.idEmpresa = idEmpresa
      crear.mutate(payload as Partial<T>, { onSuccess: cerrarModal })
    }
  }

  // El SuperUsuario ve todos los registros de todas las veterinarias, pero
  // el backend rechaza la creación de estos recursos sin una empresa
  // concreta (ver comentario más arriba): en vez de dejar que el formulario
  // falle con un error de validación críptico, se oculta el alta.
  const creacionBloqueadaPorSuper = multiEmpresa && esSuper

  // Se espera también a los catálogos auxiliares (`cargando`): si no, la tabla
  // se pinta con las columnas de relaciones vacías y estas se rellenan un
  // instante después, que es justo el parpadeo que se quiere evitar.
  if (lista.isLoading || cargando) return <Cargando />

  return (
    <>
      <CabeceraPagina
        titulo={titulo}
        descripcion={descripcion}
        acciones={
          creacionBloqueadaPorSuper ? undefined : (
            <Boton onClick={abrirCreacion}>
              <Plus className="h-4 w-4" />
              Nuevo
            </Boton>
          )
        }
      />

      {creacionBloqueadaPorSuper && (
        <div className="mb-4 flex gap-2 rounded-lg border border-acento-200 bg-acento-50 p-3 text-xs text-acento-700">
          <Info className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          <p>
            Como SuperUsuario puedes consultar los registros de todas las veterinarias, pero
            crear un nuevo {singular} requiere iniciar sesión con un usuario que pertenezca a una
            veterinaria específica.
          </p>
        </div>
      )}

      {lista.isError && (
        <div className="mb-4">
          <MensajeError error={lista.error} />
        </div>
      )}

      <TablaDatos
        datos={datos}
        columnas={columnasConAcciones}
        buscarPlaceholder={`Buscar en ${titulo.toLowerCase()}…`}
      />

      <Modal
        abierto={modalAbierto}
        titulo={registroEditado ? `Editar ${singular}` : `Nuevo ${singular}`}
        onCerrar={cerrarModal}
        ancho="max-w-2xl"
      >
        <FormularioCrud<T>
          campos={campos}
          valoresIniciales={registroEditado ?? undefined}
          edicion={Boolean(registroEditado)}
          enviando={crear.isPending || actualizar.isPending}
          error={crear.error ?? actualizar.error}
          onEnviar={enviar}
          onCancelar={cerrarModal}
        />
      </Modal>

      <Modal
        abierto={Boolean(registroAEliminar)}
        titulo={`Eliminar ${singular}`}
        onCerrar={() => setRegistroAEliminar(null)}
      >
        <p className="text-sm text-slate-600">
          Esta acción no se puede deshacer. Si el registro está referenciado por otros datos, el
          servidor rechazará la operación.
        </p>
        {eliminar.error && (
          <div className="mt-3">
            <MensajeError error={eliminar.error} />
          </div>
        )}
        <div className="mt-5 flex justify-end gap-2">
          <Boton variante="secundario" onClick={() => setRegistroAEliminar(null)}>
            Cancelar
          </Boton>
          <Boton
            variante="peligro"
            cargando={eliminar.isPending}
            onClick={() =>
              registroAEliminar &&
              eliminar.mutate(registroAEliminar[campoId] as string | number, {
                onSuccess: () => setRegistroAEliminar(null),
              })
            }
          >
            Eliminar
          </Boton>
        </div>
      </Modal>
    </>
  )
}
