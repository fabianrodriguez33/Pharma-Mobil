# Informe – Guía Práctica N.º 10: Autenticación JWT

**Alumno:** Julio Fabián Rodríguez Bazán
**Rama:** `feature/auth-jwt-rodriguez`
**Proyecto:** PharmaMobil (KMP + Ktor) consumiendo PharmaSoft

> **Alcance.** Capturas tomadas en un emulador Android (Pixel 8) contra **PharmaSoft real**
> (Spring Boot + Oracle Free 23ai en Docker, puerto 1522). iOS no se pudo verificar (requiere Mac).

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

## 2. Evidencias (Android, PharmaSoft real)

| # | Captura | Qué demuestra |
|---|---|---|
| 1 | `01-login.png` | Pantalla de login al arrancar sin sesión (guardia de navegación) |
| 2 | `02-admin-inicio.png` | Login de `admin` correcto contra `POST /api/v1/auth/login` |
| 3 | `03-admin-inventario.png` | `admin`: CRUD completo (formulario, editar, eliminar) |
| 4 | `04-sesion-restaurada.png` | Tras cerrar y reabrir la app, entra sin pedir login |
| 5 | `05-vendedor-inicio.png` | `vendedor`: ve Clientes y Pedidos |
| 6 | `06-vendedor-inventario.png` | `vendedor`: inventario de solo lectura (sin formulario ni acciones) |
| 7 | `07-almacen-inicio.png` | `almacen`: sin Clientes ni Pedidos |
| 8 | `08-almacen-inventario.png` | `almacen`: formulario y acciones visibles |
| 9 | `09-almacen-403.png` | **403** real: «No tienes permiso para realizar esta acción», la sesión sigue abierta |
| 10 | `10-sesion-expirada.png` | **401** real: vuelve al login con «Tu sesión expiró» |
| – | `log-authorization.txt` | Registro con `Authorization: ***` (saneada) |

Cómo se provocaron 403 y 401 con el backend real: con la sesión de `almacen` abierta se le cambió el rol a
`VENDEDOR` en la base (la UI aún mostraba el formulario) y se intentó registrar un producto → 403.
Luego se deshabilitó el usuario en la base y al recargar el inventario el backend respondió 401. Los datos
de prueba se restauraron después.

## 3. Matriz por rol (HTTP contra PharmaSoft)

| Operación | admin | vendedor | almacen |
|---|---|---|---|
| Listar productos | 200 | 200 | 200 |
| Registrar producto | 201 | 403 | 409 (autorizado; nombre duplicado) |
| Listar clientes | 200 | 200 | 403 |
| Registrar venta (cuerpo vacío) | 400 (autorizado) | 400 (autorizado) | 403 |
| Ver reportes | 200 | 403 | 403 |
| `/auth/me` | 200 | 200 | 200 |
| Sin token | 401 | | |

## 4. Lista de cotejo

| N.º | Criterio | Estado |
|---|---|---|
| 1 | Login contra `POST /api/v1/auth/login` | Cumple (verificado) |
| 2 | Token en `AlmacenSeguro` por plataforma | Android verificado; iOS implementado, **sin compilar** |
| 3 | Plugin Auth adjunta la cabecera | Cumple |
| 4 | `sendWithoutRequest` | Cumple: login sin cabecera, resto con cabecera desde la primera llamada |
| 5 | Registro con cabecera saneada | Cumple (`log-authorization.txt`) |
| 6 | Sesión sobrevive al reinicio | Cumple (captura 4) |
| 7 | 401 → login con aviso | Cumple (captura 10) |
| 8 | 403 mantiene sesión, mensaje distinto | Cumple (captura 9) |
| 9 | UI oculta acciones por permisos | Cumple con los tres roles |
| 10 | Android e iOS | Android verificado; **iOS pendiente** |

## 5. Pendientes

1. **iOS:** compilar y capturar en un Mac con Xcode (Keychain sin verificar).
2. **No implementado en la app:** clientes reales (hoy repositorio en memoria), ventas, reportes y `/auth/me`.
