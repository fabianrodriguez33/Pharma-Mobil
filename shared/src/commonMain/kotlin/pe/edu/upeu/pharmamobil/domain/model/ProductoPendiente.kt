package pe.edu.upeu.pharmamobil.domain.model

/** Fila de la cola de pendientes con su memoria: cuantas veces se intento y que fallo. */
data class ProductoPendiente(
    val producto: Producto,
    val intentos: Int,
    val ultimoError: String?
) {
    val id: Long get() = producto.id
}

/** Resumen de una pasada de sincronizacion. */
data class ResumenSync(val enviadas: Int, val fallidas: Int, val pendientes: Int)
