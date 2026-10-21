package pe.edu.upeu.pharmamobil.platform

import androidx.compose.runtime.Composable
import pe.edu.upeu.pharmamobil.domain.model.ImagenProducto

/**
 * Devuelve una funcion que abre la galeria del sistema; al elegir una foto llama a
 * [onElegida] con la imagen ya reducida y en JPEG.
 */
@Composable
expect fun rememberSelectorImagen(onElegida: (ImagenProducto?) -> Unit): () -> Unit
