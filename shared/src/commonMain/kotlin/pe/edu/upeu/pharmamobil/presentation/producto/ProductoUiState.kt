package pe.edu.upeu.pharmamobil.presentation.producto

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioProducto = FormularioProducto(),
    val operacion: Operacion = Operacion.Inactiva,
    val mensajeExito: String? = null
) {

    /** Fases excluyentes del inventario: solo una puede estar activa. */
    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(val productos: List<ProductoUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    /** Accion en curso, independiente de la pantalla que se muestra. */
    sealed interface Operacion {

        data object Inactiva : Operacion

        data class EnCurso(val tipo: Tipo) : Operacion

        data class Fallida(val mensaje: String) : Operacion

        enum class Tipo { Crear, Actualizar, Eliminar }
    }

    val ocupado: Boolean
        get() = operacion is Operacion.EnCurso
}

data class FormularioProducto(
    /** 0 = alta de un producto nuevo; otro valor = edicion de ese producto. */
    val id: Long = 0L,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null
) {

    val editando: Boolean
        get() = id != 0L
}
