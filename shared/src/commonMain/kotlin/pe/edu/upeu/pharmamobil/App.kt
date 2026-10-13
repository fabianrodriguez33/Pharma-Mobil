package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.Image
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import pe.edu.upeu.pharmamobil.theme.TealMarca
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.presentation.login.LoginScreen
import pe.edu.upeu.pharmamobil.presentation.login.LoginViewModel
import pe.edu.upeu.pharmamobil.presentation.login.SesionViewModel

import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosScreen
import pe.edu.upeu.pharmamobil.theme.BarraVidrio
import pe.edu.upeu.pharmamobil.theme.FondoAurora
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme
import pharmamobil.shared.generated.resources.Res
import pharmamobil.shared.generated.resources.logo_principal

/** Una sola fuente para la barra inferior y el titulo de la barra superior. */
private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector,
    /** Permiso necesario para ver la seccion; null = visible para cualquier sesion. */
    val permiso: String? = null
)

private val DESTINOS = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Inventario", Icons.Default.Inventory2),
    Destino(Screen.Catalogo, "Catálogo", Icons.Default.Store),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person, Sesion.CLIENTES_LEER),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart, Sesion.VENTAS_LEER)
)


private val ScreenSaver = Saver<Screen, Int>(
    save = { pantalla ->
        DESTINOS.indexOfFirst { it.screen == pantalla }
    },
    restore = { indice ->
        DESTINOS[indice].screen
    }
)

@Composable
fun App() = KoinContext {

    var pantallaActual by rememberSaveable(stateSaver = ScreenSaver) {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    var darkTheme by rememberSaveable {
        mutableStateOf(false)
    }

    val sesionViewModel: SesionViewModel = koinViewModel()
    val sesionActual by sesionViewModel.sesion.collectAsStateWithLifecycle()
    val avisoExpirada by sesionViewModel.avisoExpirada.collectAsStateWithLifecycle()

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {

        FondoAurora {
        val sesion = sesionActual
        if (sesion == null) {
            // Guardia: sin sesion (arranque en frio, logout o 401) solo se ve Login.
            val loginViewModel: LoginViewModel = koinViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                aviso = if (avisoExpirada) "Tu sesión expiró. Inicia sesión nuevamente." else null
            )
        } else {
        val destinos = DESTINOS.filter { it.permiso == null || sesion.puede(it.permiso) }
        // Si la seccion actual deja de estar permitida (otra sesion), se vuelve a Inicio.
        androidx.compose.runtime.LaunchedEffect(destinos, pantallaActual) {
            if (destinos.none { it.screen == pantallaActual }) pantallaActual = Screen.Inicio
        }
        Scaffold(

            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,

            topBar = {

                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.logo_principal),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = tituloDe(pantallaActual),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = sesionViewModel::salir) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión de ${sesion.usuario}"
                            )
                        }
                        IconButton(
                            onClick = { darkTheme = !darkTheme }
                        ) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Cambiar tema"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },

            bottomBar = {

                BarraVidrio(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        destinos.forEach { destino ->
                            ItemNavegacion(
                                destino = destino,
                                seleccionado = pantallaActual == destino.screen,
                                onClick = { pantallaActual = destino.screen },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                when (pantallaActual) {

                    Screen.Inicio ->
                        InicioScreen(
                            onNavegar = { destino ->
                                pantallaActual = destino
                            }
                        )

                    Screen.Productos ->
                        ProductoScreen(
                            viewModel = koinViewModel(key = sesion.token),
                            puedeEscribir = sesion.puede(Sesion.CATALOGO_ESCRIBIR)
                        )

                    Screen.Catalogo ->
                        ProductosScreen(viewModel = koinViewModel(key = sesion.token))

                    Screen.Clientes ->
                        ClienteScreen(
                            viewModel = koinViewModel(key = sesion.token)
                        )

                    Screen.Pedidos ->
                        EstadoVacio(
                            icono = Icons.Default.ShoppingCart,
                            titulo = "Pedidos en construcción",
                            descripcion = "Este módulo llega en una próxima sesión del curso.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                }
            }
        }
        }
        }
    }
}


private fun tituloDe(
    screen: Screen
): String {

    return DESTINOS.first { it.screen == screen }.titulo
}


/** Item de la barra: el seleccionado es una capsula de vidrio con brillo, no un bloque tonal. */
@Composable
private fun ItemNavegacion(
    destino: Destino,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(20.dp)
    val colorContenido by animateColorAsState(
        if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
    val interaccion = remember { MutableInteractionSource() }
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(forma)
            .then(
                if (seleccionado) {
                    Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (oscuro) 0.14f else 0.55f),
                                    TealMarca.copy(alpha = if (oscuro) 0.30f else 0.20f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.95f), TealMarca.copy(alpha = 0.35f))
                            ),
                            forma
                        )
                } else {
                    Modifier
                }
            )
            .clickable(interactionSource = interaccion, indication = null, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = destino.icono,
            contentDescription = null,
            tint = colorContenido,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = destino.titulo,
            style = MaterialTheme.typography.labelSmall,
            color = colorContenido,
            maxLines = 1,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
