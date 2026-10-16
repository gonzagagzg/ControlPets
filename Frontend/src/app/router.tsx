import { Navigate, createBrowserRouter } from 'react-router-dom'
import { AppLayout } from './layout/AppLayout'
import { PaginaInicio } from '@/features/inicio/PaginaInicio'
import { PaginaAcceso } from '@/features/acceso/PaginaAcceso'
import { PaginaRecuperarPassword } from '@/features/acceso/PaginaRecuperarPassword'
import { PaginaRestablecerPassword } from '@/features/acceso/PaginaRestablecerPassword'
import { PaginaConfirmarCuenta } from '@/features/acceso/PaginaConfirmarCuenta'
import { PaginaPanelInicio } from '@/features/panel/PaginaPanelInicio'
import { PaginaCitas } from '@/features/citas/PaginaCitas'
import { PaginaClientes } from '@/features/clientes/PaginaClientes'
import { PaginaMascotas } from '@/features/mascotas/PaginaMascotas'
import { PaginaHistoriales } from '@/features/historiales/PaginaHistoriales'
import { PaginaVacunacion } from '@/features/vacunacion/PaginaVacunacion'
import { PaginaRecetas } from '@/features/recetas/PaginaRecetas'
import { PaginaProductos } from '@/features/productos/PaginaProductos'
import { PaginaInventario } from '@/features/inventario/PaginaInventario'
import { PaginaFacturas } from '@/features/facturacion/PaginaFacturas'
import { PaginaCatalogos } from '@/features/catalogos/PaginaCatalogos'
import { PaginaUsuarios } from '@/features/admin/PaginaUsuarios'
import { PaginaVeterinarios } from '@/features/admin/PaginaVeterinarios'
import { PaginaAccesos } from '@/features/admin/PaginaAccesos'
import { PaginaEmpresas } from '@/features/admin/PaginaEmpresas'
import { PaginaMensualidades } from '@/features/mensualidades/PaginaMensualidades'
import { esSuperUsuario, useSesion } from '@/shared/session/sesion'
import { alExpirarSesion } from '@/shared/api/client'

/**
 * Redirige al selector de contexto si no hay sesión JWT válida.
 * El control real de "válido" lo hace igualmente el backend en cada
 * petición (AuthFilter) — esto solo evita el parpadeo de pantallas
 * protegidas antes de que la primera petición dispare el 401.
 */
function RequiereSesion({ children }: { children: React.ReactNode }) {
  const token = useSesion((s) => s.token)
  if (!token) return <Navigate to="/acceso" replace />
  return <>{children}</>
}

/**
 * `/api/empresas` está reservado al rol SuperUsuario en el backend
 * (EmpresaServlet.exigirSuperUsuario). Se replica aquí para no exponer
 * la pantalla a quien de todas formas recibiría 403 en cada petición.
 */
function RequiereSuperUsuario({ children }: { children: React.ReactNode }) {
  const rol = useSesion((s) => s.rol)
  if (!esSuperUsuario(rol)) return <Navigate to="/app" replace />
  return <>{children}</>
}

export const router = createBrowserRouter([
  { path: '/', element: <PaginaInicio /> },
  { path: '/acceso', element: <PaginaAcceso /> },
  { path: '/recuperar-password', element: <PaginaRecuperarPassword /> },
  { path: '/restablecer-password', element: <PaginaRestablecerPassword /> },
  { path: '/confirmar-cuenta', element: <PaginaConfirmarCuenta /> },
  {
    path: '/app',
    element: (
      <RequiereSesion>
        <AppLayout />
      </RequiereSesion>
    ),
    children: [
      { index: true, element: <PaginaPanelInicio /> },
      { path: 'citas', element: <PaginaCitas /> },
      { path: 'clientes', element: <PaginaClientes /> },
      { path: 'mascotas', element: <PaginaMascotas /> },
      { path: 'historiales', element: <PaginaHistoriales /> },
      { path: 'vacunacion', element: <PaginaVacunacion /> },
      { path: 'recetas', element: <PaginaRecetas /> },
      { path: 'productos', element: <PaginaProductos /> },
      { path: 'inventario', element: <PaginaInventario /> },
      { path: 'facturas', element: <PaginaFacturas /> },
      { path: 'catalogos', element: <PaginaCatalogos /> },
      { path: 'usuarios', element: <PaginaUsuarios /> },
      { path: 'veterinarios', element: <PaginaVeterinarios /> },
      { path: 'accesos', element: <PaginaAccesos /> },
      { path: 'mensualidades', element: <PaginaMensualidades /> },
      {
        path: 'empresas',
        element: (
          <RequiereSuperUsuario>
            <PaginaEmpresas />
          </RequiereSuperUsuario>
        ),
      },
      { path: '*', element: <Navigate to="/app" replace /> },
    ],
  },
])

// 401 (token ausente/expirado) puede ocurrir fuera de un componente (ej. en
// una queryFn de React Query), así que el cliente HTTP no puede usar
// useNavigate directamente. Se registra una vez aquí, contra el router.
alExpirarSesion(() => router.navigate('/acceso', { replace: true }))
