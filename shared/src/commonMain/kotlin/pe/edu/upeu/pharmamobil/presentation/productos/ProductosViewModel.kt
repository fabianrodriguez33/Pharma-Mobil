package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.platform.formatearSoles

class ProductosViewModel(
    private val repository: ProductoRepository,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _estado = MutableStateFlow<ProductosUiState>(ProductosUiState.Loading)
    val estado: StateFlow<ProductosUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    private var productos: List<Producto> = emptyList()

    fun compartir(id: Long) {
        productos.firstOrNull { it.id == id }?.let { compartidor.compartir(it.comoTextoParaCompartir()) }
    }

    fun cargar() = viewModelScope.launch {
        _estado.value = ProductosUiState.Loading
        try {
            _estado.value = ProductosUiState.Success(
                repository.listar().also { productos = it }.map {
                    ProductoItemUi(it.id, it.nombre, it.imagen, formatearSoles(it.precio))
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _estado.value = ProductosUiState.Error(
                e.message ?: "Error al conectar con el servidor"
            )
        }
    }
}
