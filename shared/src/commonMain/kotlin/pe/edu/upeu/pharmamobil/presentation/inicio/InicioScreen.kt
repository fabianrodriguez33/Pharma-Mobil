package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.theme.TealMarca
import pe.edu.upeu.pharmamobil.theme.TealOscuro

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(TealMarca, TealOscuro)
                )
            )
            .padding(22.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(26.dp)
                )
            }

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Text(
                text = "Inventario, clientes y pedidos de tu botica en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
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

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
