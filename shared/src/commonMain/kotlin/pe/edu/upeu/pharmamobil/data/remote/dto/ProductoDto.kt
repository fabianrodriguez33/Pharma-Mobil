package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long = 1L
)

@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null,
    /** Ruta relativa publica de la foto (/imagenes/archivo.jpg), si el producto tiene. */
    val imagen: String? = null
)
