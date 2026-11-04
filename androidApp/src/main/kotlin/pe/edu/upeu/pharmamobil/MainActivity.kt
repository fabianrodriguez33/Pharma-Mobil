package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.core.context.GlobalContext
import pe.edu.upeu.pharmamobil.platform.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobil.platform.permiso.PuentePermisos

class MainActivity : ComponentActivity() {

    private val gestorPermisos: GestorPermisosAndroid by lazy { GlobalContext.get().get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // El lanzador de permisos debe registrarse antes de que la Activity pase a STARTED.
        gestorPermisos.puente = PuentePermisos(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        gestorPermisos.puente?.takeIf { it.actividad === this }?.let {
            it.liberar()
            gestorPermisos.puente = null
        }
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}