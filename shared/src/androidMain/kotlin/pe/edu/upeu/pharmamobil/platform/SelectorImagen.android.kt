package pe.edu.upeu.pharmamobil.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto

private const val LADO_MAXIMO = 1280
private const val CALIDAD_JPEG = 85

@Composable
actual fun rememberSelectorImagen(onElegida: (ImagenProducto?) -> Unit): () -> Unit {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val alElegir = rememberUpdatedState(onElegida)

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        alcance.launch {
            val imagen = withContext(Dispatchers.IO) {
                runCatching {
                    val resolver = contexto.contentResolver
                    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                    var muestra = 1
                    while (maxOf(limites.outWidth, limites.outHeight) / muestra > LADO_MAXIMO * 2) {
                        muestra *= 2
                    }
                    val bitmap = resolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
                    } ?: return@runCatching null
                    val escala = LADO_MAXIMO.toFloat() / maxOf(bitmap.width, bitmap.height)
                    val reducido = if (escala < 1f) {
                        Bitmap.createScaledBitmap(
                            bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true
                        )
                    } else {
                        bitmap
                    }
                    val salida = ByteArrayOutputStream()
                    reducido.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
                    ImagenProducto(
                        bytes = salida.toByteArray(),
                        nombre = "producto_" + System.currentTimeMillis() + ".jpg",
                        tipo = "image/jpeg"
                    )
                }.getOrNull()
            }
            alElegir.value(imagen)
        }
    }

    return {
        lanzador.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
