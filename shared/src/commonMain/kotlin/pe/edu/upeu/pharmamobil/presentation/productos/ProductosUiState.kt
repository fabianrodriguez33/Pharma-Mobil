package pe.edu.upeu.pharmamobil.presentation.productos

/** Producto listo para pintar: el precio ya viene formateado desde el ViewModel. */
data class ProductoItemUi(
    val id: Long,
    val nombre: String,
    val imagen: String,
    val precio: String
)

sealed interface ProductosUiState {
    data object Loading : ProductosUiState
    data class Success(val productos: List<ProductoItemUi>) : ProductosUiState
    data class Error(val mensaje: String) : ProductosUiState
}
