package pe.edu.upeu.pharmamobil.domain.error

import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ErrorApiTest {

    @Test
    fun laCancelacionSeRelanzaSinTraducirseAErrorApi() = runTest {

        assertFailsWith<CancellationException> {
            CancellationException("scope cancelado").toErrorApi()
        }
    }

    @Test
    fun cancelarElScopeDuranteLaLlamadaNoProduceErrorApi() = runTest {

        val iniciada = CompletableDeferred<Unit>()
        var traducido: Throwable? = null

        val trabajo = launch {
            try {
                traducirErrores {
                    iniciada.complete(Unit)
                    CompletableDeferred<Unit>().await()
                }
            } catch (e: Throwable) {
                traducido = e
                throw e
            }
        }

        iniciada.await()
        trabajo.cancel()
        trabajo.join()

        assertTrue(trabajo.isCancelled)
        assertTrue(traducido is CancellationException)
    }

    @Test
    fun unFalloDeConexionSeTraduceASinConexion() = runTest {

        val error = Exception("Failed to connect to /10.0.2.2:8080").toErrorApi()

        assertEquals(ErrorApi.SinConexion, error)
    }

    @Test
    fun elTimeoutDeConexionSeTraduceATiempoAgotado() = runTest {

        val error = ConnectTimeoutException("timeout", null).toErrorApi()

        assertEquals(ErrorApi.TiempoAgotado, error)
    }
}
