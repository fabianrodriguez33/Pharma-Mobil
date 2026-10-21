package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SubirImagenProductoUseCase
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoUiState.Operacion
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoUiState.Operacion.Tipo


class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val subirImagen: SubirImagenProductoUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(fase = ProductoUiState.Fase.Cargando)
            }

            val fase = listarProductos().fold(
                onSuccess = { productos ->
                    if (productos.isEmpty()) {
                        ProductoUiState.Fase.SinProductos
                    } else {
                        ProductoUiState.Fase.ConProductos(productos.map { it.aUi() })
                    }
                },
                onFailure = { fallo ->
                    ProductoUiState.Fase.Error(
                        fallo.mensajeLegible("No se pudo cargar el inventario")
                    )
                }
            )

            _uiState.update {
                it.copy(fase = fase)
            }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nombre, nombreError = null),
                operacion = Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(precio = precio, precioError = null),
                operacion = Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun onStockChange(stock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(stock = stock, stockError = null),
                operacion = Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    /** Foto opcional: null la quita y el producto se guarda sin imagen nueva. */
    fun onImagenElegida(imagen: ImagenProducto?) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(imagen = imagen),
                operacion = Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun editar(producto: ProductoUi) {
        _uiState.update {
            it.copy(
                formulario = FormularioProducto(
                    id = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precioNumero.toString(),
                    stock = producto.stockNumero.toString(),
                    imagenActual = producto.imagen
                ),
                operacion = Operacion.Inactiva,
                mensajeExito = null
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(formulario = FormularioProducto(), operacion = Operacion.Inactiva)
        }
    }

    /** Crea o actualiza segun el formulario tenga o no un id. */
    fun guardar() {

        if (_uiState.value.ocupado) return

        val formulario = _uiState.value.formulario
        val tipo = if (formulario.editando) Tipo.Actualizar else Tipo.Crear

        viewModelScope.launch {

            _uiState.update {
                it.copy(operacion = Operacion.EnCurso(tipo), mensajeExito = null)
            }

            val resultado = if (formulario.editando) {
                actualizarProducto(formulario.id, formulario.nombre, formulario.precio, formulario.stock)
            } else {
                registrarProducto(formulario.nombre, formulario.precio, formulario.stock)
            }

            // La foto es opcional: solo se sube si se eligio una y el producto ya existe.
            val imagen = formulario.imagen
            var avisoImagen: String? = null
            val guardado = resultado.getOrNull()
            if (guardado != null && imagen != null && subirImagen != null) {
                subirImagen(guardado.id, imagen).onFailure {
                    avisoImagen = it.mensajeLegible("No se pudo subir la imagen")
                }
            }

            resultado.fold(
                onSuccess = { producto ->
                    val verbo = if (tipo == Tipo.Crear) "registrado" else "actualizado"
                    if (avisoImagen != null) {
                        _uiState.update {
                            it.copy(
                                operacion = Operacion.Fallida(
                                    "Producto \"${producto.nombre}\" $verbo, pero la imagen no se subió: $avisoImagen"
                                ),
                                formulario = FormularioProducto()
                            )
                        }
                        cargarProductos()
                        return@fold
                    }
                    _uiState.update {
                        it.copy(
                            operacion = Operacion.Inactiva,
                            formulario = FormularioProducto(),
                            mensajeExito = "Producto \"${producto.nombre}\" $verbo correctamente"
                        )
                    }
                    cargarProductos()
                },
                onFailure = { fallo -> _uiState.update { it.conFallo(fallo, tipo) } }
            )
        }
    }

    fun eliminar(id: Long) {

        if (_uiState.value.ocupado) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(operacion = Operacion.EnCurso(Tipo.Eliminar), mensajeExito = null)
            }

            eliminarProducto(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            operacion = Operacion.Inactiva,
                            formulario = if (it.formulario.id == id) FormularioProducto() else it.formulario,
                            mensajeExito = "Producto eliminado correctamente"
                        )
                    }
                    cargarProductos()
                },
                onFailure = { fallo -> _uiState.update { it.conFallo(fallo, Tipo.Eliminar) } }
            )
        }
    }

    /** Los errores de campo (400) van bajo cada input; el resto, a la operacion fallida. */
    private fun ProductoUiState.conFallo(fallo: Throwable, tipo: Tipo): ProductoUiState {

        val porCampo: Map<String, String> = when {
            fallo is ProductoInvalidoException -> listOfNotNull(
                fallo.errores.nombre?.let { "nombre" to it },
                fallo.errores.precio?.let { "precio" to it },
                fallo.errores.stock?.let { "stock" to it }
            ).toMap()

            fallo is ErrorApiException && fallo.error is ErrorApi.Validacion ->
                fallo.error.porCampo

            else -> emptyMap()
        }

        if (porCampo.isNotEmpty()) {
            return copy(
                operacion = Operacion.Inactiva,
                formulario = formulario.copy(
                    nombreError = porCampo["nombre"],
                    precioError = porCampo["precio"],
                    stockError = porCampo["stock"]
                )
            )
        }

        val porDefecto = when (tipo) {
            Tipo.Crear -> "No se pudo registrar el producto"
            Tipo.Actualizar -> "No se pudo actualizar el producto"
            Tipo.Eliminar -> "No se pudo eliminar el producto"
        }
        return copy(operacion = Operacion.Fallida(fallo.mensajeLegible(porDefecto)))
    }
}
