# Informe – Guía Práctica N.º 10: Autenticación JWT

**Alumno:** Julio Fabián Rodríguez Bazán
**Rama:** `feature/auth-jwt-rodriguez`
**Proyecto:** PharmaMobil (KMP + Ktor) consumiendo PharmaSoft

> **Alcance de las capturas.** Las capturas de este informe se tomaron en un emulador Android (Pixel 8)
> contra un **servidor simulado** (`servidor_simulado.py`) que reproduce el contrato de PharmaSoft
> (`POST /api/v1/auth/login`, `GET/POST /api/v1/productos`, respuestas 200/401/403 y los permisos de los tres
> usuarios de prueba). **No son capturas contra PharmaSoft real**, porque su base Oracle no estaba disponible
> en el equipo (ver «Pendientes»). Deben repetirse contra el backend real antes de entregar.

## 1. Implementación

| Pieza | Archivo |
|---|---|
| Modelo de sesión y permisos | `domain/model/Sesion.kt` |
| Contrato de autenticación | `domain/repository/AuthRepository.kt` |
| Almacén seguro (interfaz) | `domain/platform/AlmacenSeguro.kt` |
| Android: Keystore + AES-GCM | `androidMain/.../AlmacenSeguroAndroid.kt` |
| iOS: Keychain | `iosMain/.../AlmacenSeguroIos.kt` |
| DTOs de login | `data/remote/dto/LoginDto.kt` |
| Cliente Ktor (Auth Bearer, Logging saneado, validador 401) | `data/remote/HttpClientFactory.kt` |
| Repositorio REST de autenticación | `data/repository/AuthRepositoryRest.kt` |
| Casos de uso | `IniciarSesion`, `CerrarSesion`, `ObtenerSesion` |
| Errores 401/403 | `domain/error/ErrorApi.kt` (`NoAutenticado`, `SinPermiso`) |
| UI | `presentation/login/*`, guardia en `App.kt` |

Decisiones relevantes:

- **Cabecera automática:** el plugin `Auth` con `bearer { loadTokens; sendWithoutRequest }`; ninguna llamada escribe `Authorization`.
- **`sendWithoutRequest`:** `true` para todas las rutas salvo `auth/login`.
- **Caché del token:** `clearToken()` del proveedor Bearer tras login y logout.
- **Logging:** nivel `HEADERS` + `sanitizeHeader { Authorization }`.
- **Persistencia:** token cifrado en el almacén y perfil (usuario, permisos) para restaurar la sesión al reabrir.
- **401** fuera de login: se limpia la sesión y se vuelve al Login con aviso. **403:** se mantiene la sesión y se muestra un mensaje distinto.
- **Permisos en UI:** `CATALOGO_ESCRIBIR` (formulario, editar, eliminar), `CLIENTES_LEER` (Clientes), `VENTAS_LEER` (Pedidos), tanto en la barra inferior como en los accesos rápidos del inicio.

## 2. Evidencias (Android, servidor simulado)

| # | Captura | Qué demuestra |
|---|---|---|
| 1 | `01-login.png` | Pantalla de login al arrancar sin sesión (guardia de navegación) |
| 2 | `02-admin-inventario.png` | Login de `admin` correcto |
| 3 | `03-admin-inventario.png` | `admin`: CRUD completo (formulario, editar, eliminar) |
| 4 | `04-sesion-restaurada.png` | Tras cerrar y reabrir la app, entra sin pedir login |
| 5 | `05-vendedor-inicio.png` | `vendedor`: ve Clientes y Pedidos |
| 6 | `06-vendedor-inventario.png` | `vendedor`: inventario de solo lectura (sin formulario ni acciones) |
| 7 | `07-almacen-inicio.png` | `almacen`: sin Clientes ni Pedidos |
| 8 | `08-almacen-inventario.png` | `almacen`: formulario y acciones visibles |
| 9 | `09-almacen-403.png` | **403** provocado: «No tienes permiso para realizar esta acción», la sesión sigue abierta |
| 10 | `10-sesion-expirada.png` | **401**: vuelve al login con «Tu sesión expiró» |
| – | `log-authorization.txt` | Registro con `Authorization: ***` (saneada) |

El 403 se provocó haciendo que el servidor simulado rechazara la escritura (la UI ya oculta el formulario a quien no tiene permiso).
El 401 se provocó invalidando el token en el servidor simulado.

## 3. Lista de cotejo

| N.º | Criterio | Estado |
|---|---|---|
| 1 | Login contra `POST /api/v1/auth/login` | Verificado contra servidor simulado; **pendiente contra PharmaSoft** |
| 2 | Token en `AlmacenSeguro` por plataforma | Android verificado; iOS implementado, **sin compilar** |
| 3 | Plugin Auth adjunta la cabecera | Verificado (peticiones llegan con `Bearer`) |
| 4 | `sendWithoutRequest` | Verificado: login sin cabecera, resto con cabecera desde la primera llamada |
| 5 | Registro con cabecera saneada | Verificado (`log-authorization.txt`) |
| 6 | Sesión sobrevive al reinicio | Verificado (captura 4) |
| 7 | 401 → login con aviso | Verificado (captura 10) |
| 8 | 403 mantiene sesión, mensaje distinto | Verificado (captura 9) |
| 9 | UI oculta acciones por permisos | Verificado con los tres roles (capturas 3, 6, 7, 8) |
| 10 | Android e iOS | Android verificado; **iOS pendiente** (requiere Mac/Xcode) |

## 4. Pendientes

1. **PharmaSoft real:** el backend espera Oracle en `localhost:1522/FREEPDB1`. En este equipo solo hay Oracle XE
   (puerto 1521) y su listener tiene configurado un nombre de host obsoleto (`DESKTOP-OCAL5F1`), por lo que no acepta
   conexiones TCP. Hay que corregir `listener.ora` (o usar Oracle Free/Docker) y repetir la matriz de la sección 6 con
   `admin`, `vendedor` y `almacen`, y rehacer las capturas 2–10.
2. **iOS:** compilar y capturar en un Mac.
3. **Operaciones no implementadas en la app:** clientes (repositorio en memoria), ventas, reportes y `/auth/me`.
