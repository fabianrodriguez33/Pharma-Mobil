package pe.edu.upeu.pharmamobil.theme

import androidx.compose.runtime.Composable

/**
 * Ajusta el color de los iconos de la barra de estado al tema de la app, no al
 * del sistema: el modo oscuro de PharmaMobil se cambia desde la propia barra.
 */
@Composable
expect fun BarrasDelSistema(darkTheme: Boolean)
