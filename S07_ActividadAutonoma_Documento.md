# UNIVERSIDAD PERUANA UNIÓN
## Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas
### Desarrollo de Aplicaciones Móviles

**INFORME TÉCNICO: ACTIVIDAD AUTÓNOMA N.º 07**  
*Documentación de endpoints, DTO y pruebas de conexión*

---

### DATOS GENERALES
- **Asignatura:** Desarrollo de Aplicaciones Móviles
- **Unidad:** 2. Conectividad, CRUD REST y persistencia multiplataforma
- **Sesión:** 07 (Unidad 2, Sesión 1)
- **Proyecto:** PharmaMobil (`pe.edu.upeu.pharmamobil`)
- **Rama Git:** `feature/ktor-client`

---

### PRODUCTO 1. CATÁLOGO DE ENDPOINTS
Documentación del contrato de servicios para el recurso `Producto` consumido por la aplicación KMP (URL base `https://api.escuelajs.co`):

| N.º | Método | Ruta / Endpoint | Parámetros | Código Esperado | Descripción y Ejemplo de Respuesta |
|:---:|:------:|:----------------|:-----------|:---------------:|:-----------------------------------|
| 1 | `GET` | `/api/v1/products` | `limit` (Query Int), `offset` (Query Int) | `200 OK` | Retorna la lista paginada de productos en JSON. |
| 2 | `GET` | `/api/v1/products/{id}` | `id` (Path Int) | `200 OK` / `404 Not Found` | Retorna el detalle del producto solicitado por ID. |
| 3 | `POST` | `/api/v1/products/` | Body JSON (`title`, `price`, `description`, `categoryId`, `images`) | `201 Created` / `400 Bad Request` | Registra un nuevo producto en el servidor. |
| 4 | `PUT` | `/api/v1/products/{id}` | `id` (Path Int), Body JSON | `200 OK` / `400 Bad Request` | Actualiza un producto existente de forma completa. |
| 5 | `DELETE` | `/api/v1/products/{id}` | `id` (Path Int) | `200 OK` | Elimina un producto del catálogo por su ID. |

Actualmente la app implementa el endpoint 1 (`ProductoApi.obtenerProductos`); los demás quedan documentados para las siguientes sesiones.

---

### PRODUCTO 2. DICCIONARIO DE DTO
Mapeo de transferencia de datos (`ProductoDto`) hacia el modelo de dominio (`Producto`):

#### Tabla de Mapeo
| Campo JSON | Tipo en Kotlin (DTO) | Obligatorio | Valor por Defecto | Campo Dominio | Transformación / Regla de Negocio |
|:-----------|:--------------------:|:-----------:|:-----------------:|:--------------|:----------------------------------|
| `id` | `Int` | Sí | N/A | `Producto.id` | Conversión `toLong()`. |
| `title` | `String` | Sí | N/A | `Producto.nombre` | Si está en blanco se usa `"Sin nombre"`. |
| `price` | `Double` | Sí | N/A | `Producto.precio` | El dominio exige precio > 0; los productos inválidos se descartan en `toDomainValidos()`. |
| `description` | `String` | No | `""` | N/A | Se omite para mantener el dominio ligero. |
| `images` | `List<String>` | No | `emptyList()` | `Producto.imagen` | Se toma la primera imagen (o cadena vacía). |
| `category` | `CategoriaDto?` (`id`, `name`) | No | `null` | N/A | Descarte del subobjeto de categoría. |
| N/A | `Int` | N/A | `0` | `Producto.stock` | La API no expone stock; se asigna 0 en la capa de datos. |

#### Fragmento del JSON Real devuelto por el servidor
```json
[
  {
    "id": 2,
    "title": "Classic Red Pullover Hoodie",
    "slug": "classic-red-pullover-hoodie",
    "price": 15,
    "description": "Elevate your casual wardrobe with our Classic Red Pullover Hoodie. (...)",
    "category": {
      "id": 1,
      "name": "nuevo",
      "slug": "nuevo",
      "image": "https://i.imgur.com/QkIa5tT.jpeg",
      "creationAt": "2026-10-01T09:55:21.000Z",
      "updatedAt": "2026-10-01T16:58:47.000Z"
    },
    "images": [
      "https://i.imgur.com/1twoaDy.jpeg"
    ],
    "creationAt": "2026-10-01T09:55:21.000Z",
    "updatedAt": "2026-10-01T14:09:39.000Z"
  }
]
```

#### Código Fuente del DTO y Mapper en Kotlin
```kotlin
@Serializable
data class ProductoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    @SerialName("category") val categoria: CategoriaDto? = null
)

@Serializable
data class CategoriaDto(
    val id: Int,
    val name: String
)

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title.ifBlank { "Sin nombre" },
    precio = price,
    stock = 0,
    imagen = images.firstOrNull().orEmpty()
)

/** Descarta los productos que violarían los invariantes del dominio (p. ej. precio 0). */
fun List<ProductoDto>.toDomainValidos(): List<Producto> =
    mapNotNull { dto -> runCatching { dto.toDomain() }.getOrNull() }
```

---

### PRODUCTO 3. BITÁCORA DE PRUEBAS DE CONEXIÓN

| N.º | Escenario de Prueba | Pasos Seguidos | Resultado Esperado | Resultado Observado | Conclusión / Impacto en la UI |
|:---:|:-------------------|:---------------|:-------------------|:--------------------|:------------------------------|
| 1 | **Éxito HTTP 200 (Carga Normal)** | Red activa -> abrir "Catálogo" en el emulador Pixel 8. | `200 OK` con JSON de productos. | Log: `REQUEST .../products?offset=0&limit=10` y `RESPONSE: 200`. La cuadrícula muestra 10 productos con imagen y precio (Evidencia 1 y 2). | Estado `Success`: la interfaz despliega el catálogo. |
| 2 | **Error HTTP 404 (Recurso no existe)** | `GET /api/v1/products/999999` (curl; la app solo implementa el listado). | `404 Not Found`. | La API de práctica respondió **HTTP 400** con `EntityNotFoundError` (no existe la entidad). El código de error difiere del 404 esperado. | Ktor lanzaría `ClientRequestException` (4xx); la UI mostraría el estado `Error` con opción "Reintentar". |
| 3 | **Sin Conexión / Modo Avión** | Activar Modo Avión con `adb` -> abrir "Catálogo". | `UnknownHostException`. | Log: `failed with exception: java.net.UnknownHostException: Unable to resolve host`. Pantalla con icono de nube tachada, "No pudimos cargar los productos" y botón "Reintentar" (Evidencia 3). | Estado `Error`: la app no se cierra y permite reintentar al restaurar la red. |
| 4 | **Timeout de Conexión** | Cliente configurado con `requestTimeoutMillis = 15_000` (no se forzó a 1 ms en esta corrida). | `HttpRequestTimeoutException` al superarse el límite. | No ejecutado en el emulador; se documenta por configuración de `HttpTimeout`. | Estado `Error`: la excepción se propaga y la UI ofrece reintento. |
| 5 | **JSON con Campo Nuevo / Desconocido** | La API devuelve campos no modelados (`slug`, `creationAt`, `updatedAt`, `category.slug`, etc.). | `ignoreUnknownKeys = true` los ignora. | Deserialización correcta: los 10 productos se renderizan sin fallas (Evidencia 2). | Estado `Success`: tolerancia a cambios en la API. |

---

### PRODUCTO 4. EVIDENCIAS Y REPOSICIONAMIENTO GIT
- **Android Execution:** ejecutado en emulador Android (Pixel 8) con la sección "Catálogo" (Evidencias 1 y 2).
- **Manejo de Errores:** probado con Modo Avión; `UnknownHostException` interceptada y mostrada con botón "Reintentar" (Evidencia 3).
- **Control de Calidad Git:** repositorio actualizado en la rama `feature/ktor-client`.
