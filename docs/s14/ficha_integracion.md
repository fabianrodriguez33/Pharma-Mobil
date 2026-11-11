# Sesión 14 · Paso 1 · Ficha de integración cerrada y alcance congelado

Fecha de cierre de la clasificación: 11/11/2026. A partir de este commit no entra funcionalidad nueva, ni cambios de arquitectura, librerías, rediseños ni migraciones de esquema.

| N.º | Hueco (ficha de la sesión 13) | Gravedad | Clasificación | Motivo / qué haría falta |
|---|---|---|---|---|
| 1 | Listado vacío sin acción | Media | **Cerrado** (sesión 13) | Estado vacío con botón Sincronizar |
| 2 | Fila pendiente indistinguible | Alta | **Cerrado** (sesión 13) | Distintivo «Pendiente de envío» y contador |
| 3 | Cola sin límite ni memoria | Alta | **Cerrado** (sesión 13) | Intentos, espera creciente y filas fallidas |
| 4 | Ningún permiso resuelto | Alta | **Cerrado** (sesión 13) | GestorPermisos con tres estados |
| 5 | Sin telemetría | Media | **Cerrado** (sesión 13) | Telemetria + Firebase |
| 6 | Token vencido pierde el formulario | Media | **Se declara limitación** | Exige guardar borradores por formulario (función nueva); el aviso de sesión expirada sí se muestra |
| 7 | Cierre de sesión deja datos locales y el usuario escrito | Media | **Se declara limitación** | Borrar la base exige avisar si hay pendientes; es un cambio de comportamiento que no se puede probar a fondo hoy |
| 8 | Reintento tras tiempo agotado puede duplicar | Alta | **Se declara limitación** | Requiere una clave de operación (idempotencia) en PharmaSoft |
| 9 | El 400/409 del servidor no llega al campo; el formulario dice «registrado correctamente» | Media | **Se declara limitación** | Requiere validar contra las reglas del servidor antes de guardar o rediseñar el flujo offline del formulario |
| 10 | La cola local no pertenece a un usuario (403 al cambiar de sesión) | Alta | **Se declara limitación** | Requiere una columna de propietario: migración de esquema, prohibida al congelar |
| 11 | iOS: GestorPermisosIos mínimo; sin ejecución | Media | **Se declara limitación** | Requiere un Mac con Xcode (ver `docs/s14/ios.md`) |
| 12 | Nombre de producto dado de baja bloquea uno nuevo (409) | Baja | **Se descarta** | Regla del backend PharmaSoft |
| 13 | Módulo Pedidos «en construcción» | Baja | **Se descarta** (trabajo futuro) | Nunca estuvo en el alcance comprometido |

**Se cierra hoy:** ninguno nuevo. Los huecos que rompían el recorrido principal ya se cerraron en la sesión 13 (1 a 5).

**Identificador de la aplicación:** se mantiene `pe.edu.upeu.pharmamobil` (el de la guía es `pe.upeu.<proyecto>`, un patrón); cambiarlo invalidaría `google-services.json` ya registrado en Firebase.
