package pe.edu.upeu.pharmamobil.domain.platform

/**
 * Interfaz de la capacidad nativa de compartir.
 * Reside en domain y no tiene importaciones de ninguna plataforma.
 */
interface Compartidor {
    fun compartir(texto: String)
}
