package pe.edu.upeu.pharmamobil.platform

import android.os.Build

actual class InfoDispositivo actual constructor() {
    actual val sistemaOperativo: String = "Android"
    actual val versionSistema: String = Build.VERSION.RELEASE
    actual val modeloDispositivo: String = "${Build.MANUFACTURER} ${Build.MODEL}"
}
