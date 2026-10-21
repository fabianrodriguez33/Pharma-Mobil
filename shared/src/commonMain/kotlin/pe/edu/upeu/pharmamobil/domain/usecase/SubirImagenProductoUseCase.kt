package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class SubirImagenProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(id: Long, imagen: ImagenProducto): Result<Producto> =
        resultadoDe { productoRepository.subirImagen(id, imagen) }
}
