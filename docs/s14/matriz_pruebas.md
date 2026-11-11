# Sesión 14 · Paso 7 · Matriz de pruebas (versión de entrega firmada, minificada con R8)

**Limitación declarada:** no se dispuso de dispositivos físicos. Se probaron dos emuladores de versiones distintas (uno de gama baja, API 24) con el `.apk` firmado instalado desde el archivo (`adb install`). Los fallos de batería y de señal intermitente no se reproducen en emulador; el modo avión se simuló. Hace falta repetir la matriz en teléfonos reales.

| Dispositivo | Versión | Marca / ABI |
|---|---|---|
| A | Android 17 (API 37.1) | Emulador Pixel 8, x86_64 |
| B | Android 7.0 (API 24) | Emulador «Android SDK built for x86» (gama baja) |

| N.º | Paso del recorrido | A | B | Observación |
|---|---|---|---|---|
| 1 | Instalación desde el archivo firmado | OK | OK | Nombre «PharmaMobil» e icono definitivos |
| 2 | Login correcto e incorrecto | OK | OK | «Usuario o contraseña incorrectos» distingue credencial inválida de falta de conexión |
| 3 | CRUD con rechazo de validación | OK | OK (crear) | Lista cargada: serialización y Koin funcionan con R8. Rechazo 400 («nombre entre 3 y 150») verificado en A; ver D-01 |
| 4 | Operación sin privilegio | OK | — | vendedor: el servicio responde 403, se informa «Sin permiso para esta operación» y la sesión no se cierra. Ver D-02 |
| 5 | Modo avión: abrir, leer y crear | OK | OK | «Sin conexión · 1 cambio por enviar»; la fila queda pendiente |
| 6 | Recuperar la conexión y sincronizar | OK | OK | «Todo sincronizado», sin duplicados |
| 7 | Permiso: conceder, denegar, denegar para siempre | OK | n/a | En API 24 no existe POST_NOTIFICATIONS (se considera concedido). Los tres estados se verificaron en A |
| 8 | Cerrar sesión y reabrir | OK | OK | Vuelve al login; el token se borró |

## Defectos

| ID | Dispositivo | Pasos para reproducir | Esperado | Obtenido | Severidad | Estado |
|---|---|---|---|---|---|---|
| D-01 | A | 1. Login admin. 2. Inventario: nombre «AB», precio 2, stock 10. 3. Registrar | El error del servicio llega al campo nombre | El formulario dice «registrado correctamente»; el motivo aparece luego en Catálogo → Revisar | Media | Pendiente (limitación 9 de la ficha) |
| D-02 | A | 1. Sin red, admin crea «Naproxeno». 2. Recuperar red y cerrar sesión. 3. Entrar como vendedor. 4. Abrir Catálogo | La cola no se envía con otro usuario | Se intenta con el usuario nuevo y el servicio responde 403 | Alta | Pendiente (limitación 10 de la ficha) |
| D-03 | A y B | Cerrar sesión como admin y volver a abrir | Campo usuario vacío y base limpia | El usuario queda escrito y la base local conserva los datos | Media | Pendiente (limitación 7) |
| D-04 | — | Probar en teléfonos físicos | — | No hay dispositivos físicos disponibles | Media | Pendiente: repetir la matriz con teléfonos reales |

Resultado de la minificación: la versión de entrega (R8 activado) lista, crea, sincroniza y resuelve la inyección en ambos dispositivos sin cierres; no hizo falta desactivar la minificación.
