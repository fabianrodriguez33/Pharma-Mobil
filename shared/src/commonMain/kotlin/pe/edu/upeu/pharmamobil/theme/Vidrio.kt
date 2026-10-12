package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
private fun esOscuro(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Fondo de la app: base lisa con dos halos de color que dan profundidad al vidrio. */
@Composable
fun FondoAurora(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val oscuro = esOscuro()
    val base = MaterialTheme.colorScheme.background
    val halo = TealMarca.copy(alpha = if (oscuro) 0.22f else 0.30f)
    val halo2 = Color(0xFF7FDCCB).copy(alpha = if (oscuro) 0.10f else 0.22f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(halo, Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.02f),
                        radius = size.width * 0.95f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(halo2, Color.Transparent),
                        center = Offset(size.width * 0.02f, size.height * 0.88f),
                        radius = size.width * 1.0f
                    )
                )
            }
    ) {
        content()
    }
}

/** Relleno del vidrio: gradiente blanco translucido, mas denso arriba. */
@Composable
fun Modifier.vidrio(shape: Shape = MaterialTheme.shapes.large): Modifier {
    val relleno = if (esOscuro()) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f)))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.58f)))
    }
    return background(relleno, shape)
}

@Composable
private fun bordeVidrio(): BorderStroke {
    val colores = if (esOscuro()) {
        listOf(Color.White.copy(alpha = 0.30f), TealMarca.copy(alpha = 0.18f), Color.White.copy(alpha = 0.10f))
    } else {
        listOf(Color.White, TealMarca.copy(alpha = 0.28f), Color.White.copy(alpha = 0.7f))
    }
    return BorderStroke(width = 1.dp, brush = Brush.linearGradient(colores))
}

/** Tarjeta de vidrio: sustituye a Card en toda la app. */
@Composable
fun TarjetaVidrio(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interior: @Composable () -> Unit = {
        Column(modifier = Modifier.vidrio(shape), content = content)
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = Color.Transparent,
            border = bordeVidrio(),
            content = interior
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = Color.Transparent,
            border = bordeVidrio(),
            content = interior
        )
    }
}

/** Contenedor de vidrio para la barra de navegacion flotante. */
@Composable
fun BarraVidrio(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (esOscuro()) Color(0xE6101D1A) else Color(0xEBFFFFFF),
        border = bordeVidrio(),
        shadowElevation = 12.dp,
        content = content
    )
}
