package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {

    /** Catálogo reactivo: emite cada vez que cambia la fuente de verdad. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }

    /** Sube lo pendiente y luego descarga el catálogo del servidor. */
    suspend fun sincronizar() {}

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    suspend fun obtenerPorId(id: Long): Producto

    /** Reemplaza los datos del producto con el mismo id y devuelve el resultado. */
    suspend fun actualizar(producto: Producto): Producto

    suspend fun eliminar(id: Long)

    /** Sube la foto del producto y devuelve el producto con su imagen asignada. */
    suspend fun subirImagen(id: Long, imagen: ImagenProducto): Producto = obtenerPorId(id)
}
