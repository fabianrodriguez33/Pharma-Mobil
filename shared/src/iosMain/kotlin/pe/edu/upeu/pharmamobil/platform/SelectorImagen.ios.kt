package pe.edu.upeu.pharmamobil.platform

import androidx.compose.runtime.Composable
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto

// Pendiente en iOS: PHPickerViewController. Por ahora el producto se registra sin foto.
@Composable
actual fun rememberSelectorImagen(onElegida: (ImagenProducto?) -> Unit): () -> Unit = { onElegida(null) }
