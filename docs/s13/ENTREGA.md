# Sesión 13 · Entregable

Rama: `feature/integrador-13` (un commit por paso). Pruebas: `./gradlew :shared:testAndroidHostTest` → 100 pruebas, 0 fallas.

## Lista de cotejo

| N.º | Criterio | Cumple | Dónde |
|---|---|---|---|
| 1 | Recorrí el flujo completo y registré los huecos | Sí | `docs/s13/huecos.md` |
| 2 | La tabla local guarda intentos y último error | Sí | `Producto.sq`, migración `2.sqm` |
| 3 | Distingue fallo transitorio de permanente | Sí | `EnvioRepositorioRest.aResultado()` |
| 4 | Espera creciente y máximo de intentos | Sí | `SincronizarUseCase` (1, 2, 4, 8, 16 s; 5 intentos) |
| 5 | Fila que agota intentos queda marcada y visible | Sí | contador «cambios no se pudieron enviar» + diálogo Revisar |
| 6 | Subida antes de bajada, sin pérdidas | Sí | `SincronizarUseCase` + prueba `laSubidaOcurreAntesQueLaBajadaYNadaSePierde` |
| 7 | Pantalla principal muestra pendientes, fallidas y antigüedad | Sí | `ProductosScreen` (Catálogo) |
| 8 | Gestor de permisos como interfaz común | Sí | `domain/permiso/GestorPermisos.kt` |
| 9 | Android distingue los tres estados | Sí | `GestorPermisosAndroid` |
| 10 | Permiso en manifiesto e Info.plist | Sí | `AndroidManifest.xml`, `Info.plist` |
| 11 | Explica antes de pedir y ofrece Ajustes | Sí | `TarjetaPermisoNotificaciones` |
| 12 | El SDK de Firebase no aparece fuera de la capa de datos | Sí | solo `data/firebase/TelemetriaFirebase.kt` |
| 13 | Pruebas de commonTest pasan | Sí | 100 / 100 |
| 14 | Commits con convención y distribuidos | Sí | `git log` |

## Activar Firebase (una sola vez por integrante)

1. Consola de Firebase → crear proyecto → registrar app Android con el paquete `pe.edu.upeu.pharmamobil`.
2. Descargar `google-services.json` y copiarlo en `androidApp/` (está en `.gitignore`: no se versiona).
3. Ejecutar la app. Sin el archivo la app compila y corre igual; la telemetría queda inactiva.
4. El evento `sincronizacion_completada` aparece en Analytics → DebugView (`adb shell setprop debug.firebase.analytics.app pe.edu.upeu.pharmamobil`).

## Capturas pendientes (requieren emulador Android 13+)

- Permiso de notificaciones: concedido, denegado y denegado para siempre (tarjeta del Catálogo).
- Estado de sincronización con filas pendientes (modo avión + crear producto en Inventario).
- Evento en la consola de Firebase.

## Notas

- La guía nombra la tabla `labor`; en este proyecto es `producto`. La migración es `2.sqm` porque `1.sqm` ya existía.
- `GestorPermisosAndroid` y `PuentePermisos` están en `shared/androidMain` (no en `androidApp`) para que Koin las registre en `platformModule`.
- iOS: `GestorPermisosIos` es mínimo (solo abre Ajustes); los diálogos nativos quedan para la sesión 14 por requerir un Mac. No se compiló iOS en este equipo.
