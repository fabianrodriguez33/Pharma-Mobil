# UNIVERSIDAD PERUANA UNIÓN
## Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas
### Desarrollo de Aplicaciones Móviles

**INFORME TÉCNICO: ACTIVIDAD AUTÓNOMA N.º 07**  
*Documentación de endpoints, DTO y pruebas de conexión*

**Estudiante:** Fabian Rodríguez Bazán  
**Curso:** Desarrollo de Aplicaciones Móviles  
**Sesión:** 07 (Unidad 2, Sesión 1)  
**Fecha:** 01/10/2026  
**Repositorio:** https://github.com/fabianrodriguez33/Pharma-Mobil

<<PAGEBREAK>>

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
| `category` | `CategoriaDto?` | No | `null` | N/A | Descarte del subobjeto de categoría. |
| N/A | `Int` | N/A | `0` | `Producto.stock` | La API no expone stock; se asigna 0 en la capa de datos. |

#### Tabla de Mapeo: `CategoriaDto` (subobjeto `category`)
| Campo JSON | Tipo en Kotlin (DTO) | Obligatorio | Valor por Defecto | Campo Dominio | Transformación / Regla de Negocio |
|:-----------|:--------------------:|:-----------:|:-----------------:|:--------------|:----------------------------------|
| `id` | `Int` | Sí | N/A | N/A | No se mapea al dominio. |
| `name` | `String` | Sí | N/A | N/A | No se mapea al dominio. |

Los demás campos de `category` que envía la API (`slug`, `image`, `creationAt`, `updatedAt`) no están declarados en el DTO y se ignoran con `ignoreUnknownKeys`.

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
| 1 | **Éxito HTTP 200 (Carga Normal)** | Red activa -> abrir "Catálogo" en el emulador Pixel 8. | `200 OK` con JSON de productos. | Log: `RESPONSE: 200`. La cuadrícula muestra 10 productos con imagen y precio (Evidencias 1 y 2). | Estado `Success`. El usuario ve el catálogo; no hay mensaje de error. |
| 2 | **Error HTTP 404 (Recurso no existe)** | Temporalmente se pidió `GET /products` como `/productos` con `expectSuccess = true` (código revertido después). | `404 Not Found` y `ClientRequestException`. | Log: `RESPONSE: 404` (Evidencia 4). | Estado `Error`. El usuario ve «No pudimos cargar los productos» y debajo: `Client request(GET https://api.escuelajs.co/api/v1/productos?offset=0&limit=10) invalid: 404 . Text: {"message":"Cannot GET /api/v1/productos?offset=0&limit=10","error":"Not Found","statusCode":404}`, con el botón «Reintentar». |
| 3 | **Sin Conexión / Modo Avión** | Modo Avión activado con `adb` -> abrir "Catálogo". | `UnknownHostException`. | Log: `failed with exception: java.net.UnknownHostException: Unable to resolve host` (Evidencias 1 y 3). | Estado `Error`. El usuario ve «No pudimos cargar los productos» y debajo: `Unable to resolve host "api.escuelajs.co": No address associated with hostname`, con el botón «Reintentar» y el icono de nube tachada. |
| 4 | **Timeout de Conexión** | Temporalmente `requestTimeoutMillis = 1` ms (revertido después) -> abrir "Catálogo". | Excepción de tiempo agotado de Ktor. | Log: `failed with exception: java.util.concurrent.CancellationException: Request timeout has expired` (Evidencia 5). | Estado `Error`. El usuario ve «No pudimos cargar los productos» y debajo: `Request timeout has expired [url=https://api.escuelajs.co/api/v1/products?offset=0&limit=10, request_timeout=1 ms]`, con el botón «Reintentar». |
| 5 | **JSON con Campo Nuevo / Desconocido** | La API devuelve campos no modelados (`slug`, `creationAt`, `updatedAt`, `category.slug`, etc.). | `ignoreUnknownKeys = true` los ignora. | Deserialización correcta: los 10 productos se renderizan sin fallas (Evidencia 2). | Estado `Success`. El usuario ve el catálogo normal; no hay mensaje de error. |

Nota: en la app los mensajes de error son el texto de la excepción (`e.message`) bajo el título «No pudimos cargar los productos». Los escenarios 2 y 4 se provocaron con modificaciones temporales del código, que no se incluyen en el repositorio.

---

### PRODUCTO 4. EVIDENCIAS Y REPOSICIONAMIENTO GIT
- **Entorno de pruebas:** las pruebas de conectividad se ejecutaron exclusivamente en el entorno Android (Pixel 8), por restricciones de sistema operativo; no se incluyen capturas de iOS.
- **Android Execution:** ejecutado en emulador Android (Pixel 8) con la sección "Catálogo" (Evidencias 1 y 2).
- **Manejo de Errores:** probado con Modo Avión; `UnknownHostException` interceptada y mostrada con botón "Reintentar" (Evidencia 3); también se probaron el error 404 (Evidencia 4) y el timeout (Evidencia 5).
- **Control de Calidad Git:** repositorio actualizado en la rama `feature/ktor-client`.
