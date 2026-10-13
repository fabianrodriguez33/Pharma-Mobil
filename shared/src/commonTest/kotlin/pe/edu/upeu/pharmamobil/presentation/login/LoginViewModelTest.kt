package pe.edu.upeu.pharmamobil.presentation.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeAuthRepository(var resultado: Result<Sesion>) : AuthRepository {
    private val _sesion = MutableStateFlow<Sesion?>(null)
    override val sesionActual: StateFlow<Sesion?> = _sesion
    var llamadas = 0

    override suspend fun iniciarSesion(credenciales: Credenciales): Result<Sesion> {
        llamadas++
        resultado.onSuccess { _sesion.value = it }
        return resultado
    }

    override fun obtenerSesion(): Sesion? = _sesion.value
    override fun cerrarSesion() {
        _sesion.value = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val almacen = Sesion("t", "almacen", "Almacén", setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"))

    @BeforeTest
    fun instalarMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun restaurarMain() = Dispatchers.resetMain()

    private fun vm(repo: FakeAuthRepository) = LoginViewModel(IniciarSesionUseCase(repo))

    @Test
    fun sesionPuedeSoloConPermisosConcedidos() {
        val vendedor = Sesion("t", "vendedor", "Vendedor", setOf("CATALOGO_LEER"))
        assertFalse(vendedor.puede(Sesion.CATALOGO_ESCRIBIR))
        assertTrue(almacen.puede(Sesion.CATALOGO_ESCRIBIR))
    }

    @Test
    fun camposVaciosNoLlamanAlRepositorio() {
        val repo = FakeAuthRepository(Result.success(almacen))
        val viewModel = vm(repo)

        viewModel.ingresar()

        assertIs<EstadoLogin.Error>(viewModel.uiState.value.estado)
        assertEquals(0, repo.llamadas)
    }

    @Test
    fun loginCorrectoTerminaEnExitoYLimpiaLaClave() {
        val repo = FakeAuthRepository(Result.success(almacen))
        val viewModel = vm(repo)
        viewModel.onUsuarioChange("almacen")
        viewModel.onClaveChange("secreta")

        viewModel.ingresar()

        assertEquals(EstadoLogin.Exito(almacen), viewModel.uiState.value.estado)
        assertEquals("", viewModel.uiState.value.clave)
        assertEquals(almacen, repo.obtenerSesion())
    }

    @Test
    fun credencialesInvalidasMuestranMensajeEspecifico() {
        val repo = FakeAuthRepository(Result.failure(ErrorApiException(ErrorApi.NoAutenticado)))
        val viewModel = vm(repo)
        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("mala")

        viewModel.ingresar()

        val estado = assertIs<EstadoLogin.Error>(viewModel.uiState.value.estado)
        assertEquals("Usuario o contraseña incorrectos", estado.mensaje)
    }
}
