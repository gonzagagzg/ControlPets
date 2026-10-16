import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { calcularEdad, formatearFecha, indexarPor } from '@/shared/lib/utils'
import { especiesApi, mascotasApi, razasApi } from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import type { Mascota } from '@/shared/types/api'

export function PaginaMascotas() {
  const clientes = useClientesTodos()
  const razas = razasApi.useLista()
  const especies = especiesApi.useLista()

  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])
  const porRaza = useMemo(() => indexarPor(razas.data, 'idRaza'), [razas.data])
  const porEspecie = useMemo(() => indexarPor(especies.data, 'idEspecie'), [especies.data])

  const columnas = useMemo<ColumnDef<Mascota, unknown>[]>(
    () => [
      { accessorKey: 'nombre', header: 'Nombre' },
      {
        id: 'especie',
        header: 'Especie',
        accessorFn: (m) => {
          const raza = porRaza.get(String(m.idRaza))
          return raza ? (porEspecie.get(String(raza.idEspecie))?.nombre ?? '—') : '—'
        },
      },
      {
        id: 'raza',
        header: 'Raza',
        accessorFn: (m) => porRaza.get(String(m.idRaza))?.nombre ?? '—',
      },
      {
        id: 'cliente',
        header: 'Propietario',
        accessorFn: (m) => porCliente.get(m.idCliente)?.nombres ?? '—',
      },
      {
        id: 'nacimiento',
        header: 'Nacimiento',
        accessorFn: (m) => formatearFecha(m.fechaNacimiento),
      },
      {
        id: 'edad',
        header: 'Edad',
        accessorFn: (m) => calcularEdad(m.fechaNacimiento),
      },
    ],
    [porCliente, porRaza, porEspecie],
  )

  // El backend ya filtra por empresa activa a partir del token (ver
  // ClienteServlet), así que no hace falta re-filtrar aquí.
  const clientesEmpresa = clientes.data ?? []

  return (
    <PaginaCrud<Mascota>
      titulo="Mascotas"
      singular="mascota"
      descripcion="Pacientes de la clínica"
      api={mascotasApi}
      campoId="idMascota"
      multiEmpresa
      cargando={clientes.isLoading || razas.isLoading || especies.isLoading}
      columnas={columnas}
      campos={[
        {
          nombre: 'nombre',
          etiqueta: 'Nombre',
          tipo: 'texto',
          requerido: true,
          maxLength: 50,
          formato: 'nombre',
        },
        {
          nombre: 'idCliente',
          etiqueta: 'Propietario',
          tipo: 'select',
          requerido: true,
          opciones: clientesEmpresa.map((c) => ({
            valor: c.idCliente,
            etiqueta: `${c.nombres} — ${c.identificacion}`,
          })),
        },
        {
          nombre: 'idRaza',
          etiqueta: 'Raza',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (razas.data ?? []).map((r) => ({
            valor: r.idRaza,
            etiqueta: `${porEspecie.get(String(r.idEspecie))?.nombre ?? '?'} · ${r.nombre}`,
          })),
        },
        { nombre: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'fecha', maxHoy: true },
      ]}
    />
  )
}
