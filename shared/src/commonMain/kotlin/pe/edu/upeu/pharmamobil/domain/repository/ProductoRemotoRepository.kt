package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

/** Puerto hacia el servicio remoto; traduce los errores de la API a [ResultadoEnvio]. */
interface ProductoRemotoRepository {
    suspend fun enviar(fila: Producto): ResultadoEnvio

    /** Catalogo completo del servidor. */
    suspend fun listar(): List<Producto>
}
