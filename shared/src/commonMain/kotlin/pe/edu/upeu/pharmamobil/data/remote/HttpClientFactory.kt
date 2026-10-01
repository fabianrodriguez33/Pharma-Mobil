package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearHttpClient(engine: HttpClientEngine, baseUrl: String): HttpClient {
    return HttpClient(engine) {
        // 4xx/5xx lanzan ClientRequestException/ServerResponseException (se traducen a ErrorApi)
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
            })
        }

        install(Logging) {
            logger = ConsolaLogger
            level = LogLevel.HEADERS
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

/** SLF4J no imprime en Android/iOS; println llega a Logcat y a la consola de Xcode. */
private object ConsolaLogger : Logger {
    override fun log(message: String) {
        println("HttpClient: $message")
    }
}
