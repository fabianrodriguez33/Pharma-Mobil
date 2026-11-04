package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.db.Producto as ProductoEntity
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ProductoPendiente
import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository

class ProductoLocal(private val base: PharmaDb) : ProductoLocalRepository {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun observarUltimaSincronizacion(): Flow<Long?> =
        base.sincronizacionQueries.ultimaSincronizacion()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)

    fun guardarUltimaSincronizacion(epochMillis: Long) {
        base.sincronizacionQueries.guardarUltimaSincronizacion(epochMillis)
    }

    fun listar(): List<Producto> =
        consultas.listarTodos().executeAsList().map { it.aDominio() }

    fun obtenerPorId(id: Long): Producto? =
        consultas.obtenerPorId(id).executeAsOneOrNull()?.aDominio()

    override fun pendientes(maxIntentos: Int): List<ProductoPendiente> =
        consultas.pendientesPorEnviar(maxIntentos.toLong()).executeAsList().map { it.aPendiente() }

    override fun contarPendientes(maxIntentos: Int): Int =
        consultas.contarPendientes(maxIntentos.toLong()).executeAsOne().toInt()

    fun observarPendientes(maxIntentos: Int): Flow<Int> =
        consultas.contarPendientes(maxIntentos.toLong())
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toInt() }

    fun observarFallidas(maxIntentos: Int): Flow<Int> =
        consultas.contarFallidas(maxIntentos.toLong())
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toInt() }

    /** Filas que agotaron sus intentos, con el motivo del ultimo fallo. */
    fun fallidas(maxIntentos: Int): List<ProductoPendiente> =
        consultas.listarFallidas(maxIntentos.toLong()).executeAsList().map { it.aPendiente() }

    /** El usuario decide reintentar: las filas fallidas vuelven a la cola con 0 intentos. */
    fun reintentarFallidas(maxIntentos: Int) {
        consultas.reintentarFallidas(maxIntentos.toLong())
    }

    override fun sumarIntento(id: Long, motivo: String) {
        consultas.sumarIntento(motivo, id)
    }

    override fun marcarFallida(id: Long, motivo: String, maxIntentos: Int) {
        consultas.marcarFallida(maxIntentos.toLong(), motivo, id)
    }

    override fun confirmar(idLocal: Long, idServidor: Long) {
        consultas.confirmarEnvio(idServidor, idLocal)
    }

    override fun conciliar(remotos: List<Producto>) = reemplazarSincronizados(remotos)

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

    /** La fila pendiente pasa a sincronizada con el id definitivo que asignó el servidor. */
    fun confirmarPendiente(idLocal: Long, confirmado: Producto) {
        consultas.transaction {
            consultas.borrarPorId(idLocal)
            guardarSincronizado(confirmado)
        }
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

    private fun ProductoEntity.aPendiente() = ProductoPendiente(aDominio(), intentos.toInt(), ultimoError)

    private fun ProductoEntity.aDominio() = Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock.toInt(),
        imagen = imagen
    )
}
