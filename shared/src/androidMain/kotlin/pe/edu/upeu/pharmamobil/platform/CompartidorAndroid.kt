package pe.edu.upeu.pharmamobil.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor

class CompartidorAndroid(private val contexto: Context) : Compartidor {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun compartir(texto: String, imagenUrl: String?) {
        if (imagenUrl.isNullOrBlank()) {
            lanzar(texto, null)
            return
        }
        // La foto se baja a cache; si falla, se comparte solo el texto como antes.
        alcance.launch {
            val archivo = withContext(Dispatchers.IO) { descargar(imagenUrl) }
            lanzar(texto, archivo)
        }
    }

    private fun descargar(url: String): File? = runCatching {
        val carpeta = File(contexto.cacheDir, "compartidas").apply { mkdirs() }
        val destino = File(carpeta, "producto_" + url.hashCode() + ".jpg")
        val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        try {
            if (conexion.responseCode != 200) return@runCatching null
            conexion.inputStream.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            destino
        } finally {
            conexion.disconnect()
        }
    }.getOrNull()

    private fun lanzar(texto: String, imagen: File?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, texto)
            if (imagen != null) {
                type = "image/jpeg"
                val uri = FileProvider.getUriForFile(
                    contexto, contexto.packageName + ".fileprovider", imagen
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
        }
        val selector = Intent.createChooser(intent, "Compartir producto").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        contexto.startActivity(selector)
    }
}
