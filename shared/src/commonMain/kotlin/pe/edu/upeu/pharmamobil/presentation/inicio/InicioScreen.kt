package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import pe.edu.upeu.pharmamobil.theme.TarjetaVidrio
import pharmamobil.shared.generated.resources.Res
import pharmamobil.shared.generated.resources.logo_principal

/** Cada acceso rapido de la portada lleva a uno de los modulos de la app. */
private data class Opcion(
    val screen: Screen,
    val icono: ImageVector,
    val titulo: String,
    val descripcion: String
)

private val OPCIONES = listOf(
    Opcion(
        screen = Screen.Catalogo,
        icono = Icons.Default.Store,
        titulo = "Ver catálogo",
        descripcion = "Productos en línea con foto y precio."
    ),
    Opcion(
        screen = Screen.Productos,
        icono = Icons.Default.Inventory2,
        titulo = "Gestionar inventario",
        descripcion = "Da de alta medicamentos con su precio y su stock."
    ),
    Opcion(
        screen = Screen.Clientes,
        icono = Icons.Default.Person,
        titulo = "Registrar clientes",
        descripcion = "Guarda los datos de contacto para la boleta."
    ),
    Opcion(
        screen = Screen.Pedidos,
        icono = Icons.Default.ShoppingCart,
        titulo = "Revisar pedidos",
        descripcion = "Disponible en una próxima sesión del curso."
    )
)

@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Portada()

        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )

        OPCIONES.forEach { opcion ->

            AccesoRapido(
                icono = opcion.icono,
                titulo = opcion.titulo,
                descripcion = opcion.descripcion,
                onClick = {
                    onNavegar(opcion.screen)
                }
            )
        }
    }
}


@Composable
private fun Portada() {

    TarjetaVidrio(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Image(
                painter = painterResource(Res.drawable.logo_principal),
                contentDescription = "Logo de PharmaMobil",
                modifier = Modifier.size(76.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Inventario, clientes y pedidos de tu botica en un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    TarjetaVidrio(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {

                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
