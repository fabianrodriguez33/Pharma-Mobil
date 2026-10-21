package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Cada prueba crea su propia base SQLite en memoria: no comparten estado. */
class ProductoRepositorioOfflineTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val metodos = mutableListOf<HttpMethod>()

    private fun pagina(vararg filas: String) =
        """{"contenido":[${filas.joinToString(",")}],"pagina":0,"tamanio":100,""" +
            """"totalElementos":${filas.size},"totalPaginas":1,"ultima":true}"""

    private fun fila(id: Long, nombre: String) =
        """{"id":$id,"nombre":"$nombre","precio":2.5,"stock":20,"estado":true}"""

    private fun crearBase(): PharmaDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        return PharmaDb(driver)
    }

    private fun repositorio(base: PharmaDb, motor: MockEngine, ahora: () -> Long = { 1_000L }) =
        ProductoRepositorioOffline(
            local = ProductoLocal(base),
            remoto = ProductoApi(crearHttpClient(motor, "http://localhost:8080/")),
            ahora = ahora
        )

    private val servidorCaido = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }

    /** Servidor sano: acepta el POST (id 7) y lista [catalogo] en el GET. */
    private fun servidorSano(vararg catalogo: String) = MockEngine { request ->
        metodos += request.method
        when (request.method) {
            HttpMethod.Post -> respond(fila(7, "Paracetamol"), HttpStatusCode.Created, json)
            else -> respond(pagina(*catalogo), HttpStatusCode.OK, json)
        }
    }

    @Test
    fun prueba1_sincronizarConRedGuardaLosRemotosYObservarLosEmite() = runTest {
        val base = crearBase()
        val repo = repositorio(base, servidorSano(fila(1, "Aspirina"), fila(2, "Ibuprofeno")))

        repo.sincronizar()

        assertEquals(2, base.productoQueries.listarTodos().executeAsList().size)
        assertEquals(listOf("Aspirina", "Ibuprofeno"), repo.observar().first().map { it.nombre })
    }

    @Test
    fun prueba2_registrarSinRedGuardaIdNegativoPendienteYElFlowLoEmite() = runTest {
        val base = crearBase()
        val repo = repositorio(base, servidorCaido)

        val guardado = repo.registrar(Producto(0L, "Paracetamol", 2.5, 20))

        assertTrue(guardado.id < 0)
        val pendientes = base.productoQueries.listarPendientes().executeAsList()
        assertEquals(1, pendientes.size)
        assertEquals(1L, pendientes.single().pendiente)
        assertEquals(listOf("Paracetamol"), repo.observar().first().map { it.nombre })
    }

    @Test
    fun prueba3_alReconectarSubeElPendienteYQuedaConElIdDelServidor() = runTest {
        val base = crearBase()
        repositorio(base, servidorCaido).registrar(Producto(0L, "Paracetamol", 2.5, 20))

        repositorio(base, servidorSano(fila(7, "Paracetamol"))).sincronizar()

        assertEquals(listOf(HttpMethod.Post, HttpMethod.Get), metodos)
        assertTrue(base.productoQueries.listarPendientes().executeAsList().isEmpty())
        val filas = base.productoQueries.listarTodos().executeAsList()
        assertEquals(listOf(7L), filas.map { it.id })
        assertEquals(0L, filas.single().pendiente)
    }

    @Test
    fun prueba4_laBajadaNoBorraLoPendiente() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.reemplazarSincronizados(listOf(Producto(1L, "Viejo", 1.0, 5)))
        val pendiente = local.guardarPendiente(Producto(0L, "Creado sin red", 2.0, 3))

        local.reemplazarSincronizados(listOf(Producto(2L, "Nuevo", 1.0, 5)))

        val filas = base.productoQueries.listarTodos().executeAsList()
        assertEquals(setOf("Creado sin red", "Nuevo"), filas.map { it.nombre }.toSet())
        assertEquals(listOf(pendiente.id), base.productoQueries.listarPendientes().executeAsList().map { it.id })
    }

    @Test
    fun prueba5_idsNegativosYPositivosConviven() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        val a = local.guardarPendiente(Producto(0L, "Local A", 1.0, 1))
        val b = local.guardarPendiente(Producto(0L, "Local B", 1.0, 1))

        local.reemplazarSincronizados(listOf(Producto(1L, "Remoto 1", 1.0, 1), Producto(2L, "Remoto 2", 1.0, 1)))

        assertEquals(listOf(-1L, -2L), listOf(a.id, b.id))
        assertEquals(listOf(-2L, -1L, 1L, 2L), base.productoQueries.listarTodos().executeAsList().map { it.id }.sorted())
    }

    @Test
    fun prueba6_sincronizarDosVecesNoDuplicaRegistros() = runTest {
        val base = crearBase()
        val repo = repositorio(base, servidorSano(fila(1, "Aspirina"), fila(2, "Ibuprofeno")))

        repo.sincronizar()
        repo.sincronizar()

        assertEquals(2, base.productoQueries.listarTodos().executeAsList().size)
    }

    @Test
    fun conElServidorFueraDeLineaObservarEmiteLosDatosGuardados() = runTest {
        val base = crearBase()
        ProductoLocal(base).reemplazarSincronizados(listOf(Producto(1L, "Aspirina", 1.0, 5)))
        val repo = repositorio(base, servidorCaido)

        assertFailsWith<Exception> { repo.sincronizar() }

        assertEquals(listOf("Aspirina"), repo.observar().first().map { it.nombre })
    }

    @Test
    fun laSincronizacionExitosaGuardaSuMarcaDeTiempoYLaFallidaNo() = runTest {
        val base = crearBase()
        assertEquals(null, repositorio(base, servidorCaido).ultimaSincronizacion().first())

        assertFailsWith<Exception> { repositorio(base, servidorCaido).sincronizar() }
        assertEquals(null, repositorio(base, servidorCaido).ultimaSincronizacion().first())

        val repo = repositorio(base, servidorSano(fila(1, "Aspirina")), ahora = { 1_700_000_000_000L })
        repo.sincronizar()
        assertEquals(1_700_000_000_000L, repo.ultimaSincronizacion().first())
    }
}
