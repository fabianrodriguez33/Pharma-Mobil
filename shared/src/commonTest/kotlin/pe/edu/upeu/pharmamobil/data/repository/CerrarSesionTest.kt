package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.remote.AuthApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class AlmacenEnMemoria : AlmacenSeguro {
    var token: String? = null
    var perfil: String? = null
    override fun guardarToken(token: String) { this.token = token }
    override fun leerToken() = token
    override fun guardarPerfil(json: String) { perfil = json }
    override fun leerPerfil() = perfil
    override fun borrarToken() { token = null; perfil = null }
}

/** Verifica los 4 efectos de cerrarSesion() sin red: el motor es un MockEngine. */
class CerrarSesionTest {

    private val loginJson = """
        {"token":"jwt-de-prueba","tipo":"Bearer","expiraEnSegundos":3600,"username":"admin",
         "nombreCompleto":"Admin","roles":["ADMIN"],"permisos":["CATALOGO_LEER"]}
    """.trimIndent()

    @Test
    fun cerrarSesionAplicaLosCuatroEfectos() = runTest {
        val cabeceras = mutableListOf<String?>()
        val motor = MockEngine { request ->
            cabeceras += request.headers[HttpHeaders.Authorization]
            val cuerpo = if (request.url.encodedPath.endsWith("auth/login")) loginJson else "{}"
            respond(cuerpo, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val almacen = AlmacenEnMemoria()
        val client = crearHttpClient(motor, "http://localhost/", almacen)
        val repo = AuthRepositoryRest(AuthApi(client), almacen, client)

        repo.iniciarSesion(Credenciales("admin", "Admin123")).getOrThrow()
        assertEquals("jwt-de-prueba", almacen.leerToken())
        client.get("api/v1/productos").bodyAsText()
        assertEquals("Bearer jwt-de-prueba", cabeceras.last())

        repo.cerrarSesion()

        // Efecto 1: token borrado del almacen
        assertNull(almacen.leerToken())
        assertNull(almacen.leerPerfil())
        // Efecto 2: sesion en memoria en null
        assertNull(repo.sesionActual.value)
        assertNull(repo.obtenerSesion())
        // Efecto 4: la cache del plugin Auth se invalido; no se reutiliza el token anterior
        client.get("api/v1/productos").bodyAsText()
        assertNull(cabeceras.last())
        assertNotNull(cabeceras.first { it != null })
    }
}
