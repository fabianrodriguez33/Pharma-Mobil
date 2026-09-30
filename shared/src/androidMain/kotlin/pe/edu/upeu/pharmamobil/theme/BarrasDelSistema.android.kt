package pe.edu.upeu.pharmamobil.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

@Composable
actual fun BarrasDelSistema(darkTheme: Boolean) {
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as? Activity)?.window ?: return@SideEffect
            val iconosOscuros = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val mascara = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                ventana.insetsController?.setSystemBarsAppearance(
                    if (iconosOscuros) mascara else 0,
                    mascara
                )
            } else {
                @Suppress("DEPRECATION")
                ventana.decorView.systemUiVisibility = if (iconosOscuros) {
                    ventana.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    ventana.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }
}
