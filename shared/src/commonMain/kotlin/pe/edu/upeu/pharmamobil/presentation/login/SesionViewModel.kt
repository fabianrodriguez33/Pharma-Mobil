package pe.edu.upeu.pharmamobil.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.remote.EventosSesion
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase

/** Guardia de navegacion: la app muestra Login mientras [sesion] sea null. */
class SesionViewModel(
    repository: AuthRepository,
    private val cerrarSesion: CerrarSesionUseCase,
    eventos: EventosSesion
) : ViewModel() {

    val sesion: StateFlow<Sesion?> = repository.sesionActual

    private val _avisoExpirada = MutableStateFlow(false)
    val avisoExpirada: StateFlow<Boolean> = _avisoExpirada.asStateFlow()

    init {
        viewModelScope.launch {
            eventos.expirada.collect {
                // 401 en ruta protegida: se descarta la sesion y se vuelve a Login.
                if (sesion.value != null) {
                    _avisoExpirada.value = true
                    cerrarSesion()
                }
            }
        }
    }

    fun salir() {
        _avisoExpirada.value = false
        cerrarSesion()
    }

    fun descartarAviso() {
        _avisoExpirada.value = false
    }
}
