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
import pe.edu.upeu.pharmamobil.domain.usecase.comoTextoParaCompartir

class ProductosViewModel(
    private val repository: ProductoRepository,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _estado = MutableStateFlow<ProductosUiState>(ProductosUiState.Loading)
    val estado: StateFlow<ProductosUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun compartir(producto: Producto) = compartidor.compartir(producto.comoTextoParaCompartir())

    fun cargar() = viewModelScope.launch {
        _estado.value = ProductosUiState.Loading
        try {
            _estado.value = ProductosUiState.Success(repository.listar())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _estado.value = ProductosUiState.Error(
                e.message ?: "Error al conectar con el servidor"
            )
        }
    }
}
