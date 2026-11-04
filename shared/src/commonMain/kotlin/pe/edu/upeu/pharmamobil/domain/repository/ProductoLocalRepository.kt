package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ProductoPendiente

/** Puerto hacia la base local que necesita la sincronizacion. */
interface ProductoLocalRepository {
    /** Filas pendientes que aun no agotaron sus intentos. */
    fun pendientes(maxIntentos: Int): List<ProductoPendiente>

    /** La fila local pasa a sincronizada con el id definitivo del servidor. */
    fun confirmar(idLocal: Long, idServidor: Long)

    /** Fallo permanente: la fila agota sus intentos y queda visible para el usuario. */
    fun marcarFallida(id: Long, motivo: String, maxIntentos: Int)

    /** Fallo transitorio: suma un intento y recuerda el motivo. */
    fun sumarIntento(id: Long, motivo: String)

    /** Reemplaza lo sincronizado por lo que dice el servidor sin tocar lo pendiente. */
    fun conciliar(remotos: List<Producto>)

    fun contarPendientes(maxIntentos: Int): Int
}
