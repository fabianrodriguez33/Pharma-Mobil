package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.db.Producto as ProductoEntity
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun listar(): List<Producto> =
        consultas.listarTodos().executeAsList().map { it.aDominio() }

    fun obtenerPorId(id: Long): Producto? =
        consultas.obtenerPorId(id).executeAsOneOrNull()?.aDominio()

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList().map { it.aDominio() }

    /** Guarda con un id local negativo y pendiente = 1 hasta que el servidor lo confirme. */
    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(id = minOf(menor, 0L) - 1L)
        consultas.insertar(
            local.id, local.nombre, local.precio, local.stock.toLong(), local.imagen, 1L
        )
        return local
    }

    fun actualizarPendiente(producto: Producto) {
        consultas.insertar(
            producto.id, producto.nombre, producto.precio, producto.stock.toLong(), producto.imagen, 1L
        )
    }

    fun guardarSincronizado(producto: Producto) {
        consultas.insertar(
            producto.id, producto.nombre, producto.precio, producto.stock.toLong(), producto.imagen, 0L
        )
    }

    fun eliminar(id: Long) {
        consultas.borrarPorId(id)
    }

    /** Descarta la fila pendiente que el servidor ya confirmó con su id real. */
    fun confirmarPendiente(idLocal: Long) {
        consultas.borrarPorId(idLocal)
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach {
                consultas.insertar(
                    it.id, it.nombre, it.precio, it.stock.toLong(), it.imagen, 0L
                )
            }
        }
    }

    private fun ProductoEntity.aDominio() = Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock.toInt(),
        imagen = imagen
    )
}
