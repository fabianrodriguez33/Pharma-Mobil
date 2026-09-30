package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.domain.model.Producto

/**
 * El dominio actual solo conoce id, nombre, precio y stock. La API no expone
 * stock, asi que se inicia en 0; descripcion, imagen y categoria no se mapean
 * para no tocar el modelo de dominio.
 */
fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title.ifBlank { "Sin nombre" },
    precio = price,
    stock = 0
)

/** Descarta los productos que violarian los invariantes del dominio (p. ej. precio 0). */
fun List<ProductoDto>.toDomainValidos(): List<Producto> =
    mapNotNull { dto -> runCatching { dto.toDomain() }.getOrNull() }
