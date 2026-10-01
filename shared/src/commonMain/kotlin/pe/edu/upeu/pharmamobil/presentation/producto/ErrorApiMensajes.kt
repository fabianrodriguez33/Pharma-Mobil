package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException

/** Texto legible para el usuario; presentation nunca ve tipos de Ktor. */
fun ErrorApi.mensaje(): String = when (this) {
    is ErrorApi.Validacion -> "Revisa los datos del formulario"
    ErrorApi.NoEncontrado -> "El producto ya no existe"
    is ErrorApi.Conflicto -> mensaje
    ErrorApi.Servidor -> "El servidor no está disponible. Inténtalo más tarde"
    ErrorApi.SinConexion -> "Sin conexión con el servidor"
    ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder"
    is ErrorApi.Desconocido -> detalle
}

fun Throwable.mensajeLegible(porDefecto: String): String =
    (this as? ErrorApiException)?.error?.mensaje() ?: message ?: porDefecto
