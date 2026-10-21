package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoUiState
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoRepositorioOfflineTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val metodos = mutableListOf<HttpMethod>()

    private fun pagina(vararg filas: String) =
        """{"contenido":[${filas.joinToString(",")}],"pagina":0,"tamanio":1000,""" +
            """"totalElementos":${filas.size},"totalPaginas":1,"ultima":true}"""

    private fun fila(id: Long, nombre: String) =
        """{"id":$id,"nombre":"$nombre","precio":2.5,"stock":20,"estado":true}"""

    private fun crearBase(): PharmaDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        return PharmaDb(driver)
    }

    private fun repositorio(base: PharmaDb, motor: MockEngine) = ProductoRepositorioOffline(
        local = ProductoLocal(base),
        remoto = ProductoApi(crearHttpClient(motor, "http://localhost:8080/"))
    )

    private val servidorCaido = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }

    @Test
    fun loRegistradoSinRedQuedaConIdNegativoYPendiente() = runTest {
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
    fun sincronizarSubePendientesYReemplazaLaListaLocal() = runTest {
        val base = crearBase()
        repositorio(base, servidorCaido).registrar(Producto(0L, "Paracetamol", 2.5, 20))

        val motor = MockEngine { request ->
            metodos += request.method
            when (request.method) {
                HttpMethod.Post -> respond(fila(7, "Paracetamol"), HttpStatusCode.Created, json)
                else -> respond(pagina(fila(7, "Paracetamol"), fila(8, "Ibuprofeno")), HttpStatusCode.OK, json)
            }
        }
        val repo = repositorio(base, motor)

        repo.sincronizar()

        assertEquals(listOf(HttpMethod.Post, HttpMethod.Get), metodos)
        assertTrue(base.productoQueries.listarPendientes().executeAsList().isEmpty())
        assertEquals(listOf(7L, 8L), repo.observar().first().map { it.id }.sorted())
    }

    @Test
    fun conElServidorFueraDeLineaObservarEmiteLosDatosGuardados() = runTest {
        val base = crearBase()
        val local = ProductoLocal(base)
        local.reemplazarSincronizados(listOf(Producto(1L, "Aspirina", 1.0, 5)))
        val repo = repositorio(base, servidorCaido)

        assertFailsWith<Exception> { repo.sincronizar() }

        assertEquals(listOf("Aspirina"), repo.observar().first().map { it.nombre })
    }

    @Test
    fun elViewModelMuestraLoRegistradoSinRecargarAMano() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val repo = repositorio(crearBase(), servidorCaido)
            val viewModel = ProductoViewModel(
                registrarProducto = RegistrarProductoUseCase(repo),
                listarProductos = ListarProductosUseCase(repo),
                actualizarProducto = ActualizarProductoUseCase(repo),
                eliminarProducto = EliminarProductoUseCase(repo),
                observarProductos = ObservarProductosUseCase(repo)
            )
            viewModel.onNombreChange("Paracetamol")
            viewModel.onPrecioChange("2.5")
            viewModel.onStockChange("20")

            viewModel.guardar()

            val fase = viewModel.uiState.first { it.fase is ProductoUiState.Fase.ConProductos }.fase
            assertEquals(1, (fase as ProductoUiState.Fase.ConProductos).productos.size)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
