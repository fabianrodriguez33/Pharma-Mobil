package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.remote.AuthApi
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.PerfilGuardadoDto
import pe.edu.upeu.pharmamobil.data.remote.limpiarTokenEnCache
import pe.edu.upeu.pharmamobil.domain.error.traducirErrores
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.resultadoDe

class AuthRepositoryRest(
    private val api: AuthApi,
    private val almacen: AlmacenSeguro,
    private val client: HttpClient
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _sesion = MutableStateFlow(restaurar())
    override val sesionActual: StateFlow<Sesion?> = _sesion.asStateFlow()

    override suspend fun iniciarSesion(credenciales: Credenciales): Result<Sesion> = resultadoDe {
        // Un token viejo no debe viajar en la peticion de login.
        almacen.borrarToken()
        client.limpiarTokenEnCache()

        val respuesta = traducirErrores {
            api.login(LoginRequestDto(credenciales.usuario.trim(), credenciales.clave))
        }
        val sesion = Sesion(
            token = respuesta.token,
            usuario = respuesta.username,
            nombreCompleto = respuesta.nombreCompleto.ifBlank { respuesta.username },
            permisos = respuesta.permisos.toSet()
        )
        almacen.guardarToken(sesion.token)
        almacen.guardarPerfil(
            json.encodeToString(
                PerfilGuardadoDto.serializer(),
                PerfilGuardadoDto(sesion.usuario, sesion.nombreCompleto, sesion.permisos.toList())
            )
        )
        client.limpiarTokenEnCache()
        _sesion.value = sesion
        sesion
    }

    override fun obtenerSesion(): Sesion? = _sesion.value

    override fun cerrarSesion() {
        almacen.borrarToken()
        client.limpiarTokenEnCache()
        _sesion.value = null
    }

    private fun restaurar(): Sesion? {
        val token = almacen.leerToken() ?: return null
        val perfil = almacen.leerPerfil()?.let {
            runCatching { json.decodeFromString(PerfilGuardadoDto.serializer(), it) }.getOrNull()
        } ?: return null
        return Sesion(token, perfil.usuario, perfil.nombreCompleto, perfil.permisos.toSet())
    }
}
