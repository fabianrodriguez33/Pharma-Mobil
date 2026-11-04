package pe.edu.upeu.pharmamobil.domain.port

/**
 * Lo unico que el dominio conoce de la telemetria. Se registra el contexto
 * tecnico (pantalla, operacion, codigo de estado) y nunca datos personales
 * ni el token de sesion.
 */
interface Telemetria {
    fun registrarError(e: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}
