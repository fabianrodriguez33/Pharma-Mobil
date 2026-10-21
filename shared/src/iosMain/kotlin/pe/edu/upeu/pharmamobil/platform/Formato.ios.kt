package pe.edu.upeu.pharmamobil.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970
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

actual fun formatearEpoch(epochMillis: Long, patron: String): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = patron
        locale = NSLocale("es_PE")
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
