package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** El cliente HTTP avisa aqui de un 401 en una ruta protegida; la UI reacciona yendo a Login. */
class EventosSesion {
    private val _expirada = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val expirada: SharedFlow<Unit> = _expirada.asSharedFlow()

    fun notificarExpirada() {
        _expirada.tryEmit(Unit)
    }
}
