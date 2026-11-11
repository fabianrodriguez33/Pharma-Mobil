# iOS · Paso 6 · Límite documentado

Equipo de desarrollo: Windows 11. No hay Mac ni Xcode, por lo que no es posible firmar, archivar ni exportar el `.ipa`.

Evidencia del estado real (11/11/2026):
- `./gradlew :shared:compileKotlinIosSimulatorArm64` → **BUILD SUCCESSFUL**: el módulo compartido y `iosMain` (AlmacenSeguroIos, CompartidorIos, GestorPermisosIos, Firebase) compilan a klib.
- No se puede ejecutar: enlazar el framework `Shared` y abrir `iosApp` requieren macOS.
- Limitación declarada: no se generó `.ipa` ni archivo de Xcode; no hay pruebas en un iPhone.

Qué falta para cerrarla: Mac con Xcode; seleccionar el equipo en Signing & Capabilities con el identificador del proyecto; destino «Any iOS Device»; Product · Archive; distribuir (con cuenta gratuita: solo desarrollo, perfil de 7 días); agregar `GoogleService-Info.plist` y los pods de Firebase; completar `GestorPermisosIos`.
