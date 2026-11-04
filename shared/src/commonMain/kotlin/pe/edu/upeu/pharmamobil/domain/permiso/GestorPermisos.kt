package pe.edu.upeu.pharmamobil.domain.permiso

enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso            // se puede volver a pedir
    data object DenegadoParaSiempre : EstadoPermiso // solo queda Ajustes
}

/**
 * Es una interfaz y no expect/actual porque la implementacion de Android
 * necesita la Activity para lanzar el dialogo del sistema.
 */
interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}
