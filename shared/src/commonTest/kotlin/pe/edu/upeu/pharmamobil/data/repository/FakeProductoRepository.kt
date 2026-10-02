package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.CompletableDeferred
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Doble del inventario para las pruebas: sin delay y capaz de fallar a
 * voluntad. Sin el, probar el camino de error del caso de uso o del ViewModel
 * era imposible, porque el repositorio en memoria nunca falla.
 */
class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null

    /** Si no es nulo, la llamada queda suspendida hasta completarlo: permite observar estados intermedios. */
    var puertaAlListar: CompletableDeferred<Unit>? = null
    var puertaAlEliminar: CompletableDeferred<Unit>? = null

    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {

        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Producto> {

        puertaAlListar?.await()
        fallaAlListar?.let { throw it }

        return productos.toList()
    }

    override suspend fun obtenerPorId(id: Long): Producto =
        productos.firstOrNull { it.id == id } ?: throw ErrorApiException(ErrorApi.NoEncontrado)

    override suspend fun actualizar(producto: Producto): Producto {

        fallaAlActualizar?.let { throw it }

        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice < 0) throw ErrorApiException(ErrorApi.NoEncontrado)
        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {

        puertaAlEliminar?.await()
        fallaAlEliminar?.let { throw it }

        if (!productos.removeAll { it.id == id }) throw ErrorApiException(ErrorApi.NoEncontrado)
    }
}
