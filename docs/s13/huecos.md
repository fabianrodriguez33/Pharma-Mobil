# Sesión 13 · Paso 1 · Recorrido del flujo y huecos detectados

Recorrido: instalación desde cero, inicio de sesión, creación de un registro, pérdida de conexión, sincronización, error de validación y cierre de sesión.

| N.º | Momento del recorrido | Qué se observó | Gravedad | Decisión |
|---|---|---|---|---|
| 1 | Primera apertura, sin sesión | Solo se ve la pantalla de login (guardia de sesión). Tiene salida: el usuario puede iniciar sesión. | Baja | Sin hueco |
| 2 | Credenciales incorrectas | El mensaje distingue "Usuario o contraseña incorrectos" de los fallos de red. | Baja | Sin hueco |
| 3 | Listado con la base local vacía | Estado vacío con texto, pero sin acción sugerida y con un texto que parece un error ("La API no devolvió productos"). | Media | **Se cierra hoy**: texto propio y botón Sincronizar |
| 4 | Creación con modo avión | El registro se guarda con id negativo y `pendiente = 1`, pero el catálogo no lo distingue de los demás. | Alta | **Se cierra hoy**: distintivo "Pendiente" por fila y contador en la cabecera |
| 5 | Sincronización al recuperar la señal | La fila sube y la pantalla se actualiza sola (Flow). Pero un pendiente que falla se reintenta sin límite y sin memoria; uno rechazado por el servidor queda atascado para siempre. | Alta | **Se cierra hoy**: intentos por fila, espera creciente, fallo transitorio vs. permanente, fila fallida visible con acción Reintentar |
| 6 | Error de validación del servidor | El mensaje por campo llega al formulario (sesión 8). | Baja | Sin hueco |
| 7 | Token vencido a mitad de un formulario | Se vuelve al login y se pierde lo escrito en el formulario. | Media | **Sesión 14**: guardar borrador y restaurarlo; requiere almacenamiento del borrador por formulario |
| 8 | Cierre de sesión | El token se borra, pero la base local conserva el catálogo y los pendientes del usuario anterior. | Media | **Sesión 14**: borrar la base local al cerrar sesión exige antes avisar si hay pendientes sin enviar (borrarlos en silencio perdería datos) |
| 9 | Permisos del sistema | La aplicación no resuelve ningún permiso: no hay forma de pedir, explicar ni llevar a Ajustes. | Alta | **Se cierra hoy**: `GestorPermisos` + permiso de notificaciones con sus tres estados |
| 10 | Observabilidad | Un fallo en el teléfono del usuario no deja rastro. | Media | **Se cierra hoy**: `Telemetria` detrás de una interfaz, con Firebase en la capa de datos |
| 11 | Creación reintentada tras tiempo agotado | Si el servidor guardó el registro pero la respuesta se perdió, el reintento lo duplica. | Alta | **Sesión 14**: exige una clave de operación en PharmaSoft; hoy se documenta el riesgo |

## Prioridad de hoy

1. Cola de pendientes acotada y visible (huecos 4 y 5).
2. Permiso de notificaciones con sus tres estados (hueco 9).
3. Estado vacío con acción (hueco 3) y telemetría (hueco 10).
