# Entrega - Guia Practica N.o 09 (expect/actual y capacidades nativas)

Estudiante: Julio Fabian Rodriguez Bazan (trabajo individual)

Rama: https://github.com/fabianrodriguez33/Pharma-Mobil/tree/feature/expect-actual-rodriguez

## Evidencias

| Evidencia | Archivo |
|---|---|
| Listado con precio formateado (Android, datos reales de PharmaSoft/Oracle) | `01_listado_android.png` |
| Selector de compartir de Android (texto armado en codigo comun) | `02_compartir_android.png` |
| Error del punto de control 1 (falta un `actual`) | `03_error_punto_control_1.png` |
| Listado y hoja de compartir en iOS | Pendiente: requiere Mac con Xcode |

## Lista de cotejo

| N.o | Criterio | Cumple | Donde |
|---|---|---|---|
| 1 | `expect` en commonMain con `actual` en androidMain e iosMain | Si | `platform/Formato*.kt` |
| 2 | Mismo paquete y misma firma | Si | `pe.edu.upeu.pharmamobil.platform` |
| 3 | Precio como moneda en ambas plataformas | Si (Android verificado; iOS por codigo) | `S/ 12.50` |
| 4 | Formato en presentacion | Si | `ProductosViewModel` |
| 5 | `Compartidor` en domain sin plataforma | Si | `domain/platform/Compartidor.kt` |
| 6 | Implementacion nativa por plataforma | Si | `CompartidorAndroid`, `CompartidorIos` |
| 7 | `platformModule` registra y Koin resuelve | Si | `AppModuleTest` en verde |
| 8 | Boton Compartir con texto de codigo comun | Si (Android verificado) | `02_compartir_android.png` |
| 9 | Presentacion sin `android.*` ni `platform.UIKit.*` | Si | verificado con grep |
| 10 | Capturas en las dos plataformas | Parcial: falta iOS | |
