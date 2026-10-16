import { useEsSuperUsuario } from '@/shared/session/sesion'
import { PaginaPanel } from './PaginaPanel'
import { PaginaPanelSuperUsuario } from './PaginaPanelSuperUsuario'

/**
 * Punto de entrada de /app. El rol decide el panel:
 *  - SuperUsuario  -> panel estadístico global de la plataforma.
 *  - resto de roles -> panel operativo de su veterinaria.
 */
export function PaginaPanelInicio() {
  return useEsSuperUsuario() ? <PaginaPanelSuperUsuario /> : <PaginaPanel />
}
