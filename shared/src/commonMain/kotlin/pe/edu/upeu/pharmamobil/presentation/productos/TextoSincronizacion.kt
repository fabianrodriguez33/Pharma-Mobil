package pe.edu.upeu.pharmamobil.presentation.productos

import pe.edu.upeu.pharmamobil.platform.formatearEpoch

private const val MINUTO = 60_000L
private const val HORA = 60 * MINUTO

/** Texto del indicador de última sincronización; [formato] se inyecta para poder probarlo. */
fun textoUltimaSincronizacion(
    ultima: Long?,
    ahora: Long,
    formato: (Long, String) -> String = ::formatearEpoch
): String {
    if (ultima == null) return "Nunca sincronizado"
    val diferencia = (ahora - ultima).coerceAtLeast(0L)
    return when {
        diferencia < MINUTO -> "Actualizado hace un momento"
        diferencia < HORA -> {
            val minutos = diferencia / MINUTO
            "Actualizado hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
        }
        formato(ultima, "yyyyMMdd") == formato(ahora, "yyyyMMdd") ->
            "Actualizado hoy a las ${formato(ultima, "HH:mm")}"
        else -> "Actualizado el ${formato(ultima, "dd/MM")} a las ${formato(ultima, "HH:mm")}"
    }
}
