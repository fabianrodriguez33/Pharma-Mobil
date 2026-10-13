package pe.edu.upeu.pharmamobil.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.presentation.producto.mensajeLegible

sealed interface EstadoLogin {
    data object Inactivo : EstadoLogin
    data object Cargando : EstadoLogin
    data class Exito(val sesion: Sesion) : EstadoLogin
    data class Error(val mensaje: String) : EstadoLogin
}

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val estado: EstadoLogin = EstadoLogin.Inactivo
) {
    val cargando: Boolean get() = estado is EstadoLogin.Cargando
}

class LoginViewModel(
    private val iniciarSesion: IniciarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(valor: String) = _uiState.update { it.copy(usuario = valor, estado = EstadoLogin.Inactivo) }

    fun onClaveChange(valor: String) = _uiState.update { it.copy(clave = valor, estado = EstadoLogin.Inactivo) }

    fun ingresar() {
        val actual = _uiState.value
        if (actual.cargando) return
        if (actual.usuario.isBlank() || actual.clave.isBlank()) {
            _uiState.update { it.copy(estado = EstadoLogin.Error("Ingresa usuario y contraseña")) }
            return
        }
        _uiState.update { it.copy(estado = EstadoLogin.Cargando) }
        viewModelScope.launch {
            iniciarSesion(Credenciales(actual.usuario, actual.clave)).fold(
                onSuccess = { sesion ->
                    // La clave no se conserva en memoria tras un login correcto.
                    _uiState.update { it.copy(clave = "", estado = EstadoLogin.Exito(sesion)) }
                },
                onFailure = { fallo ->
                    val mensaje = if ((fallo as? ErrorApiException)?.error == ErrorApi.NoAutenticado) {
                        "Usuario o contraseña incorrectos"
                    } else {
                        fallo.mensajeLegible("No se pudo iniciar sesión")
                    }
                    _uiState.update { it.copy(estado = EstadoLogin.Error(mensaje)) }
                }
            )
        }
    }
}
