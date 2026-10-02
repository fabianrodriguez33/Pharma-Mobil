This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

### Cliente Ktor y consumo GET (Guia 07)

- **URL base:** `https://api.escuelajs.co/api/v1/` (Platzi Fake Store API)
- **Endpoint consumido:** `GET /products?offset=0&limit=10` (completo: `https://api.escuelajs.co/api/v1/products?offset=0&limit=10`)
- **Motores:** OkHttp en Android, Darwin en iOS (se inyectan con Koin en `platformModule`).
- **Pantalla:** menu lateral > "Catalogo API" (`ProductosScreen`), con estados Loading, Success y Error con boton "Reintentar".

Campos del DTO (`ProductoDto`, `data/remote/dto`):

| Campo JSON | Propiedad Kotlin | Tipo | Notas |
|---|---|---|---|
| `id` | `id` | `Int` | |
| `title` | `title` | `String` | |
| `price` | `price` | `Double` | |
| `description` | `description` | `String` | por defecto `""` |
| `images` | `images` | `List<String>` | por defecto lista vacia |
| `category` | `categoria` | `CategoriaDto?` (`id: Int`, `name: String`) | opcional |

El mapper (`ProductoDto.toDomain()`) convierte el DTO al modelo de dominio `Producto` (`id`, `nombre`, `precio`, `stock`); el dominio no tiene anotaciones de serializacion.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Conectividad REST (Sesión 07)

### Configuración del Cliente Ktor
- **Cliente HTTP:** Ktor Client v3.6.0 (motor OkHttp en Android y Darwin en iOS).
- **URL Base:** `https://api.escuelajs.co/api/v1/`
- **Plugins Instalados:** `ContentNegotiation` (con `kotlinx.serialization`), `Logging` (nivel HEADERS), `HttpTimeout` y `DefaultRequest`.

### Endpoints Consumidos
- `GET /products`: Obtiene el catálogo de productos remotos.
  - **Parámetros:** `limit` (Int), `offset` (Int).
  - **Respuesta:** `List<ProductoDto>` mapeada a `List<Producto>`.

### Estructura DTO vs Dominio
- **DTO (`ProductoDto`):** Mapea los campos del contrato REST (`id`, `title`, `price`, `description`, `images`, `category`).
- **Dominio (`Producto`):** Mapeo limpio que conserva los atributos necesarios para el negocio (`id`, `nombre`, `precio`, `stock`, `imagen`).

## CRUD REST PharmaSoft (Sesión 08)

- **URL base:** `http://10.0.2.2:8080/` en el emulador Android (equivale a `http://localhost:8080/` del equipo anfitrión). Recurso: `/api/v1/productos`.
- **Backend:** PharmaSoft (Spring Boot + Oracle Free). La baja es lógica: el producto queda con `estado = false` y deja de listarse en la app.

| Operación | Método y ruta | Payload / DTO | Éxito |
|---|---|---|---|
| Listar | `GET /api/v1/productos?pagina=0&tamanio=20` | `PaginaResponseDto<ProductoResponseDto>` | `200 OK` |
| Obtener | `GET /api/v1/productos/{id}` | `ProductoResponseDto` | `200 OK` |
| Crear | `POST /api/v1/productos` | `ProductoRequestDto` → `ProductoResponseDto` | `201 Created` |
| Editar | `PUT /api/v1/productos/{id}` | `ProductoRequestDto` → `ProductoResponseDto` | `200 OK` |
| Eliminar | `DELETE /api/v1/productos/{id}` | sin cuerpo (no se deserializa JSON) | `204 No Content` |

## Manejo de Errores

Todo fallo de red o HTTP se traduce en `data/repository` a `ErrorApiException(ErrorApi)`; la capa de presentación nunca ve tipos de Ktor. `toErrorApi()` relanza `CancellationException` antes del catch genérico.

| Origen | `ErrorApi` | Mensaje al usuario |
|---|---|---|
| `400` con `validationErrors` | `Validacion(porCampo)` | Mensaje bajo `nombreError`, `precioError` o `stockError` |
| `404` | `NoEncontrado` | "El producto ya no existe" |
| `409` (regla de negocio, duplicado, ya inactivo) | `Conflicto(mensaje)` | Mensaje enviado por el servidor |
| `5xx` | `Servidor` | "El servidor no está disponible. Inténtalo más tarde" |
| `ConnectException` / host no resuelto | `SinConexion` | "Sin conexión con el servidor" |
| `HttpRequestTimeoutException` / `ConnectTimeoutException` | `TiempoAgotado` | "El servidor tardó demasiado en responder" |
| otros | `Desconocido(detalle)` | detalle |
| `CancellationException` | (se relanza, sin `ErrorApi`) | ninguno |

### Estado de pantalla: `Fase` y `Operacion`

`ProductoUiState` separa dos ejes independientes:

- **`Fase`** (qué se muestra): `Cargando`, `SinProductos`, `ConProductos(lista)`, `Error(mensaje)`.
- **`Operacion`** (qué acción corre): `Inactiva`, `EnCurso(Tipo.Crear | Actualizar | Eliminar)`, `Fallida(mensaje)`.

Los errores 400 caen en el formulario sin cambiar la `Fase`; los fallos de red o conflictos quedan en `Operacion.Fallida` sin tocar la lista.

### Pruebas automatizadas

```bash
./gradlew testAndroidHostTest assembleDebug
```

El informe de la actividad está en `docs/entrega/S08_ActividadAutonoma_RodriguezBazan.docx`.
