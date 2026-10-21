package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toDomainValidos
import pe.edu.upeu.pharmamobil.data.mapper.toRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.error.traducirErrores
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** La base local es la única fuente de verdad; la red solo sincroniza. */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val categoriaPorDefectoId: Long = 1L,
    private val baseUrl: String = ""
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun obtenerPorId(id: Long): Producto =
        local.obtenerPorId(id) ?: throw ErrorApiException(ErrorApi.NoEncontrado)

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        try {
            sincronizar()
        } catch (_: Exception) {
            // Sin red: la fila queda guardada con pendiente = 1
        }
        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {
            local.actualizarPendiente(producto)
            return producto
        }
        val actualizado = traducirErrores {
            remoto.actualizar(producto.id, producto.toRequestDto(categoriaPorDefectoId)).toDomain(baseUrl)
        }
        local.guardarSincronizado(actualizado)
        return actualizado
    }

    override suspend fun eliminar(id: Long) {
        if (id >= 0) traducirErrores { remoto.eliminar(id) }
        local.eliminar(id)
    }

    override suspend fun subirImagen(id: Long, imagen: ImagenProducto): Producto {
        if (id < 0) throw ErrorApiException(ErrorApi.SinConexion)
        val actualizado = traducirErrores {
            remoto.subirImagen(id, imagen.bytes, imagen.nombre, imagen.tipo).toDomain(baseUrl)
        }
        local.guardarSincronizado(actualizado)
        return actualizado
    }

    /** Primero sube lo pendiente; invertir el orden borraría lo creado sin red. */
    override suspend fun sincronizar() = traducirErrores {
        subirPendientes()
        local.reemplazarSincronizados(descargarTodo())
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { producto ->
            remoto.crear(producto.toRequestDto(categoriaPorDefectoId))
            local.confirmarPendiente(producto.id)
        }
    }

    /** El backend limita el tamaño de página a 100: se recorren todas. */
    private suspend fun descargarTodo(): List<Producto> {
        val todos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val respuesta = remoto.listar(pagina, TAMANIO_PAGINA)
            todos += respuesta.contenido.toDomainValidos(baseUrl)
            pagina++
        } while (!respuesta.ultima)
        return todos
    }

    private companion object {
        const val TAMANIO_PAGINA = 100
    }
}
