package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ProductoPendiente
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.platform.formatearSoles
import pe.edu.upeu.pharmamobil.presentation.producto.mensajeLegible
import pe.edu.upeu.pharmamobil.presentation.sincronizacion.SincronizacionUiState
import kotlin.time.Clock

class ProductosViewModel(
    private val repository: ProductoRepository,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _estado = MutableStateFlow<ProductosUiState>(ProductosUiState.Loading)
    val estado: StateFlow<ProductosUiState> = _estado.asStateFlow()

    private val enCurso = MutableStateFlow(false)
    private val sinConexion = MutableStateFlow(false)

    /** Pendientes, fallidas y antigüedad de los datos: salen de la base local, nunca de la red. */
    val sincronizacion: StateFlow<SincronizacionUiState> = combine(
        repository.observarPendientes(),
        repository.observarFallidas(),
        enCurso,
        repository.ultimaSincronizacion(),
        sinConexion
    ) { pendientes, fallidas, curso, ultima, sinRed ->
        SincronizacionUiState(curso, pendientes, fallidas, ultima, sinRed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SincronizacionUiState())

    private val _detalleFallidas = MutableStateFlow<List<ProductoPendiente>>(emptyList())

    /** Filas que agotaron sus intentos, con el motivo; se carga al pulsar «Revisar». */
    val detalleFallidas: StateFlow<List<ProductoPendiente>> = _detalleFallidas.asStateFlow()

    private var productos: List<Producto> = emptyList()
    private var sincronizando = false
    private var avisoRed: String? = null
    private var ultimaSincronizacion: Long? = null

    init {
        viewModelScope.launch {
            try {
                repository.observar().collect { lista ->
                    productos = lista
                    publicar()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ProductosUiState.Error(
                    e.mensajeLegible("No se pudo leer el catálogo guardado")
                )
            }
        }
        viewModelScope.launch {
            repository.ultimaSincronizacion().collect {
                ultimaSincronizacion = it
                publicar()
            }
        }
        viewModelScope.launch {
            // Refresca el "hace X minutos" aunque no cambie nada en la base.
            while (true) {
                delay(30_000)
                publicar()
            }
        }
        sincronizar()
    }

    fun compartir(id: Long) {
        productos.firstOrNull { it.id == id }?.let { compartidor.compartir(it.comoTextoParaCompartir(), it.imagen.ifBlank { null }) }
    }

    fun revisarFallidas() {
        viewModelScope.launch { _detalleFallidas.value = repository.fallidas() }
    }

    fun cerrarRevision() {
        _detalleFallidas.value = emptyList()
    }

    /** El usuario decide: las filas fallidas vuelven a la cola y se sincroniza de nuevo. */
    fun reintentarFallidas() = ejecutarSincronizacion { repository.reintentarFallidas() }

    /** La pantalla lee siempre de la base local; esto solo la refresca desde el servidor. */
    fun sincronizar() = ejecutarSincronizacion { repository.sincronizar() }

    private fun ejecutarSincronizacion(accion: suspend () -> Unit) = viewModelScope.launch {
        sincronizando = true
        enCurso.value = true
        publicar()
        try {
            accion()
            avisoRed = null
            sinConexion.value = false
            _detalleFallidas.value = emptyList()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            sinConexion.value = true
            avisoRed = if ((e as? ErrorApiException)?.error == ErrorApi.NoAutenticado) {
                "Sesión vencida: inicia sesión de nuevo. Tus cambios pendientes siguen guardados"
            } else {
                "Sin conexión: mostrando datos guardados"
            }
        }
        sincronizando = false
        enCurso.value = false
        publicar()
    }

    fun cargar() = sincronizar()

    private fun publicar() {
        if (_estado.value is ProductosUiState.Loading && productos.isEmpty() && sincronizando) return
        _estado.value = ProductosUiState.Success(
            productos.map { ProductoItemUi(it.id, it.nombre, it.imagen, formatearSoles(it.precio)) },
            sincronizando,
            avisoRed,
            textoUltimaSincronizacion(ultimaSincronizacion, Clock.System.now().toEpochMilliseconds())
        )
    }
}
