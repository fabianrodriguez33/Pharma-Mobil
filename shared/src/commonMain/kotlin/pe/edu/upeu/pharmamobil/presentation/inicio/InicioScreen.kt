package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import pe.edu.upeu.pharmamobil.domain.model.Sesion
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
    val descripcion: String,
    val permiso: String? = null
)

private val OPCIONES = listOf(
    Opcion(
        screen = Screen.Catalogo,
        icono = Icons.Default.Store,
        titulo = "Catálogo",
        descripcion = "Productos con foto y precio."
    ),
    Opcion(
        screen = Screen.Productos,
        icono = Icons.Default.Inventory2,
        titulo = "Inventario",
        descripcion = "Medicamentos, precio y stock."
    ),
    Opcion(
        screen = Screen.Clientes,
        icono = Icons.Default.Person,
        titulo = "Clientes",
        descripcion = "Datos de contacto.",
        permiso = Sesion.CLIENTES_LEER
    ),
    Opcion(
        screen = Screen.Pedidos,
        icono = Icons.Default.ShoppingCart,
        titulo = "Pedidos",
        descripcion = "Próximamente.",
        permiso = Sesion.VENTAS_LEER
    )
)

@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    /** Decide si la sesion actual tiene un permiso; las opciones sin permiso se ocultan. */
    puede: (String) -> Boolean = { true }
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

        OPCIONES.filter { it.permiso == null || puede(it.permiso) }.chunked(2).forEach { fila ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                fila.forEach { opcion ->

                    AccesoRapido(
                        icono = opcion.icono,
                        titulo = opcion.titulo,
                        descripcion = opcion.descripcion,
                        onClick = {
                            onNavegar(opcion.screen)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (fila.size == 1) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    TarjetaVidrio(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
