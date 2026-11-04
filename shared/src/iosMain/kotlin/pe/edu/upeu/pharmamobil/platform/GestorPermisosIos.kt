package pe.edu.upeu.pharmamobil.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso

/**
 * Implementacion minima para iOS: la guia de la sesion 13 exige la de Android.
 * Los dialogos nativos (camara, ubicacion, notificaciones) se completan en la
 * sesion 14 con un Mac; por ahora solo se puede llevar al usuario a Ajustes.
 */
class GestorPermisosIos : GestorPermisos {
    override suspend fun estado(permiso: Permiso): EstadoPermiso = EstadoPermiso.Denegado

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = EstadoPermiso.Denegado

    override fun abrirAjustes() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }
}
