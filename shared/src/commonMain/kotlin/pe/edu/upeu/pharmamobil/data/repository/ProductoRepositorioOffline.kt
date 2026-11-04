package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.error.traducirErrores
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ProductoPendiente
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import kotlin.time.Clock

/** La base local es la única fuente de verdad; la red solo sincroniza. */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val categoriaPorDefectoId: Long = 1L,
    private val baseUrl: String = "",
    private val ahora: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    esperar: suspend (Long) -> Unit = { delay(it) }
) : ProductoRepository {

    private val sincronizarCaso = SincronizarUseCase(
        local = local,
        remoto = EnvioRepositorioRest(remoto, categoriaPorDefectoId, baseUrl),
        esperar = esperar
    )

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun ultimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun obtenerPorId(id: Long): Producto =
        local.obtenerPorId(id) ?: throw ErrorApiException(ErrorApi.NoEncontrado)

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        try {
            ejecutarSincronizacion(conEspera = false)
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
    override suspend fun sincronizar() = ejecutarSincronizacion(conEspera = true)

    override fun observarPendientes(): Flow<Int> = local.observarPendientes(SincronizarUseCase.MAX_INTENTOS)

    override fun observarFallidas(): Flow<Int> = local.observarFallidas(SincronizarUseCase.MAX_INTENTOS)

    override suspend fun fallidas(): List<ProductoPendiente> = local.fallidas(SincronizarUseCase.MAX_INTENTOS)

    override suspend fun reintentarFallidas() {
        local.reintentarFallidas(SincronizarUseCase.MAX_INTENTOS)
        ejecutarSincronizacion(conEspera = true)
    }

    private suspend fun ejecutarSincronizacion(conEspera: Boolean) = traducirErrores {
        sincronizarCaso(conEspera)
        local.guardarUltimaSincronizacion(ahora())
    }
}
