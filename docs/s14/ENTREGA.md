# Sesión 14 · Entregable

Rama `feature/builds-14` (un commit por paso). Etiqueta: `v1.0-final`.

| Dato | Valor |
|---|---|
| versionCode | 1 |
| versionName | 1.0.0 |
| applicationId | `pe.edu.upeu.pharmamobil` (se mantiene: cambiarlo invalida `google-services.json`) |
| Paquete firmado | `entregable/s14/Equipo1_ProyectoFinal_Android.aab` |
| Instalable de revisión | `entregable/s14/Equipo1_ProyectoFinal_Android_revision.apk` |
| Titular del certificado | CN=Julio Fabian Rodriguez Bazan, OU=EP Ingenieria de Sistemas, O=Universidad Peruana Union, L=Lima, C=PE |
| SHA-256 del certificado | `2e:11:e5:28:4e:a5:36:23:96:0e:82:2e:7b:e2:2b:2c:… (ver verificacion_firma.txt)` |
| Validez | 10 000 días desde el 11/11/2026 |

Captura de la verificación: `docs/s14/capturas/firma_verificacion.png`.

## Lista de cotejo

| N.º | Criterio | Cumple | Dónde |
|---|---|---|---|
| 1 | Ficha cerrada y alcance congelado | Sí | `ficha_integracion.md` |
| 2 | Almacén creado y copia fuera del equipo | **Parcial** | El `.jks` existe en `C:\Users\Usuario\claves\`. Falta guardar una copia en otro lugar (USB o nube privada) |
| 3 | Ni almacén ni contraseñas en el repositorio | Sí | `.gitignore` (`keystore.properties`, `*.jks`, `*.keystore`) |
| 4 | Abre y compila sin el archivo de propiedades | Sí | `hayFirma` en `androidApp/build.gradle.kts`; depuración compila sin él |
| 5 | Paquete firmado generado | Sí | `bundleRelease` |
| 6 | Instalable generado e instalado | Sí (emuladores) | No hubo teléfono físico |
| 7 | Verificación muestra titular y huella | Sí | `firma_verificacion.png` |
| 8 | Versión de entrega recorrida; serialización e inyección OK | Sí | `matriz_pruebas.md` |
| 9 | Minificación desactivada: decisión escrita | No aplica | Quedó **activada** y funciona |
| 10 | iOS: firma y archivo, o límite documentado | Sí (límite) | `ios.md` |
| 11 | Al menos dos dispositivos reales | **No** | Dos emuladores; limitación declarada en `matriz_pruebas.md` |
| 12 | Cada defecto con id, pasos, severidad y estado | Sí | `matriz_pruebas.md` |
| 13 | versionCode, versionName e id declarados | Sí | tabla superior |
| 14 | Commits con convención | Sí | `git log` |

## Notas
- No existe la rama `develop`: el trabajo se encadena sobre `feature/integrador-13`.
- El nombre del `.aab` usa `Equipo1` (Grupo 1); ajústalo si el equipo tiene otro número.
