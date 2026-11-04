package pe.edu.upeu.pharmamobil.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

/**
 * Unica clase que conoce el SDK de Firebase. Si el archivo de configuracion
 * (google-services.json) no esta, Firebase no se inicializa y cada llamada se
 * ignora: la telemetria nunca debe tumbar la aplicacion.
 */
class TelemetriaFirebase : Telemetria {

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        runCatching {
            val crashlytics = Firebase.crashlytics
            contexto.forEach { (clave, valor) -> crashlytics.setCustomKey(clave, valor) }
            crashlytics.recordException(e)
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        runCatching {
            Firebase.analytics.logEvent(nombre, datos.ifEmpty { null })
        }
    }
}
