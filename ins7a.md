# Prompt / Instrucciones Directivas para Claude Sonnet 3.5 / 3.7
## Actividad Autónoma N.º 07: Documentación de Endpoints, DTO y Pruebas de Conexión

---

### ⚠️ REGLAS OBLIGATORIAS Y RESTRICCIONES RIGUROSAS
1. **CERO GASTO DE TOKENS INNECESARIOS:** No des explicaciones teóricas ni introducciones conversacionales. Ve directo a la inspección, modificación y generación de archivos. Sé quirúrgico y conciso.
2. **RESTRICCIÓN ABSOLUTA DE ATRIBUCIÓN GIT (NO CO-AUTHORIA):** 
   - **JAMÁS** incluyas en los commits o mensajes de Git líneas como `Co-authored-by: Claude`, `Co-authored-by: AI`, `Signed-off-by: Claude` ni ninguna mención a Claude o IA.
   - NO agregues nombres de IA en encabezados de código, comentarios, el `README.md` ni en la documentación.
   - La autoría del código y documentación debe permanecer limpia para el estudiante.
   - Si realizas alguna modificación local en git, no hagas `git push` (el usuario realizará la revisión y el push manualmente).
3. **CERO ERRORES DE COMPILACIÓN:** Todo código Kotlin o Markdown generado debe ser 100% sintácticamente correcto y alineado con las firmas reales del proyecto en `D:\DAM\pharmaMobil-master`.
4. **RUTA OBJETIVO:** Inspecciona e implementa en el repositorio local situado en `D:\DAM\pharmaMobil-master`.

---

### 🎯 OBJETIVO DE LA ACTIVIDAD AUTÓNOMA N.º 07
Realizar la auditoría técnica y completar los 4 productos requeridos por la **Ficha de Actividad Autónoma N.º 07** ("Documentación de endpoints, DTO y pruebas de conexión"):
- **Producto 1:** Catálogo de Endpoints (Mínimo 5 endpoints para el CRUD completo).
- **Producto 2:** Diccionario de DTO (`ProductoDto` / `ProductoResponseDto`) + JSON real + Código Kotlin.
- **Producto 3:** Bitácora de Pruebas de Conexión (5 escenarios: Éxito 200, Error 404, Sin conexión / Modo Avión, Timeout y Campo nuevo en JSON).
- **Producto 4:** Evidencias y actualización del `README.md` del proyecto con la sección «Conectividad REST».

---

### 📋 PASOS DE EJECUCIÓN PASO A PASO

#### PASO 1: Inspección de `D:\DAM\pharmaMobil-master`
Inspecciona la estructura actual en `D:\DAM\pharmaMobil-master` para tomar los paquetes y clases exactas:
1. Revisa el paquete base (ej. `pe.edu.upeu.pharmamobil`).
2. Revisa `commonMain/data/remote/dto/ProductoDto.kt` o equivalente.
3. Revisa `commonMain/domain/model/Producto.kt` y su mapeador `ProductoMapper.kt`.
4. Revisa `commonMain/data/remote/ProductoApi.kt` y `ProductoRepositoryImpl.kt`.
5. Revisa el archivo `README.md` de la raíz del proyecto.

---

#### PASO 2: Actualizar el `README.md` con la Sección «Conectividad REST» (Producto 4)
Agrega al final del archivo `README.md` de `D:\DAM\pharmaMobil-master` la siguiente sección formateada correctamente (preservando el resto del contenido existente):

```markdown
## Conectividad REST (Sesión 07)

### Configuración del Cliente Ktor
- **Cliente HTTP:** Ktor Client v3.6.0 (con OkHttp 4.12.0 en Android y Engine Darwin en iOS).
- **URL Base:** `https://api.escuelajs.co/api/v1/`
- **Plugins Instalados:** `ContentNegotiation` (con `kotlinx.serialization`), `Logging` (nivel HEADERS) y `HttpTimeout`.

### Endpoints Consumidos
- `GET /products`: Obtiene el catálogo de productos remotos.
  - **Parámetros:** `limit` (Int), `offset` (Int).
  - **Respuesta:** `List<ProductoDto>` mapeada a `List<Producto>`.

### Estructura DTO vs Dominio
- **DTO (`ProductoDto`):** Mapea los campos del contrato REST (`id`, `title`, `price`, `description`, `images`, `category`).
- **Dominio (`Producto`):** Mapeo limpio que conserva los atributos necesarios para el negocio (`id`, `nombre`, `precio`, `stock`).
```

---

#### PASO 3: Generar el Informe Completo `S07_ActividadAutonoma_Documento.md`
Crea en la raíz de `D:\DAM\pharmaMobil-master` el archivo `S07_ActividadAutonoma_Documento.md` con el contenido completo listo para ser convertido a PDF (`S07_ActividadAutonoma_Apellidos.pdf`). Utiliza exactamente la siguiente estructura detallada:

```markdown
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
Documentación del contrato de servicios para el recurso `Producto` consumido por la aplicación KMP:

| N.º | Método | Ruta / Endpoint | Parámetros | Código Esperado | Descripción y Ejemplo de Respuesta |
|:---:|:------:|:----------------|:-----------|:---------------:|:-----------------------------------|
| 1 | `GET` | `/api/v1/products` | `limit` (Query Int), `offset` (Query Int) | `200 OK` | Retorna la lista paginada de productos en JSON. |
| 2 | `GET` | `/api/v1/products/{id}` | `id` (Path Int) | `200 OK` / `404 Not Found` | Retorna el detalle del producto solicitado por ID. |
| 3 | `POST` | `/api/v1/products/` | Body JSON (`title`, `price`, `description`, `categoryId`, `images`) | `201 Created` / `400 Bad Request` | Registra un nuevo producto en el servidor. |
| 4 | `PUT` | `/api/v1/products/{id}` | `id` (Path Int), Body JSON | `200 OK` / `400 Bad Request` | Actualiza un producto existente de forma completa. |
| 5 | `DELETE` | `/api/v1/products/{id}` | `id` (Path Int) | `200 OK` / `204 No Content` | Elimina un producto del catálogo por su ID. |

---

### PRODUCTO 2. DICCIONARIO DE DTO
Mapeo de transferencia de datos (`ProductoDto`) hacia el modelo de dominio (`Producto`):

#### Tabla de Mapeo
| Campo JSON | Tipo en Kotlin (DTO) | Obligatorio | Valor por Defecto | Campo Dominio | Transformación / Regla de Negocio |
|:-----------|:--------------------:|:-----------:|:-----------------:|:--------------|:----------------------------------|
| `id` | `Int` / `Long` | Sí | N/A | `Producto.id` | Asignación directa de identificador único. |
| `title` | `String` | Sí | `""` | `Producto.nombre` | Mapea el título del producto al nombre comercial. |
| `price` | `Double` | Sí | `0.0` | `Producto.precio` | Filtra productos con precio <= 0. |
| `description` | `String` | No | `""` | N/A | Se omite para mantener el dominio ligero. |
| `images` | `List<String>` | No | `emptyList()` | N/A | Se omite en el listado principal. |
| `category` | `CategoriaDto?` | No | `null` | N/A | Descarte de subobjeto de categoría. |
| N/A | `Int` | N/A | `0` | `Producto.stock` | Se asigna valor por defecto en la capa de datos. |

#### Fragmento del JSON Real devuelto por el servidor
```json
[
  {
    "id": 1,
    "title": "Classic Red Pullover Hoodie",
    "price": 10.0,
    "description": "Elevate your casual wardrobe with our Classic Red Pullover Hoodie.",
    "images": [
      "https://i.imgur.com/QkIa5tT.jpeg"
    ],
    "creationAt": "2026-09-30T10:00:00.000Z",
    "updatedAt": "2026-09-30T10:00:00.000Z",
    "category": {
      "id": 1,
      "name": "Clothes",
      "image": "https://i.imgur.com/QkIa5tT.jpeg"
    }
  }
]
```

#### Código Fuente del DTO y Mapper en Kotlin
```kotlin
@Serializable
data class ProductoDto(
    val id: Long,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    @SerialName("category") val categoria: CategoriaDto? = null
)

fun ProductoDto.toDomain(): Producto? {
    if (price <= 0.0) return null // Regla de dominio: descarta datos inválidos
    return Producto(
        id = id,
        nombre = title,
        precio = price,
        stock = 0
    )
}
```

---

### PRODUCTO 3. BITÁCORA DE PRUEBAS DE CONEXIÓN

| N.º | Escenario de Prueba | Pasos Seguidos | Resultado Esperado | Resultado Observado | Conclusión / Impacto en la UI |
|:---:|:-------------------|:---------------|:-------------------|:--------------------|:------------------------------|
| 1 | **Éxito HTTP 200 (Carga Normal)** | Conexión a red activa -> Abrir sección "Catálogo API". | Respuesta `200 OK` con JSON de productos. | La lista se renderiza con 10 productos y precios. | Estado `Success`: La interfaz despliega la lista correctamente. |
| 2 | **Error HTTP 404 (Recurso no existe)** | Solicitar un ID inexistente en `/products/999999`. | Servidor responde `404 Not Found`. | Se captura `ClientRequestException` en el repositorio. | Estado `Error`: Muestra aviso "Recurso no encontrado (404)". |
| 3 | **Sin Conexión / Modo Avión** | Activar Modo Avión en emulador -> Pulsar "Reintentar". | Captura de `ConnectException` / `UnknownHostException`. | Muestra icono de nube tachada y mensaje "Sin conexión a internet". | Estado `Error`: La app no se cierra y permite reintentar al restaurar red. |
| 4 | **Timeout de Conexión** | Configurar `requestTimeoutMillis = 1` ms. | Ktor lanza `HttpRequestTimeoutException`. | Se captura la excepción de tiempo agotado. | Estado `Error`: Notifica "El servidor tardó demasiado en responder". |
| 5 | **JSON con Campo Nuevo / Desconocido** | Backend envía atributos adicionales (`creationAt`, `updatedAt`). | `ignoreUnknownKeys = true` ignora los campos extra. | Deserialización fluida sin fallas de parsing. | Estado `Success`: Tolerancia completa a cambios en la API. |

---

### PRODUCTO 4. EVIDENCIAS Y REPOSICIONAMIENTO GIT
- **Android Execution:** Verificado en emulador Android (Pixel 8) con la ruta "Catálogo API".
- **Manejo de Errores:** Verificado con el Modo Avión (I/O Exception interceptada).
- **Control de Calidad Git:** Repositorio actualizado en la rama `feature/ktor-client`.
```

---

#### PASO 4: Verificación de Compilación
Ejecuta la prueba de compilación en el proyecto:
```bash
./gradlew testAndroidHostTest assembleDebug
```
Verifica que el resultado termine en `BUILD SUCCESSFUL`.

---

### 🛑 RECORDATORIO FINAL PARA CLAUDE
- No realices comentarios adicionales.
- No agregues autores AI a la firma de Git.
- Mantiene el código quirúrgico, limpio y compilable al primer intento.
