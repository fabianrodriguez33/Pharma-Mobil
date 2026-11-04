package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobil.data.mapper.toDomainValidos
import pe.edu.upeu.pharmamobil.data.mapper.toRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.toErrorApi
import pe.edu.upeu.pharmamobil.domain.error.traducirErrores
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRemotoRepository

/** Traduce los errores de la API a las tres ramas de [ResultadoEnvio]. */
class EnvioRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefectoId: Long = 1L,
    private val baseUrl: String = ""
) : ProductoRemotoRepository {

    override suspend fun enviar(fila: Producto): ResultadoEnvio = try {
        ResultadoEnvio.Exito(api.crear(fila.toRequestDto(categoriaPorDefectoId)).id)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        e.toErrorApi().aResultado()
    }

    override suspend fun listar(): List<Producto> = traducirErrores { descargarTodo() }

    /** El backend limita el tamano de pagina a 100: se recorren todas. */
    private suspend fun descargarTodo(): List<Producto> {
        val todos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val respuesta = api.listar(pagina, TAMANIO_PAGINA)
            todos += respuesta.contenido.toDomainValidos(baseUrl)
            pagina++
        } while (!respuesta.ultima)
        return todos
    }

    private companion object {
        const val TAMANIO_PAGINA = 100
    }
}

internal fun ErrorApi.aResultado(): ResultadoEnvio = when (this) {
    ErrorApi.SinConexion -> ResultadoEnvio.Transitorio("Sin conexión con el servidor")
    ErrorApi.TiempoAgotado -> ResultadoEnvio.Transitorio("El servidor tardó demasiado en responder")
    ErrorApi.Servidor -> ResultadoEnvio.Transitorio("El servidor no está disponible")
    is ErrorApi.Validacion -> ResultadoEnvio.Permanente(
        porCampo.values.joinToString("; ").ifBlank { "El servidor rechazó los datos" }
    )
    ErrorApi.NoEncontrado -> ResultadoEnvio.Permanente("El recurso ya no existe en el servidor")
    is ErrorApi.Conflicto -> ResultadoEnvio.Permanente(mensaje)
    ErrorApi.SinPermiso -> ResultadoEnvio.Permanente("Sin permiso para esta operación")
    ErrorApi.NoAutenticado -> ResultadoEnvio.Permanente("Sesión vencida")
    is ErrorApi.Desconocido -> ResultadoEnvio.Permanente(detalle)
}
