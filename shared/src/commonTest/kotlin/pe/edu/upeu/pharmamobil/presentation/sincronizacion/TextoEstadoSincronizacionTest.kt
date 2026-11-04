package pe.edu.upeu.pharmamobil.presentation.sincronizacion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextoEstadoSincronizacionTest {

    @Test
    fun sinConexionDiceCuantosCambiosFaltan() {
        val e = SincronizacionUiState(pendientes = 3, sinConexion = true)
        assertEquals("Sin conexión · 3 cambios por enviar", textoEstadoSincronizacion(e))
    }

    @Test
    fun unSoloCambioVaEnSingular() {
        val e = SincronizacionUiState(pendientes = 1, sinConexion = true)
        assertEquals("Sin conexión · 1 cambio por enviar", textoEstadoSincronizacion(e))
    }

    @Test
    fun conConexionYPendientesSoloDiceCuantos() {
        assertEquals("2 cambios por enviar", textoEstadoSincronizacion(SincronizacionUiState(pendientes = 2)))
    }

    @Test
    fun mientrasSincronizaTienePrioridad() {
        val e = SincronizacionUiState(enCurso = true, pendientes = 2, sinConexion = true)
        assertEquals("Sincronizando…", textoEstadoSincronizacion(e))
    }

    @Test
    fun sinPendientesNiFallosTodoEstaSincronizado() {
        assertEquals("Todo sincronizado", textoEstadoSincronizacion(SincronizacionUiState()))
    }

    @Test
    fun lasFilasFallidasOfrecenUnMensajePropio() {
        assertNull(textoFallidas(0))
        assertEquals("1 cambio no se pudo enviar", textoFallidas(1))
        assertEquals("4 cambios no se pudieron enviar", textoFallidas(4))
    }
}
