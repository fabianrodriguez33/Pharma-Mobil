package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosScreen
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme

/** Una sola fuente para la barra inferior y el titulo de la barra superior. */
private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val DESTINOS = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Inventario", Icons.Default.Inventory2),
    Destino(Screen.Catalogo, "Catálogo", Icons.Default.Store),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
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

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {

        Scaffold(

            containerColor = MaterialTheme.colorScheme.background,

            topBar = {

                TopAppBar(
                    title = {
                        Text(
                            text = tituloDe(pantallaActual),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },

            bottomBar = {

                Box {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        DESTINOS.forEach { destino ->
                            NavigationBarItem(
                                selected = pantallaActual == destino.screen,
                                onClick = { pantallaActual = destino.screen },
                                icon = {
                                    Icon(
                                        imageVector = destino.icono,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = destino.titulo,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                            viewModel = koinViewModel()
                        )

                    Screen.Catalogo ->
                        ProductosScreen()

                    Screen.Clientes ->
                        ClienteScreen(
                            viewModel = koinViewModel()
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


private fun tituloDe(
    screen: Screen
): String {

    return DESTINOS.first { it.screen == screen }.titulo
}
