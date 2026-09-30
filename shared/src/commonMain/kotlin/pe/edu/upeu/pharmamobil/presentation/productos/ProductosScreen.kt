package pe.edu.upeu.pharmamobil.presentation.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio

@Composable
fun ProductosScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductosViewModel = koinViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val e = estado) {
            ProductosUiState.Loading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            is ProductosUiState.Success ->
                if (e.productos.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.Inventory2,
                        titulo = "Sin productos",
                        descripcion = "La API no devolvió productos.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = e.productos, key = { it.id }) { ProductoRemotoItem(it) }
                    }
                }

            is ProductosUiState.Error ->
                EstadoVacio(
                    icono = Icons.Default.CloudOff,
                    titulo = "No pudimos cargar los productos",
                    descripcion = e.mensaje,
                    colorIcono = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                    accion = {
                        FilledTonalButton(onClick = { viewModel.cargar() }) {
                            Text("Reintentar")
                        }
                    }
                )
        }
    }
}

@Composable
private fun ProductoRemotoItem(producto: Producto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = producto.imagen.ifBlank { null },
                contentDescription = producto.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = producto.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "S/ ${producto.precio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
