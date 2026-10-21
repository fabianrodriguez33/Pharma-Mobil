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

### Capacidades nativas (Guia 09)

Tres capacidades con dos estrategias de KMP para acceder a la plataforma:

1. **`expect/actual` (funcion pura):** `formatearSoles(valor: Double): String` formatea precios como moneda peruana (`S/ 10.00`).
2. **Interfaz + Koin (capacidad con contexto):** `Compartidor` (en `domain`) se implementa por plataforma y se inyecta desde cada `platformModule`.

El formato se aplica en `presentation` (`ProductosViewModel` entrega el precio ya formateado y arma el texto a compartir con `comoTextoParaCompartir()`); `presentation` no importa `android.*` ni `platform.UIKit.*`.

| Elemento | commonMain | androidMain | iosMain |
|---|---|---|---|
| Formato de moneda | `platform/Formato.kt` (`expect`) | `platform/Formato.android.kt` (`NumberFormat`, `es_PE`) | `platform/Formato.ios.kt` (`NSNumberFormatter`, `PEN`) |
| Compartir | `domain/platform/Compartidor.kt` (interfaz) | `platform/CompartidorAndroid.kt` (`Intent.ACTION_SEND`) | `platform/CompartidorIos.kt` (`UIActivityViewController`) |
| Registro en Koin | `di/AppModule.kt` (`expect val platformModule`) | `di/PlatformModule.android.kt` | `di/PlatformModule.ios.kt` |
| Texto a compartir | `presentation/productos/TextoParaCompartir.kt` | | |

### Código específico de plataforma

Tercera capacidad: **`InfoDispositivo`** (`expect class`) entrega sistema operativo, versión y modelo. Se muestra en el catálogo con el botón «Acerca del dispositivo».

| Capacidad | commonMain | androidMain | iosMain |
|---|---|---|---|
| Formato de moneda | `expect fun formatearSoles` | `NumberFormat` (`es_PE`) | `NSNumberFormatter` (`es_PE`) |
| Compartir | `interface Compartidor` | `Intent.ACTION_SEND` + `androidContext()` | `UIActivityViewController` |
| Info del dispositivo | `expect class InfoDispositivo` | `Build.VERSION.RELEASE`, `Build.MODEL` | `UIDevice.currentDevice` |

El informe está en `docs/entrega/S09_ActividadAutonoma_RodriguezBazan.docx`.

Uso: en "Catalogo API" cada tarjeta muestra el precio en soles y un icono de Compartir.

## Autenticación (Guía 10)

PharmaMobil se autentica contra **PharmaSoft** con JWT (Bearer). Rama de entrega: `feature/auth-jwt-rodriguez`.

### Flujo

1. Al arrancar, `App` observa `SesionViewModel.sesion`. Sin sesión se muestra `LoginScreen`; con sesión, la app.
2. `POST /api/v1/auth/login` con `{ "username", "password" }` devuelve `token`, `tipo`, `expiraEnSegundos`, `username`, `nombreCompleto`, `roles` y `permisos`.
3. El token se guarda con `AlmacenSeguro`; el perfil (usuario, nombre, permisos) se guarda aparte para restaurar la sesión al reabrir la app.
4. El plugin **Auth (Bearer)** de Ktor adjunta `Authorization: Bearer <token>` en cada petición.
5. Un **401** fuera del login limpia la sesión y vuelve al Login con aviso; un **403** mantiene la sesión y muestra un mensaje distinto.

### Componentes

| Capa | Pieza | Archivo |
|---|---|---|
| domain | `Sesion` (`puede(permiso)`), `Credenciales` | `domain/model/Sesion.kt` |
| domain | `AuthRepository` | `domain/repository/AuthRepository.kt` |
| domain | `IniciarSesion`, `CerrarSesion`, `ObtenerSesion` | `domain/usecase/*UseCase.kt` |
| domain | `AlmacenSeguro` (interfaz) | `domain/platform/AlmacenSeguro.kt` |
| domain | `ErrorApi.NoAutenticado` (401), `ErrorApi.SinPermiso` (403) | `domain/error/ErrorApi.kt` |
| data | `LoginRequestDto`, `LoginResponseDto` | `data/remote/dto/LoginDto.kt` |
| data | `AuthApi`, `AuthRepositoryRest`, `EventosSesion` | `data/remote/`, `data/repository/` |
| data | Cliente Ktor: Auth Bearer, Logging saneado, validador 401 | `data/remote/HttpClientFactory.kt` |
| androidMain | `AlmacenSeguroAndroid` (Android Keystore + AES-GCM) | `platform/AlmacenSeguroAndroid.kt` |
| iosMain | `AlmacenSeguroIos` (Keychain) | `platform/AlmacenSeguroIos.kt` |
| presentation | `LoginViewModel`, `LoginScreen`, `SesionViewModel` | `presentation/login/` |

### Configuración del cliente Ktor

- `install(Auth) { bearer { loadTokens { ... }; sendWithoutRequest { ruta != auth/login } } }`: el token viaja desde la primera petición, salvo en el login.
- `clearToken()` del proveedor Bearer tras guardar el token (login) y tras borrarlo (logout), porque Ktor cachea el valor cargado.
- `install(Logging) { level = HEADERS; sanitizeHeader { Authorization } }`: en Logcat/Xcode aparece `Authorization: ***`.
- `HttpResponseValidator`: un `401` fuera de `auth/login` notifica `EventosSesion.expirada`.

### Permisos en la interfaz

| Permiso | Efecto en la UI |
|---|---|
| `CATALOGO_ESCRIBIR` | Muestra el formulario de registro y las acciones editar/eliminar del inventario |
| `CLIENTES_LEER` | Muestra la pestaña y el acceso rápido «Clientes» |
| `VENTAS_LEER` | Muestra la pestaña y el acceso rápido «Pedidos» |

### Usuarios de prueba (PharmaSoft)

| Usuario | Clave | Rol | Permisos |
|---|---|---|---|
| `admin` | `Admin123` | ADMIN | todos |
| `vendedor` | `Vendedor123` | VENDEDOR | `CATALOGO_LEER`, `CLIENTES_LEER`, `CLIENTES_ESCRIBIR`, `VENTAS_LEER`, `VENTAS_REGISTRAR` |
| `almacen` | `Almacen123` | ALMACENERO | `CATALOGO_LEER`, `CATALOGO_ESCRIBIR` |

Claves de desarrollo definidas en `V3__datos_iniciales_seguridad.sql` del backend.

### Verificación por rol (HTTP contra PharmaSoft)

| Operación | admin | vendedor | almacen |
|---|---|---|---|
| Listar productos | 200 | 200 | 200 |
| Registrar producto | 201 | 403 | 409 (autorizado; nombre duplicado) |
| Listar clientes | 200 | 200 | 403 |
| Registrar venta (cuerpo vacío) | 400 (autorizado) | 400 (autorizado) | 403 |
| Ver reportes | 200 | 403 | 403 |
| `/auth/me` | 200 | 200 | 200 |

Sin token: 401.

### Cómo ejecutarlo

1. **Backend:** PharmaSoft con Oracle Free 23ai en el puerto 1522 (contenedor `gvenzl/oracle-free`, usuario `PHARMADB`) y `./mvnw spring-boot:run` (puerto 8080).
2. **Emulador Android:** la URL base es `http://10.0.2.2:8080/` (tráfico en claro permitido en el manifiesto de desarrollo). En un celular físico: `adb reverse tcp:8080 tcp:8080`.
3. **App:** `./gradlew :androidApp:assembleDebug` e instalar el APK; iniciar sesión con un usuario de la tabla.

### Pruebas

```bash
./gradlew testAndroidHostTest assembleDebug
```

Incluye `LoginViewModelTest` (login correcto, credenciales inválidas, campos vacíos, permisos de `Sesion`) y `AppModuleTest` (grafo de Koin con el almacén sustituido en JVM).

### Evidencias y limitaciones

- Informe y capturas: `docs/entrega/S10_Guia10_Autenticacion_RodriguezBazan.docx` y `docs/guia10/` (Android, PharmaSoft real).
- **iOS:** `AlmacenSeguroIos` (Keychain) está implementado pero no se compiló ni probó, porque requiere un Mac con Xcode.
- Los módulos de clientes, ventas y reportes no consumen aún el backend (clientes usa un repositorio en memoria).

## Seguridad y Gestión de Tokens JWT

### AlmacenSeguro
- Interfaz en `commonMain` (`guardarToken`, `leerToken`, `borrarToken`, más `guardarPerfil`/`leerPerfil`); una implementación por plataforma inyectada con Koin.
- **Android:** AES-256 en modo GCM con una clave del Android Keystore (alias `pharmamobil_token_key`, no exportable). El IV (12 bytes) se antepone al texto cifrado y el Base64 resultante se guarda en `shared_prefs/almacen_seguro.xml`; los nombres de clave se ofuscan con SHA-256, de modo que el archivo no revela el token ni el perfil. Si la clave se invalida o el dato se corrompe, se descarta y se pide un nuevo login.
- **iOS:** Keychain (`kSecClassGenericPassword`, accesible tras el primer desbloqueo, solo en este dispositivo). Implementado; pendiente de compilar en un Mac.

### Saneamiento de logs
`Logging` en nivel `HEADERS` con `sanitizeHeader { Authorization }`: en Logcat/Xcode aparece `Authorization: ***` y el token nunca se imprime.

### Guardia de navegación y cierre de sesión
- `App` observa `SesionViewModel.sesion`: sin sesión solo se muestra `LoginScreen`.
- Un **401** fuera de `auth/login` (token expirado, manipulado, ausente o usuario deshabilitado) llama a `cerrarSesion()` y vuelve al Login con aviso. Un **403** mantiene la sesión.
- `cerrarSesion()` aplica cuatro efectos: borra el token del almacén, deja `Sesion` en `null`, redirige a Login e invalida la caché del plugin Auth con `authProvider<BearerAuthProvider>()?.clearToken()`. Los cuatro están cubiertos por `CerrarSesionTest`.

### Verificación (Actividad Autónoma 10)
Bitácora de los 6 escenarios de token, evidencia del almacenamiento seguro, cierre de sesión y matriz por rol en `docs/entrega/S10_ActividadAutonoma_RodriguezBazan.docx` (capturas y script generador en `docs/actividad10/`).

## Persistencia offline-first

La base SQLite local (`pharma.db`, gestionada con SQLDelight) es la única fuente de verdad del catálogo; la red solo sincroniza.

- **Esquema:** `shared/src/commonMain/sqldelight/.../db/Producto.sq`. La columna `pendiente` marca lo creado sin red (`1`) frente a lo sincronizado (`0`).
- **Drivers:** `CreadorDeDriver` con `AndroidSqliteDriver`, `NativeSqliteDriver` (iOS) y `JdbcSqliteDriver` en memoria para las pruebas.
- **Capa local:** `ProductoLocal` expone `observar(): Flow<List<Producto>>`; no importa Ktor ni DTOs.
- **Repositorio:** `ProductoRepositorioOffline`. `registrar()` guarda con id local negativo y `pendiente = 1`; `sincronizar()` **sube primero los pendientes** y luego descarga y reemplaza los sincronizados dentro de una transacción.
- **Pantallas:** `ProductosViewModel` y `ProductoViewModel` recogen el `Flow` de la base y no recargan la lista a mano. Sin red se muestra «Sin conexión: mostrando datos guardados».
- **Indicador de sincronización:** la marca de la última sincronización exitosa (epoch millis) vive en la tabla `sincronizacion` de la misma base (migración `1.sqm`, esquema v2). El encabezado del catálogo muestra «Nunca sincronizado», «Actualizado hace X minutos» o «Actualizado hoy a las HH:mm», y el botón «Sincronizar» fuerza el ciclo subir → bajar.
- **Sesión vencida:** si la sincronización recibe `401`, la app vuelve al login con aviso y **no borra** lo pendiente; al reingresar se envía.
- **Edición/eliminación sin red:** un producto *pendiente* se edita en local y viaja en el `POST`; uno *ya sincronizado* exige conexión (se informa y no se altera nada).
- **Pruebas:** `ProductoRepositorioOfflineTest` (6 pruebas del enunciado + 2 de apoyo; `commonTest`, `JdbcSqliteDriver` en memoria + `MockEngine`), `TextoSincronizacionTest` y `ProductoViewModelOfflineTest`.
- **Informe de la Actividad Autónoma 11:** `docs/entrega/S11_ActividadAutonoma_RodriguezBazan.docx` (generado con `build_doc_s11.py`).

```bash
./gradlew testAndroidHostTest assembleDebug
```
