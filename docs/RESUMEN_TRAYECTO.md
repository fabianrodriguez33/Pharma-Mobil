# PharmaMobil: qué se hizo y qué teoría se aplicó

Resumen del trayecto desde la primera guía hasta la entrega (sesión 14), y una lista de pautas para revisar cualquier otra app (por ejemplo, la de la residencia).

## 1. Qué es PharmaMobil

Aplicación móvil de gestión farmacéutica hecha con **Kotlin Multiplatform + Compose Multiplatform**, que consume el servicio **PharmaSoft** (Spring Boot + Oracle).

| Capa | Dónde vive | Para qué |
|---|---|---|
| Presentación | `presentation/` (pantallas Compose + ViewModel) | Pinta estados; no conoce Ktor ni SQL |
| Dominio | `domain/` (modelos, casos de uso, interfaces) | Reglas del negocio; no depende de nadie |
| Datos | `data/` (Ktor, SQLDelight, Firebase, repositorios) | Todo lo que viene del exterior |
| Plataforma | `androidMain` / `iosMain` | Lo que depende del sistema (almacén seguro, permisos, compartir) |
| Inyección | `di/` (Koin) | Une las capas sin que se conozcan |

Regla de oro aplicada en todo el proyecto: **las dependencias apuntan hacia el dominio** (arquitectura limpia).

## 2. El trayecto, sesión por sesión

| Sesión | Qué se construyó | Teoría / pauta aplicada |
|---|---|---|
| Base (previas a la 7) | Modelos, casos de uso, repositorios en memoria, ViewModels, Koin, pruebas | Arquitectura limpia, inversión de dependencias, estados de UI, pruebas unitarias |
| **07** Cliente REST | Ktor, consumo GET de productos, DTO, imágenes con Coil | DTO separado del dominio y mapeo; cliente HTTP único; registro de peticiones |
| **08** CRUD completo | Crear, listar, editar y eliminar contra la API; errores tipados | Jerarquía sellada de errores (`ErrorApi`); traducir en datos, mostrar en presentación; relanzar `CancellationException` |
| **09** Capacidades nativas | Compartir, formato de moneda, información del dispositivo | `expect/actual` y interfaces + Koin para lo que depende de la plataforma |
| **10** Autenticación JWT | Login, token en almacén seguro, cabecera centralizada, roles | Token cifrado (Keystore/Llavero), `Authorization` en un solo sitio, 401 cierra sesión, ocultar UI por permiso (el servicio sigue siendo la autoridad) |
| **11** Offline-first | SQLDelight como fuente de verdad, sincronización subir-y-bajar | La pantalla observa la base local (Flow), nunca la red; subir antes de bajar; id local negativo para lo pendiente |
| **13** Integrador | Reintentos acotados, estado de sincronización visible, permisos, Firebase | Reintento con límite y memoria; fallo transitorio vs. permanente; el proveedor externo detrás de una interfaz; pedir permiso explicando antes |
| **14** Entrega | Firma propia, `.aab` y `.apk`, R8, matriz de pruebas, guía de instalación | Secretos fuera de Git; la versión de entrega se recorre entera; el emulador no basta; limitaciones declaradas |

## 3. Pautas que se aplicaron (y por qué)

**Arquitectura**
- El dominio no importa Ktor, SQLDelight ni Firebase. Solo `data/` los conoce.
- Un caso de uso por acción (`SincronizarUseCase`), con interfaces como puertos (`ProductoLocalRepository`, `Telemetria`, `GestorPermisos`).
- Cambiar de proveedor (Firebase, API, base) toca una clase, no las pantallas.

**Errores y estados**
- Los errores llegan como tipos (`ErrorApi`), no como textos ni códigos HTTP.
- Cada pantalla tiene estados: cargando, éxito, vacío con acción, error con motivo.
- Nunca decir «guardado» si no se sabe (hueco conocido: el formulario dice «registrado» aunque el servidor lo rechace después).

**Red y datos**
- Un único cliente HTTP, con la cabecera de sesión y el manejo del 401 en un solo lugar.
- Offline-first: la lista sale de la base local; la red solo sincroniza.
- Reintentos: máximo de intentos, espera creciente, solo se reintenta lo transitorio.
- Lo no idempotente (crear) puede duplicar si se reintenta: riesgo declarado.

**Seguridad**
- El token se guarda cifrado; no se imprime en logs.
- La UI oculta lo que el rol no puede hacer, pero la autorización real está en el servicio.
- Almacén de claves y contraseñas fuera del repositorio (`.gitignore`).
- Telemetría sin datos personales ni el token.

**Permisos**
- Pedir en el momento de usar la función, explicar antes, y resolver los tres estados: concedido, denegado, denegado para siempre (ir a Ajustes).
- En Android hay que recordar si ya se pidió para distinguir «primera vez» de «denegado definitivo».

**Pruebas**
- Lógica en `commonTest`, sin dispositivo: base SQLite en memoria y `MockEngine` para la red.
- Dobles falsos para lo que depende del sistema (gestor de permisos).
- Pruebas manuales en versión de entrega y en dispositivo real (el emulador no reproduce batería ni señal).

**Entrega**
- Versionar con `versionCode`, `versionName` y etiqueta Git sobre el commit compilado.
- Compilar la versión de entrega pronto: R8 puede romper serialización o inyección solo ahí.
- Documentar límites con evidencia (iOS sin Mac, un solo teléfono real).

## 4. Lista para revisar otra app (por ejemplo, la residencia)

Marca cada punto con una prueba concreta, no con una impresión.

### Arquitectura
- [ ] ¿El dominio compila sin importar librerías de red, base o UI?
- [ ] ¿Las pantallas hablan con ViewModels y estos con casos de uso/repositorios por interfaz?
- [ ] ¿La inyección de dependencias está en un solo lugar?
- [ ] ¿Hay DTOs separados de los modelos de dominio?

### Pantallas
- [ ] ¿Cada pantalla tiene carga, vacío con acción y error con motivo?
- [ ] ¿Los mensajes distinguen «sin conexión», «credencial inválida» y «sin permiso»?
- [ ] ¿Al girar la pantalla o volver de segundo plano no se pierde el estado?

### Red y sesión
- [ ] ¿Hay un único cliente HTTP con la cabecera de sesión centralizada?
- [ ] ¿Qué pasa con un 401 a mitad de uso (vuelve al login con aviso)?
- [ ] ¿Se relanza la cancelación de corrutinas?
- [ ] ¿El token está cifrado y no aparece en logs?

### Datos locales y sincronización
- [ ] ¿La pantalla lee de la base local y se actualiza sola?
- [ ] ¿Funciona en modo avión: abrir, leer y crear?
- [ ] ¿Lo pendiente sube antes de bajar y no se duplica?
- [ ] ¿Hay límite de reintentos y se ve lo que falló?
- [ ] ¿Qué pasa con los datos al cerrar sesión y entrar otro usuario?

### Seguridad
- [ ] ¿La UI y el servicio aplican los mismos permisos?
- [ ] ¿Hay secretos (claves, tokens, `google-services.json`, `.jks`) en el repo o en su historial? (`git log --all --diff-filter=A --name-only | grep -Ei "jks|keystore|google-services"`)
- [ ] ¿El tráfico sin cifrar está justificado (solo desarrollo)?

### Permisos del sistema
- [ ] ¿Se piden en el momento justo y con explicación?
- [ ] ¿Los tres estados funcionan, incluido «ir a Ajustes»?
- [ ] ¿Están declarados en el manifiesto (y `Info.plist` en iOS)?

### Pruebas
- [ ] ¿Hay pruebas de la lógica que corren sin dispositivo? (`./gradlew :shared:testAndroidHostTest`)
- [ ] ¿Se probó la versión de entrega (minificada), no solo la de depuración?
- [ ] ¿Se probó en un teléfono real, sin el entorno de desarrollo conectado?

### Entrega
- [ ] ¿`.aab` y `.apk` firmados con certificado propio y verificados con `apksigner`?
- [ ] ¿Versión, identificador y etiqueta coinciden con el archivo entregado?
- [ ] ¿Alguien ajeno siguió la guía de instalación sin preguntar?
- [ ] ¿Las limitaciones están escritas con su motivo?

## 5. Qué le falta a PharmaMobil (limitaciones reconocidas)

| Tema | Estado |
|---|---|
| iOS | Compila a klib, pero no se firmó ni ejecutó (sin Mac) |
| Cola local por usuario | La cola de pendientes no pertenece a un usuario (otro usuario puede enviarla) |
| Mensaje de validación del servidor | No llega al campo en el flujo offline del formulario |
| Idempotencia | Reintentar una creación tras un tiempo agotado puede duplicar (requiere clave de operación en el servicio) |
| Cierre de sesión | Deja datos locales y el usuario escrito |
| Dispositivos reales | Dos teléfonos del mismo modelo; falta otro modelo/versión y el recorrido completo |
| Copia del almacén de claves | Pendiente guardarla fuera de la PC |

## 6. Dónde está la evidencia

| Sesión | Carpeta o archivo |
|---|---|
| 07 a 11 | `docs/actividad10`, `docs/actividad11`, `entregable/`, informes PDF/Word en `docs/entrega` |
| 13 | `docs/s13/` (huecos, capturas, `ENTREGA.md`), `docs/entrega/S13_*.pdf` y `docs/entrega/RodriguezBazan_Julio_Sesion13_Evidencias.pdf` |
| 14 | `docs/s14/` (ficha, iOS, matriz, firma), `entregable/s14/` (`.aab`, `.apk`), `docs/entrega/*Sesion14*` y `*Autonoma14*` |
