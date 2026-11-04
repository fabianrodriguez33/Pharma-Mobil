package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ProductoPendiente

interface ProductoRepository {

    /** Catálogo reactivo: emite cada vez que cambia la fuente de verdad. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }

    /** Marca de tiempo (epoch millis) de la última sincronización exitosa; null si nunca hubo. */
    fun ultimaSincronizacion(): Flow<Long?> = flowOf(null)

    /** Cambios que faltan por enviar y aun tienen intentos disponibles. */
    fun observarPendientes(): Flow<Int> = flowOf(0)

    /** Cambios que agotaron sus intentos y esperan una decision del usuario. */
    fun observarFallidas(): Flow<Int> = flowOf(0)

    /** Detalle de las filas fallidas, con el motivo del ultimo error. */
    suspend fun fallidas(): List<ProductoPendiente> = emptyList()

    /** Devuelve las filas fallidas a la cola y sincroniza de nuevo. */
    suspend fun reintentarFallidas() {}

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
