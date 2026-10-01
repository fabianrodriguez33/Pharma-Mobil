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
