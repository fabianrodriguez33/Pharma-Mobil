package pe.edu.upeu.pharmamobil.platform

import java.text.NumberFormat
import java.util.Locale

actual fun formatearSoles(valor: Double): String {
    return try {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        formato.format(valor)
    } catch (e: Exception) {
        "S/ ${String.format(Locale.US, "%.2f", valor)}"
    }
}

actual fun formatearEpoch(epochMillis: Long, patron: String): String =
    java.text.SimpleDateFormat(patron, Locale("es", "PE")).format(java.util.Date(epochMillis))
