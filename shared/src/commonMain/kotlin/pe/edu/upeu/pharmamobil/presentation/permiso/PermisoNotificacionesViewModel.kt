package pe.edu.upeu.pharmamobil.presentation.permiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso

enum class DialogoPermiso { NINGUNO, EXPLICAR, IR_A_AJUSTES }

data class PermisoUiState(
    val estado: EstadoPermiso? = null,
    val dialogo: DialogoPermiso = DialogoPermiso.NINGUNO
)

/**
 * Los tres estados del permiso se traducen a tres respuestas: continuar,
 * explicar y volver a pedir, o llevar a Ajustes. Nunca se pide sin explicar.
 */
class PermisoNotificacionesViewModel(
    private val gestor: GestorPermisos,
    private val permiso: Permiso = Permiso.NOTIFICACIONES
) : ViewModel() {

    private val _estado = MutableStateFlow(PermisoUiState())
    val estado: StateFlow<PermisoUiState> = _estado.asStateFlow()

    init {
        refrescar()
    }

    /** Se llama al abrir la pantalla y al volver de Ajustes. */
    fun refrescar() {
        viewModelScope.launch {
            val actual = gestor.estado(permiso)
            _estado.update { it.copy(estado = actual) }
        }
    }

    fun alPulsarActivar() {
        when (_estado.value.estado) {
            EstadoPermiso.Concedido, null -> Unit
            EstadoPermiso.Denegado -> mostrar(DialogoPermiso.EXPLICAR)
            EstadoPermiso.DenegadoParaSiempre -> mostrar(DialogoPermiso.IR_A_AJUSTES)
        }
    }

    /** El usuario leyo la explicacion y acepto: recien ahora aparece el dialogo del sistema. */
    fun alConfirmarExplicacion() {
        mostrar(DialogoPermiso.NINGUNO)
        viewModelScope.launch {
            val resultado = gestor.solicitar(permiso)
            _estado.update {
                it.copy(
                    estado = resultado,
                    dialogo = if (resultado == EstadoPermiso.DenegadoParaSiempre) DialogoPermiso.IR_A_AJUSTES
                    else DialogoPermiso.NINGUNO
                )
            }
        }
    }

    fun alIrAAjustes() {
        mostrar(DialogoPermiso.NINGUNO)
        gestor.abrirAjustes()
    }

    fun alCerrarDialogo() = mostrar(DialogoPermiso.NINGUNO)

    private fun mostrar(dialogo: DialogoPermiso) = _estado.update { it.copy(dialogo = dialogo) }
}
