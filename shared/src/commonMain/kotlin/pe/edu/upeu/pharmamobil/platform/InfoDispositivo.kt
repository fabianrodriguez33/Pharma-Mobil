package pe.edu.upeu.pharmamobil.platform

/**
 * Información técnica del dispositivo donde corre la app.
 * Clase expect sin dependencias de contexto: cada plataforma aporta su actual.
 */
expect class InfoDispositivo() {
    val sistemaOperativo: String
    val versionSistema: String
    val modeloDispositivo: String
}
