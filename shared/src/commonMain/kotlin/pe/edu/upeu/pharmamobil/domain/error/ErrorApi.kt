package pe.edu.upeu.pharmamobil.domain.error

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobil.data.remote.dto.ErrorResponseDto

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
    data class Desconocido(val detalle: String) : ErrorApi
}

/** Unico tipo que cruza hacia presentation: no expone nada de Ktor. */
class ErrorApiException(val error: ErrorApi) : Exception(error.toString())

suspend fun Throwable.toErrorApi(): ErrorApi {
    // CRITICO: re-lanzar CancellationException para no bloquear corrutinas al salir de pantalla.
    // HttpRequestTimeoutException hereda de IOException, no de CancellationException.
    if (this is CancellationException) throw this

    return when (this) {
        is ClientRequestException -> {
            val status = response.status.value
            val cuerpo = runCatching { response.body<ErrorResponseDto>() }.getOrNull()
            when (status) {
                400 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
                404 -> ErrorApi.NoEncontrado
                409 -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida")
                else -> ErrorApi.Desconocido(cuerpo?.message ?: "Error cliente HTTP $status")
            }
        }
        is ServerResponseException -> ErrorApi.Servidor
        is RedirectResponseException -> ErrorApi.Desconocido("Redirección inesperada")
        is ConnectTimeoutException, is SocketTimeoutException, is HttpRequestTimeoutException ->
            ErrorApi.TiempoAgotado
        else -> {
            val msg = message.orEmpty().lowercase()
            if (msg.contains("unable to resolve host") || msg.contains("failed to connect") || msg.contains("connection refused")) {
                ErrorApi.SinConexion
            } else {
                ErrorApi.Desconocido(message ?: "Error de red no identificado")
            }
        }
    }
}

/** Ejecuta una llamada remota y traduce cualquier fallo a [ErrorApiException]. */
internal suspend fun <T> traducirErrores(bloque: suspend () -> T): T = try {
    bloque()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    throw ErrorApiException(e.toErrorApi())
}
