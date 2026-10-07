package pe.edu.upeu.pharmamobil.platform

import platform.UIKit.UIDevice

actual class InfoDispositivo actual constructor() {
    actual val sistemaOperativo: String = UIDevice.currentDevice.systemName
    actual val versionSistema: String = UIDevice.currentDevice.systemVersion
    actual val modeloDispositivo: String = UIDevice.currentDevice.model
}
