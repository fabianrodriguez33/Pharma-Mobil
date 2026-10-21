package pe.edu.upeu.pharmamobil.domain.model

/** Foto elegida por el usuario para un producto, lista para subirse al backend. */
class ImagenProducto(
    val bytes: ByteArray,
    val nombre: String,
    val tipo: String = "image/jpeg"
)
