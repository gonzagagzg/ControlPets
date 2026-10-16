import { useMemo, useState } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { CabeceraPagina } from '@/shared/components/ui'
import { cn, indexarPor } from '@/shared/lib/utils'
import { categoriasApi, especiesApi, razasApi, sriIvaApi, vacunasApi } from '@/shared/api/recursos'
import type { Categoria, Especie, Raza, SriIva, Vacuna } from '@/shared/types/api'

/**
 * Los cinco catálogos comparten pantalla mediante pestañas: son tablas cortas
 * y de baja frecuencia de uso; darles una entrada de menú a cada una satura
 * la navegación sin aportar nada.
 */

const PESTANIAS = ['Especies', 'Razas', 'Vacunas', 'Categorías', 'IVA (SRI)'] as const
type Pestania = (typeof PESTANIAS)[number]

export function PaginaCatalogos() {
  const [activa, setActiva] = useState<Pestania>('Especies')

  return (
    <>
      <CabeceraPagina
        titulo="Catálogos"
        descripcion="Tablas maestras que alimentan el resto de módulos"
      />

      <div className="mb-6 flex gap-1 border-b border-slate-200">
        {PESTANIAS.map((p) => (
          <button
            key={p}
            onClick={() => setActiva(p)}
            className={cn(
              '-mb-px border-b-2 px-3.5 py-2 text-sm font-medium transition-colors',
              activa === p
                ? 'border-brand-600 text-brand-700'
                : 'border-transparent text-slate-500 hover:text-slate-800',
            )}
          >
            {p}
          </button>
        ))}
      </div>

      {activa === 'Especies' && <TabEspecies />}
      {activa === 'Razas' && <TabRazas />}
      {activa === 'Vacunas' && <TabVacunas />}
      {activa === 'Categorías' && <TabCategorias />}
      {activa === 'IVA (SRI)' && <TabIva />}
    </>
  )
}

function TabEspecies() {
  const columnas = useMemo<ColumnDef<Especie, unknown>[]>(
    () => [
      { accessorKey: 'idEspecie', header: 'ID', size: 60 },
      { accessorKey: 'nombre', header: 'Nombre' },
    ],
    [],
  )

  return (
    <PaginaCrud<Especie>
      titulo="Especies"
      singular="especie"
      api={especiesApi}
      campoId="idEspecie"
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
      ]}
    />
  )
}

function TabRazas() {
  const especies = especiesApi.useLista()
  const porEspecie = useMemo(() => indexarPor(especies.data, 'idEspecie'), [especies.data])

  const columnas = useMemo<ColumnDef<Raza, unknown>[]>(
    () => [
      { accessorKey: 'idRaza', header: 'ID', size: 60 },
      { accessorKey: 'nombre', header: 'Raza' },
      {
        id: 'especie',
        header: 'Especie',
        accessorFn: (r) => porEspecie.get(String(r.idEspecie))?.nombre ?? '—',
      },
    ],
    [porEspecie],
  )

  return (
    <PaginaCrud<Raza>
      titulo="Razas"
      singular="raza"
      api={razasApi}
      campoId="idRaza"
      cargando={especies.isLoading}
      columnas={columnas}
      campos={[
        {
          nombre: 'idEspecie',
          etiqueta: 'Especie',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (especies.data ?? []).map((e) => ({
            valor: e.idEspecie,
            etiqueta: e.nombre,
          })),
        },
        {
          nombre: 'nombre',
          etiqueta: 'Nombre',
          tipo: 'texto',
          requerido: true,
          maxLength: 50,
          formato: 'nombre',
        },
      ]}
    />
  )
}

function TabVacunas() {
  const columnas = useMemo<ColumnDef<Vacuna, unknown>[]>(
    () => [
      { accessorKey: 'idVacuna', header: 'ID', size: 60 },
      { accessorKey: 'nombre', header: 'Nombre' },
    ],
    [],
  )

  return (
    <PaginaCrud<Vacuna>
      titulo="Vacunas"
      singular="vacuna"
      api={vacunasApi}
      campoId="idVacuna"
      columnas={columnas}
      campos={[
        { nombre: 'nombre', etiqueta: 'Nombre', tipo: 'texto', requerido: true, maxLength: 100 },
      ]}
    />
  )
}

function TabCategorias() {
  const columnas = useMemo<ColumnDef<Categoria, unknown>[]>(
    () => [
      { accessorKey: 'idCategoria', header: 'ID', size: 60 },
      { accessorKey: 'nombre', header: 'Nombre' },
    ],
    [],
  )

  return (
    <PaginaCrud<Categoria>
      titulo="Categorías de producto"
      singular="categoría"
      api={categoriasApi}
      campoId="idCategoria"
      multiEmpresa
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
      ]}
    />
  )
}

function TabIva() {
  const columnas = useMemo<ColumnDef<SriIva, unknown>[]>(
    () => [
      { accessorKey: 'idIva', header: 'ID', size: 60 },
      {
        id: 'porcentaje',
        header: 'Porcentaje',
        accessorFn: (r) => `${Number(r.porcentaje).toFixed(2)} %`,
      },
      { accessorKey: 'codigoSri', header: 'Código SRI' },
    ],
    [],
  )

  return (
    <PaginaCrud<SriIva>
      titulo="Tarifas de IVA"
      singular="tarifa"
      descripcion="Códigos oficiales del SRI usados en la facturación"
      api={sriIvaApi}
      campoId="idIva"
      columnas={columnas}
      campos={[
        {
          nombre: 'porcentaje',
          etiqueta: 'Porcentaje',
          tipo: 'decimal',
          requerido: true,
          min: 0,
          max: 100,
        },
        { nombre: 'codigoSri', etiqueta: 'Código SRI', tipo: 'texto', maxLength: 10, formato: 'digitos' },
      ]}
    />
  )
}
