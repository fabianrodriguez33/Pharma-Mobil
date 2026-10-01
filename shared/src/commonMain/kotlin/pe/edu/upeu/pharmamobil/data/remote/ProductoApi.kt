package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto

class ProductoApi(private val client: HttpClient) {

    suspend fun listar(pagina: Int = 0, tamanio: Int = 20): PaginaResponseDto<ProductoResponseDto> {
        return client.get("api/v1/productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()
    }

    suspend fun obtenerPorId(id: Long): ProductoResponseDto {
        return client.get("api/v1/productos/$id").body()
    }

    suspend fun crear(request: ProductoRequestDto): ProductoResponseDto {
        return client.post("api/v1/productos") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun actualizar(id: Long, request: ProductoRequestDto): ProductoResponseDto {
        return client.put("api/v1/productos/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminar(id: Long) {
        // HTTP 204 No Content: no intentar hacer .body()
        client.delete("api/v1/productos/$id")
    }
}
