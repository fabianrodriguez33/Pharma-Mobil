package pe.edu.upeu.pharmamobil.presentation.sincronizacion

/** Estado de la cola de sincronizacion; se arma solo con consultas de la base local. */
data class SincronizacionUiState(
    val enCurso: Boolean = false,
    val pendientes: Int = 0,
    val fallidas: Int = 0,
    val ultimaSync: Long? = null,   // epoch en milisegundos
    val sinConexion: Boolean = false
)

private fun cambios(n: Int) = if (n == 1) "1 cambio" else "$n cambios"

/** «Sin conexión · 3 cambios por enviar» dice más que un icono rojo. */
fun textoEstadoSincronizacion(e: SincronizacionUiState): String = when {
    e.enCurso -> "Sincronizando…"
    e.sinConexion && e.pendientes > 0 -> "Sin conexión · ${cambios(e.pendientes)} por enviar"
    e.sinConexion -> "Sin conexión · mostrando datos guardados"
    e.pendientes > 0 -> "${cambios(e.pendientes)} por enviar"
    else -> "Todo sincronizado"
}

/** Null cuando no hay filas fallidas. */
fun textoFallidas(fallidas: Int): String? = when {
    fallidas <= 0 -> null
    fallidas == 1 -> "1 cambio no se pudo enviar"
    else -> "$fallidas cambios no se pudieron enviar"
}
