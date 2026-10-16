import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { indexarPor } from '@/shared/lib/utils'
import { useVeterinariosDisponibles, usuariosApi, veterinariosApi } from '@/shared/api/recursos'
import type { Veterinario } from '@/shared/types/api'

export function PaginaVeterinarios() {
  // GET /api/usuarios/veterinarios-disponibles ya filtra en el backend por
  // rol Veterinario, empresa del JWT y que no estén vinculados todavía: se
  // usa solo para poblar el selector de alta. Para mostrar nombre/usuario en
  // la tabla de veterinarios ya creados se necesita el listado completo,
  // porque esos usuarios ya no aparecen entre los "disponibles".
  const usuarios = usuariosApi.useLista()
  const usuariosDisponibles = useVeterinariosDisponibles()

  const porUsuario = useMemo(() => indexarPor(usuarios.data, 'idUsuario'), [usuarios.data])

  const columnas = useMemo<ColumnDef<Veterinario, unknown>[]>(
    () => [
      {
        id: 'nombre',
        header: 'Nombre',
        accessorFn: (v) => porUsuario.get(v.idUsuario)?.nombres ?? '—',
      },
      {
        id: 'usuario',
        header: 'Usuario',
        accessorFn: (v) => porUsuario.get(v.idUsuario)?.usuario ?? '—',
      },
      { accessorKey: 'codigoVeterinario', header: 'Código' },
      { accessorKey: 'especialidad', header: 'Especialidad' },
    ],
    [porUsuario],
  )

  return (
    <PaginaCrud<Veterinario>
      titulo="Veterinarios"
      singular="veterinario"
      descripcion="Profesionales que atienden las citas"
      api={veterinariosApi}
      campoId="idVeterinario"
      multiEmpresa
      cargando={usuarios.isLoading || usuariosDisponibles.isLoading}
      columnas={columnas}
      campos={[
        {
          nombre: 'idUsuario',
          etiqueta: 'Usuario asociado',
          tipo: 'select',
          requerido: true,
          soloCreacion: true,
          ayuda: 'Solo usuarios con rol Veterinario de tu empresa, aún sin vincular',
          opciones: (usuariosDisponibles.data ?? []).map((u) => ({
            valor: u.idUsuario,
            etiqueta: `${u.nombres} (${u.usuario})`,
          })),
        },
        {
          nombre: 'codigoVeterinario',
          etiqueta: 'Código de veterinario',
          tipo: 'texto',
          requerido: true,
          maxLength: 50,
          formato: 'codigo',
          placeholder: 'VET-001',
          ayuda: 'Único dentro de esta veterinaria. Solo letras, números y guiones',
        },
        {
          nombre: 'especialidad',
          etiqueta: 'Especialidad',
          tipo: 'texto',
          maxLength: 100,
          formato: 'nombre',
          placeholder: 'Medicina interna, cirugía…',
        },
      ]}
    />
  )
}
