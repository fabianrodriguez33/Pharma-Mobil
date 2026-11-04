package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.EnvioRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private typealias Respuesta = MockRequestHandleScope.() -> HttpResponseData

/** Base SQLite en memoria y motor de red simulado: no hace falta red ni dispositivo. */
class SincronizacionTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val peticiones = mutableListOf<HttpMethod>()
    private val max = SincronizarUseCase.MAX_INTENTOS

    private fun fila(id: Long, nombre: String) =
        """{"id":$id,"nombre":"$nombre","precio":2.5,"stock":20,"estado":true}"""

    private fun pagina(vararg filas: String) =
        """{"contenido":[${filas.joinToString(",")}],"pagina":0,"tamanio":100,""" +
            """"totalElementos":${filas.size},"totalPaginas":1,"ultima":true}"""

    private fun crearBase(): PharmaDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        return PharmaDb(driver)
    }

    /** El POST responde con [alCrear]; el GET entrega [catalogo]. */
    private fun servidor(alCrear: Respuesta, catalogo: String = pagina()) = MockEngine { request ->
        peticiones += request.method
        when (request.method) {
            HttpMethod.Post -> alCrear(this)
            else -> respond(catalogo, HttpStatusCode.OK, json)
        }
    }

    private fun postFalla(estado: HttpStatusCode): Respuesta = { respondError(estado) }

    private fun postCrea(id: Long, nombre: String): Respuesta =
        { respond(fila(id, nombre), HttpStatusCode.Created, json) }

    private fun repositorio(base: PharmaDb, motor: MockEngine, esperas: MutableList<Long> = mutableListOf()) =
        ProductoRepositorioOffline(
            local = ProductoLocal(base),
            remoto = ProductoApi(crearHttpClient(motor, "http://localhost:8080/")),
            esperar = { esperas += it }
        )

    private val paracetamol = Producto(0L, "Paracetamol", 2.5, 20)

    @Test
    fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        val pendiente = local.guardarPendiente(paracetamol)
        val repo = repositorio(base, servidor(postFalla(HttpStatusCode.ServiceUnavailable)))

        repo.sincronizar()

        val fila = base.productoQueries.obtenerPorId(pendiente.id).executeAsOne()
        assertEquals(1L, fila.intentos)
        assertEquals(1L, fila.pendiente)
        assertNotNull(fila.ultimoError)
        assertEquals(0, local.observarFallidas(max).first())
        assertEquals(1, local.observarPendientes(max).first())
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        val pendiente = local.guardarPendiente(paracetamol)
        val repo = repositorio(base, servidor(postFalla(HttpStatusCode.Conflict)))

        repo.sincronizar()
        repo.sincronizar()

        val fila = base.productoQueries.obtenerPorId(pendiente.id).executeAsOne()
        assertEquals(max.toLong(), fila.intentos)
        assertEquals(1L, fila.pendiente)
        assertNotNull(fila.ultimoError)
        assertEquals(1, local.observarFallidas(max).first())
        assertEquals(0, local.observarPendientes(max).first())
        assertEquals(1, peticiones.count { it == HttpMethod.Post })
    }

    @Test
    fun laFilaQueAgotaSusIntentosQuedaVisibleYDejaDeReintentarse() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.guardarPendiente(paracetamol)
        val repo = repositorio(base, servidor(postFalla(HttpStatusCode.ServiceUnavailable)))

        repeat(max + 2) { repo.sincronizar() }

        assertEquals(max, peticiones.count { it == HttpMethod.Post })
        assertEquals(1, local.observarFallidas(max).first())
        val detalle = repo.fallidas()
        assertEquals(listOf("Paracetamol"), detalle.map { it.producto.nombre })
        assertEquals(max, detalle.single().intentos)
    }

    @Test
    fun laEsperaCreceConCadaIntentoYTieneTope() = runTest {
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L),
            (0..5).map { SincronizarUseCase.espera(it) }
        )

        val base = crearBase()
        ProductoLocal(base).guardarPendiente(paracetamol)
        val esperas = mutableListOf<Long>()
        val repo = repositorio(base, servidor(postFalla(HttpStatusCode.ServiceUnavailable)), esperas)

        repeat(3) { repo.sincronizar() }

        assertEquals(listOf(1_000L, 2_000L, 4_000L), esperas)
    }

    @Test
    fun laSubidaOcurreAntesQueLaBajadaYNadaSePierde() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        val pendiente = local.guardarPendiente(paracetamol)
        // El servidor no puede crear ahora y su catalogo no incluye el pendiente.
        val repo = repositorio(
            base,
            servidor(postFalla(HttpStatusCode.ServiceUnavailable), pagina(fila(1, "Aspirina")))
        )

        repo.sincronizar()

        assertEquals(listOf(HttpMethod.Post, HttpMethod.Get), peticiones)
        val ids = base.productoQueries.listarTodos().executeAsList().map { it.id }.toSet()
        assertEquals(setOf(pendiente.id, 1L), ids)
    }

    @Test
    fun reintentarLasFallidasLasDevuelveALaColaYLasEnvia() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.guardarPendiente(paracetamol)
        repositorio(base, servidor(postFalla(HttpStatusCode.Conflict))).sincronizar()
        assertEquals(1, local.observarFallidas(max).first())

        val repo = repositorio(
            base,
            servidor(postCrea(7, "Paracetamol"), pagina(fila(7, "Paracetamol")))
        )
        repo.reintentarFallidas()

        assertEquals(0, local.observarFallidas(max).first())
        assertEquals(0, local.observarPendientes(max).first())
        assertEquals(listOf(7L), base.productoQueries.listarTodos().executeAsList().map { it.id })
    }

    @Test
    fun elResumenCuentaEnviadasFallidasYPendientes() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.guardarPendiente(paracetamol)
        val motor = servidor(postCrea(7, "Paracetamol"), pagina(fila(7, "Paracetamol")))
        val caso = SincronizarUseCase(
            local = local,
            remoto = EnvioRepositorioRest(ProductoApi(crearHttpClient(motor, "http://localhost:8080/"))),
            esperar = {}
        )

        val resumen = caso()

        assertEquals(1, resumen.enviadas)
        assertEquals(0, resumen.fallidas)
        assertEquals(0, resumen.pendientes)
    }

    @Test
    fun sinRedElListadoFallaPeroLoPendienteSigueGuardado() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.guardarPendiente(paracetamol)
        val caido = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }
        val repo = repositorio(base, caido)

        assertFailsWith<ErrorApiException> { repo.sincronizar() }

        assertTrue(base.productoQueries.listarPendientes().executeAsList().isNotEmpty())
    }
}
