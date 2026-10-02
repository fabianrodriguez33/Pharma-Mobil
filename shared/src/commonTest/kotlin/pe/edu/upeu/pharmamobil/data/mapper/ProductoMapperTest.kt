package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoMapperTest {

    @Test
    fun elListadoOmiteLosProductosInactivosYLosInvalidos() {

        val dtos = listOf(
            ProductoResponseDto(id = 1, nombre = "Activo", precio = 5.0, stock = 1),
            ProductoResponseDto(id = 2, nombre = "Baja", precio = 5.0, stock = 1, estado = false),
            ProductoResponseDto(id = 3, nombre = "Precio cero", precio = 0.0, stock = 1)
        )

        assertEquals(listOf(1L), dtos.toDomainValidos().map { it.id })
    }

    @Test
    fun elRequestSiempreLlevaEstadoActivoYLaCategoriaIndicada() {

        val request = Producto(id = 0L, nombre = "Paracetamol", precio = 12.5, stock = 3)
            .toRequestDto(categoriaId = 7L)

        assertTrue(request.estado)
        assertEquals(7L, request.categoriaId)
        assertEquals("Paracetamol", request.nombre)
    }
}
