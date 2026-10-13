package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequestDto): LoginResponseDto {
        return client.post(RUTA_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    companion object {
        const val RUTA_LOGIN = "api/v1/auth/login"
    }
}
