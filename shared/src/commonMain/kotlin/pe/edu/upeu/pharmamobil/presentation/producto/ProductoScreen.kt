package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import pe.edu.upeu.pharmamobil.platform.rememberSelectorImagen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import pe.edu.upeu.pharmamobil.theme.TarjetaVidrio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier,
    /** CATALOGO_ESCRIBIR: sin el, el formulario y las acciones de edicion se ocultan. */
    puedeEscribir: Boolean = true
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (puedeEscribir) {
            FormularioProductoCard(
                formulario = uiState.formulario,
                operacion = uiState.operacion,
                onNombreChange = viewModel::onNombreChange,
                onPrecioChange = viewModel::onPrecioChange,
                onStockChange = viewModel::onStockChange,
                onImagenElegida = viewModel::onImagenElegida,
                onGuardar = viewModel::guardar,
                onCancelar = viewModel::cancelarEdicion
            )
        }

        (uiState.operacion as? ProductoUiState.Operacion.Fallida)?.let {
            Text(
                text = it.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        uiState.mensajeExito?.let {
            MensajeExito(it)
        }

        EncabezadoInventario(uiState.fase)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            when (val fase = uiState.fase) {

                ProductoUiState.Fase.Cargando ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        CircularProgressIndicator()

                        Text(
                            text = "Cargando inventario…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                ProductoUiState.Fase.SinProductos ->
                    EstadoVacio(
                        icono = Icons.Default.Inventory2,
                        titulo = "Todavía no hay productos",
                        descripcion = "Registra el primero con el formulario de arriba.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                is ProductoUiState.Fase.ConProductos ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = fase.productos,
                            key = { it.id }
                        ) { producto ->
                            ProductoItem(
                                producto = producto,
                                habilitado = !uiState.ocupado,
                                puedeEscribir = puedeEscribir,
                                onEditar = { viewModel.editar(producto) },
                                onEliminar = { viewModel.eliminar(producto.id) }
                            )
                        }
                    }

                is ProductoUiState.Fase.Error ->
                    EstadoVacio(
                        icono = Icons.Default.CloudOff,
                        titulo = "No pudimos cargar el inventario",
                        descripcion = fase.mensaje,
                        colorIcono = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                        accion = {
                            FilledTonalButton(onClick = viewModel::cargarProductos) {
                                Text("Reintentar")
                            }
                        }
                    )
            }
        }
    }
}


@Composable
private fun FormularioProductoCard(
    formulario: FormularioProducto,
    operacion: ProductoUiState.Operacion,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onImagenElegida: (pe.edu.upeu.pharmamobil.domain.model.ImagenProducto?) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {

    val enCurso = operacion as? ProductoUiState.Operacion.EnCurso
    val ocupado = enCurso != null

    TarjetaVidrio(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = if (formulario.editando) "Editar producto" else "Registrar producto",
                style = MaterialTheme.typography.titleMedium
            )

            ValidatedTextField(
                value = formulario.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = formulario.nombreError,
                leadingIcon = Icons.Default.Medication,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ValidatedTextField(
                    value = formulario.precio,
                    onValueChange = onPrecioChange,
                    label = "Precio",
                    error = formulario.precioError,
                    ayuda = "En soles",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                ValidatedTextField(
                    value = formulario.stock,
                    onValueChange = onStockChange,
                    label = "Stock",
                    error = formulario.stockError,
                    ayuda = "Unidades",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            val abrirGaleria = rememberSelectorImagen(onImagenElegida)
            val vistaPrevia: Any? = formulario.imagen?.bytes
                ?: formulario.imagenActual.ifBlank { null }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(64.dp)
                ) {
                    if (vistaPrevia != null) {
                        AsyncImage(
                            model = vistaPrevia,
                            contentDescription = "Foto del producto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = abrirGaleria, enabled = !ocupado) {
                        Text(if (vistaPrevia != null) "Cambiar foto" else "Agregar foto")
                    }
                    Text(
                        text = if (formulario.imagen != null) "Foto lista para subir"
                        else "Opcional · sin foto se guarda igual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (formulario.imagen != null) {
                    TextButton(onClick = { onImagenElegida(null) }, enabled = !ocupado) {
                        Text("Quitar")
                    }
                }
            }

            Button(
                onClick = onGuardar,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (enCurso?.tipo) {
                        ProductoUiState.Operacion.Tipo.Crear -> "Registrando…"
                        ProductoUiState.Operacion.Tipo.Actualizar -> "Guardando…"
                        else -> if (formulario.editando) "Guardar cambios" else "Registrar"
                    }
                )
            }

            if (formulario.editando) {
                OutlinedButton(
                    onClick = onCancelar,
                    enabled = !ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición")
                }
            }
        }
    }
}


@Composable
private fun EncabezadoInventario(
    fase: ProductoUiState.Fase
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        if (fase is ProductoUiState.Fase.ConProductos) {

            val cantidad = fase.productos.size

            Text(
                text = if (cantidad == 1) "1 producto" else "$cantidad productos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun ProductoItem(
    producto: ProductoUi,
    habilitado: Boolean,
    puedeEscribir: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {

    TarjetaVidrio(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {

                if (producto.imagen.isNotBlank()) {
                    AsyncImage(
                        model = producto.imagen,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = "${producto.precio}  ·  ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (producto.requiereReposicion) {

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {

                    Text(
                        text = "Reponer",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    )
                }
            }

            if (puedeEscribir) {
                IconButton(onClick = onEditar, enabled = habilitado) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${producto.nombre}")
                }

                IconButton(onClick = onEliminar, enabled = habilitado) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar ${producto.nombre}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
