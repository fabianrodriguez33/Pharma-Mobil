package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    almacen: AlmacenSeguro? = null,
    onSesionExpirada: () -> Unit = {}
): HttpClient {
    return HttpClient(engine) {
        // 4xx/5xx lanzan ClientRequestException/ServerResponseException (se traducen a ErrorApi)
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
                // estado y categoriaId tienen valor por defecto pero el backend los exige
                encodeDefaults = true
            })
        }

        install(Logging) {
            logger = ConsolaLogger
            level = LogLevel.HEADERS
            // El token nunca debe llegar a Logcat ni a la consola de Xcode.
            sanitizeHeader { it.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }

        if (almacen != null) {
            install(Auth) {
                bearer {
                    loadTokens { almacen.leerToken()?.let { BearerTokens(it, null) } }
                    // Adjuntar el token desde la primera peticion, sin esperar al 401; el login va sin token.
                    sendWithoutRequest { request -> !request.url.encodedPath.endsWith(RUTA_LOGIN) }
                }
            }
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { error, request ->
                if (error is ClientRequestException &&
                    error.response.status == HttpStatusCode.Unauthorized &&
                    !request.url.encodedPath.endsWith(RUTA_LOGIN)
                ) {
                    onSesionExpirada()
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }

        install(DefaultRequest) {
            url(baseUrl)
            header(HttpHeaders.ContentType, "application/json")
        }
    }
}

private const val RUTA_LOGIN = "auth/login"

/** Ktor cachea el token cargado; hay que invalidarlo tras login/logout. */
fun HttpClient.limpiarTokenEnCache() {
    authProvider<BearerAuthProvider>()?.clearToken()
}

/** SLF4J no imprime en Android/iOS; println llega a Logcat y a la consola de Xcode. */
private object ConsolaLogger : Logger {
    override fun log(message: String) {
        println("HttpClient: $message")
    }
}
