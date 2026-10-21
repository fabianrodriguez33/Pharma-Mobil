package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Producto

/** [baseUrl] convierte la ruta relativa de la foto en una URL absoluta que Coil puede pedir. */
fun ProductoResponseDto.toDomain(baseUrl: String = ""): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    imagen = imagen?.takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("http")) it else baseUrl.trimEnd('/') + "/" + it.trimStart('/') }
        .orEmpty()
)

fun Producto.toRequestDto(categoriaId: Long): ProductoRequestDto = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = true,
    categoriaId = categoriaId
)

/**
 * El backend elimina con baja logica (estado = false) y el listado devuelve
 * tambien los inactivos: se omiten. Tambien se descartan los que violarian
 * los invariantes del dominio (p. ej. precio 0).
 */
fun List<ProductoResponseDto>.toDomainValidos(baseUrl: String = ""): List<Producto> =
    filter { it.estado }.mapNotNull { dto -> runCatching { dto.toDomain(baseUrl) }.getOrNull() }
