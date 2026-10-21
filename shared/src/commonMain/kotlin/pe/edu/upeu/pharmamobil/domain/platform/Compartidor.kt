package pe.edu.upeu.pharmamobil.domain.platform

/**
 * Interfaz de la capacidad nativa de compartir.
 * Reside en domain y no tiene importaciones de ninguna plataforma.
 */
interface Compartidor {
    /** [imagenUrl] es opcional: si se indica, la plataforma comparte la foto junto con el texto. */
    fun compartir(texto: String, imagenUrl: String? = null)
}
