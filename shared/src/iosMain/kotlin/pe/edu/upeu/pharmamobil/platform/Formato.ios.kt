package pe.edu.upeu.pharmamobil.platform

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun formatearSoles(valor: Double): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        currencyCode = "PEN"
        locale = NSLocale("es_PE")
    }
    return formatter.stringFromNumber(NSNumber(valor)) ?: "S/ $valor"
}
