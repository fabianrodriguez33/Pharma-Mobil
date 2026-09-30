package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(limite: Int = 10): List<ProductoDto> {
        return client.get("products") {
            parameter("offset", 0)
            parameter("limit", limite)
        }.body()
    }
}
