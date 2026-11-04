package pe.edu.upeu.pharmamobil.presentation.productos

/** Producto listo para pintar: el precio ya viene formateado desde el ViewModel. */
data class ProductoItemUi(
    val id: Long,
    val nombre: String,
    val imagen: String,
    val precio: String
) {
    /** Los ids locales son negativos: la fila aun no llego al servidor. */
    val pendiente: Boolean get() = id < 0
}

sealed interface ProductosUiState {
    data object Loading : ProductosUiState
    data class Success(
        val productos: List<ProductoItemUi>,
        val sincronizando: Boolean = false,
        val avisoRed: String? = null,
        val ultimaSincronizacion: String = "Nunca sincronizado"
    ) : ProductosUiState
    data class Error(val mensaje: String) : ProductosUiState
}
