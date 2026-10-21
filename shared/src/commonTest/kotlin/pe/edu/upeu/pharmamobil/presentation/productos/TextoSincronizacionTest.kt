package pe.edu.upeu.pharmamobil.presentation.productos

import kotlin.test.Test
import kotlin.test.assertEquals

class TextoSincronizacionTest {

    private val minuto = 60_000L
    private val hora = 60 * minuto

    /** Formato falso: el "día" es el cociente entre 24 h, la hora es fija. */
    private val formato: (Long, String) -> String = { millis, patron ->
        when (patron) {
            "yyyyMMdd" -> (millis / (24 * hora)).toString()
            "HH:mm" -> "10:30"
            else -> "21/10"
        }
    }

    private fun texto(ultima: Long?, ahora: Long) = textoUltimaSincronizacion(ultima, ahora, formato)

    @Test
    fun sinSincronizacionPreviaDiceNuncaSincronizado() =
        assertEquals("Nunca sincronizado", texto(null, 1_000L))

    @Test
    fun menosDeUnMinutoDiceHaceUnMomento() =
        assertEquals("Actualizado hace un momento", texto(100 * hora, 100 * hora + 30_000))

    @Test
    fun enMinutosDiceHaceXMinutos() {
        assertEquals("Actualizado hace 1 minuto", texto(100 * hora, 100 * hora + minuto))
        assertEquals("Actualizado hace 5 minutos", texto(100 * hora, 100 * hora + 5 * minuto))
    }

    @Test
    fun elMismoDiaDiceHoyALasHoraMinuto() =
        assertEquals("Actualizado hoy a las 10:30", texto(97 * hora, 101 * hora))

    @Test
    fun deOtroDiaMuestraLaFecha() =
        assertEquals("Actualizado el 21/10 a las 10:30", texto(24 * hora, 60 * hora))
}
