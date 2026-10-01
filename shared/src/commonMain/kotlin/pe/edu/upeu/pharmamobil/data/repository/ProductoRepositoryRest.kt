package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toDomainValidos
import pe.edu.upeu.pharmamobil.data.mapper.toRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.error.traducirErrores
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Repositorio REST de PharmaSoft. Todo fallo de red o HTTP sale como
 * [pe.edu.upeu.pharmamobil.domain.error.ErrorApiException].
 */
class ProductoRepositoryRest(
    private val api: ProductoApi,
    private val categoriaPorDefectoId: Long = 1L
) : ProductoRepository {

    override suspend fun listar(): List<Producto> = traducirErrores {
        api.listar().contenido.toDomainValidos()
    }

    override suspend fun obtenerPorId(id: Long): Producto = traducirErrores {
        api.obtenerPorId(id).toDomain()
    }

    override suspend fun registrar(producto: Producto): Producto = traducirErrores {
        api.crear(producto.toRequestDto(categoriaPorDefectoId)).toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = traducirErrores {
        api.actualizar(producto.id, producto.toRequestDto(categoriaPorDefectoId)).toDomain()
    }

    override suspend fun eliminar(id: Long) = traducirErrores {
        api.eliminar(id)
    }
}
