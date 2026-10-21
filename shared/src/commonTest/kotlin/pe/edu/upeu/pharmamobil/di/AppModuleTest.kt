package pe.edu.upeu.pharmamobil.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositoryRest
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo se ensambla sin arrancar la aplicacion: si una
 * definicion falta o esta declarada con el tipo equivocado, falla aqui.
 *
 * Se cargan los cuatro modulos, incluido presentationModule: construir un
 * ViewModel es justo lo que se rompe al cambiar un constructor, y antes era
 * lo unico que el grafo no cubria. Como los ViewModel arrancan una carga en
 * su init, hace falta un Dispatchers.Main de prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, networkModule, platformModule, almacenDePrueba, driverDePrueba)
    }.koin

    /** El Keystore real necesita un Context de Android; en JVM se sustituye por memoria. */
    private val almacenDePrueba = module {
        single<AlmacenSeguro> {
            object : AlmacenSeguro {
                var token: String? = null
                var perfil: String? = null
                override fun guardarToken(token: String) { this.token = token }
                override fun leerToken() = token
                override fun guardarPerfil(json: String) { perfil = json }
                override fun leerPerfil() = perfil
                override fun borrarToken() { token = null; perfil = null }
            }
        }
    }

    /** SQLite en memoria en lugar del driver de Android, que necesita un Context. */
    private val driverDePrueba = module {
        single<CreadorDeDriver> {
            object : CreadorDeDriver {
                override fun crear() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                    .also { PharmaDb.Schema.create(it) }
            }
        }
    }

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<ProductoRepositorioOffline>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioEnMemoria>(koin.get<ClienteRepository>())
        assertIs<AuthRepositoryRest>(koin.get<AuthRepository>())
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {

        val koin = grafoCompleto()

        assertSame(
            koin.get<ProductoRepository>(),
            koin.get<ProductoRepository>()
        )
        assertSame(
            koin.get<ClienteRepository>(),
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun resuelveLosCuatroCasosDeUsoConSusRepositorios() {

        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<RegistrarClienteUseCase>()
        koin.get<ListarClientesUseCase>()
    }

    @Test
    fun resuelveLosViewModelConSusCasosDeUso() {

        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
    }
}
