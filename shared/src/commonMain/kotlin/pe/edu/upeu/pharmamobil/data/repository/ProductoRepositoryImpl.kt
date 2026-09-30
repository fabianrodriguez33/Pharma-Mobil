package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.mapper.toDomainValidos
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Repositorio remoto (solo lectura en esta guia). Los errores de red se
 * propagan como excepcion; quien consume los convierte en estado de error.
 */
class ProductoRepositoryImpl(
    private val api: ProductoApi
) : ProductoRepository {

    override suspend fun registrar(producto: Producto): Producto =
        throw UnsupportedOperationException("El registro remoto llega en una guia posterior")

    override suspend fun listar(): List<Producto> =
        api.obtenerProductos().toDomainValidos()
}
