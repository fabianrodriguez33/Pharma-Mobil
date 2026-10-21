package pe.edu.upeu.pharmamobil.presentation.producto

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelOfflineTest {

    private fun repositorio(): ProductoRepositorioOffline {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        val servidorCaido = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }
        return ProductoRepositorioOffline(
            ProductoLocal(PharmaDb(driver)),
            ProductoApi(crearHttpClient(servidorCaido, "http://localhost:8080/"))
        )
    }

    @Test
    fun elViewModelMuestraLoRegistradoSinRecargarAMano() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val repo = repositorio()
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
