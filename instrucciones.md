# PROMPT Y GUÍA DE EJECUCIÓN DIRECTA PARA CLAUDE SONNET

## DIRECTIVA DE EJECUCIÓN (SYSTEM PROMPT)
- **Modo de trabajo:** Ejecución directa y quirúrgica sin explicaciones redundantes ni rodeos.
- **Directorio de trabajo objetivo:** `D:\DAM\pharmaMobil-master` (o la ruta raíz del proyecto KMP en el entorno).
- **Prohibiciones estrictas:**
  1. NO realizar `git commit` ni `git push`.
  2. NO generar respuestas conversacionales largas ("quemar tokens").
  3. NO modificar el modelo de dominio en `domain/model/` agregando anotaciones de red/serialización (mantener el Dominio limpio).
- **Objetivo:** Implementar la **Guía Práctica N.º 07 - Cliente Ktor y consumo GET inicial** en el proyecto Kotlin Multiplatform `pharmaMobil` garantizando que compile y ejecute a la primera en Android e iOS.

---

## FASE 0: INSPECCIÓN INICIAL DEL PROYECTO
Antes de modificar archivos, inspecciona la estructura de `D:\DAM\pharmaMobil-master`:
1. Verifica los nombres exactos del módulo compartido (ej. `shared`, `core`, o `composeApp`).
2. Revisa el paquete base (ej. `com.pharmamobil` o `pe.upeu.pharmamobil`).
3. Revisa los archivos de dependencias (`build.gradle.kts` o `gradle/libs.versions.toml`).
4. Localiza los módulos existentes de Koin DI (`di/AppModule.kt` o similar).

---

## FASE 1: DEPENDENCIAS Y PERMISOS

### 1.1 Permiso de Internet (Android)
Asegúrate de que en `androidMain/AndroidManifest.xml` (o `composeApp/src/androidMain/AndroidManifest.xml`) esté presente:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 1.2 Configuración de Gradle (`build.gradle.kts` del módulo compartido)
Agrega el plugin de serialización y las dependencias de Ktor Client v3.6.0 (o compatible con el proyecto):

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.3.20" // Ajustar según la versión Kotlin del proyecto
}

val ktorVersion = "3.6.0"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            
            // kotlinx.serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        }
    }
}
```

---

## FASE 2: MOTORES POR PLATAFORMA (KOIN DI)

### 2.1 Engine Android (`androidMain/.../di/PlatformModule.android.kt`)
```kotlin
package pe.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<HttpClientEngine> { OkHttp.create() }
}
```

### 2.2 Engine iOS (`iosMain/.../di/PlatformModule.ios.kt`)
```kotlin
package pe.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<HttpClientEngine> { Darwin.create() }
}
```

---

## FASE 3: FACTORÍA DEL CLIENTE KTOR

Crea la factoría del cliente HTTP en `commonMain/data/remote/HttpClientFactory.kt`:

```kotlin
package pe.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }
        
        defaultRequest {
            // URL Base para la práctica (Platzi Fake Store API o PharmaSoft)
            url("https://api.escuelajs.co/api/v1/")
            header(HttpHeaders.ContentType, "application/json")
        }
    }
}
```

---

## FASE 4: CAPA DE DATOS - DTO Y MAPPER

### 4.1 DTOs (`commonMain/data/remote/dto/ProductoDto.kt`)
```kotlin
package pe.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
```

### 4.2 Mapper (`commonMain/data/mapper/ProductoMapper.kt`)
*Mapea de DTO a Modelo de Dominio sin contaminar el Dominio con anotaciones.*

```kotlin
package pe.upeu.pharmamobil.data.mapper

import pe.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.upeu.pharmamobil.domain.model.Producto

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    descripcion = description,
    imagen = images.firstOrNull() ?: "",
    categoria = categoria?.name ?: "Sin categoría"
)
```

---

## FASE 5: SERVICIO REMOTE Y REPOSITO

### 5.1 Service API (`commonMain/data/remote/ProductoApi.kt`)
```kotlin
package pe.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.upeu.pharmamobil.data.remote.dto.ProductoDto

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(limite: Int = 10): List<ProductoDto> {
        return client.get("products") {
            parameter("limit", limite)
        }.body()
    }
}
```

### 5.2 Repositorio Conectado (`commonMain/data/repository/ProductoRepositoryImpl.kt`)
```kotlin
package pe.upeu.pharmamobil.data.repository

import pe.upeu.pharmamobil.data.mapper.toDomain
import pe.upeu.pharmamobil.data.remote.ProductoApi
import pe.upeu.pharmamobil.domain.model.Producto
import pe.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val api: ProductoApi
) : ProductoRepository {

    override suspend fun listar(): Result<List<Producto>> = runCatching {
        api.obtenerProductos().map { it.toDomain() }
    }
}
```

---

## FASE 6: INYECCIÓN DE DEPENDENCIAS (KOIN DI)

Actualiza el módulo de datos de Koin en `commonMain/di/DataModule.kt` (o `AppModule.kt`):

```kotlin
package pe.upeu.pharmamobil.di

import org.koin.dsl.module
import pe.upeu.pharmamobil.data.remote.ProductoApi
import pe.upeu.pharmamobil.data.remote.crearHttpClient
import pe.upeu.pharmamobil.data.repository.ProductoRepositoryImpl
import pe.upeu.pharmamobil.domain.repository.ProductoRepository

val networkModule = module {
    single { crearHttpClient(get()) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositoryImpl(get()) }
}
```

---

## FASE 7: PRESENTACIÓN (VIEWMODEL & UISTATE EN COMPOSE)

### 7.1 UiState (`commonMain/presentation/productos/ProductosUiState.kt`)
```kotlin
package pe.upeu.pharmamobil.presentation.productos

import pe.upeu.pharmamobil.domain.model.Producto

sealed interface ProductosUiState {
    data object Loading : ProductosUiState
    data class Success(val productos: List<Producto>) : ProductosUiState
    data class Error(val mensaje: String) : ProductosUiState
}
```

### 7.2 ViewModel (`commonMain/presentation/productos/ProductosViewModel.kt`)
```kotlin
package pe.upeu.pharmamobil.presentation.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductosViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<ProductosUiState>(ProductosUiState.Loading)
    val estado: StateFlow<ProductosUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() = viewModelScope.launch {
        _estado.value = ProductosUiState.Loading
        repository.listar()
            .onSuccess { lista ->
                _estado.value = ProductosUiState.Success(lista)
            }
            .onFailure { error ->
                _estado.value = ProductosUiState.Error(
                    error.message ?: "Error al conectar con el servidor"
                )
            }
    }
}
```

---

## COTEJO FINAL DE VERIFICACIÓN (PUNTOS DE CONTROL)
1. **Compilación Clean Architecture:** El módulo `domain` no tiene ninguna importación de `io.ktor` ni `@Serializable`.
2. **Koin DI:** `HttpClient` e `HttpClientEngine` se resuelven como una única instancia `single`.
3. **Ejecución:**
   - Probar `./gradlew assembleDebug` o ejecutar el target Android.
   - Probar el target iOS en Xcode / Kotlin Multiplatform plugin.
4. **Respuesta 200:** Al arrancar la pantalla de productos, Ktor registra en consola la petición GET a `/products?limit=10` y deserializa la lista.
5. **Manejo de Errores:** Al activar el Modo Avión, la app muestra `ProductosUiState.Error` sin destruirse.

---
**RECORDATORIO IMPORTANTE PARA CLAUDE SONNET:**
- Aplica los cambios directamente en `D:\DAM\pharmaMobil-master`.
- No hagas `git commit` ni `git push`.
- No generes respuestas extensas innecesarias.
