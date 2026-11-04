package pe.edu.upeu.pharmamobil.presentation.permiso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso

@Composable
fun TarjetaPermisoNotificaciones(
    modifier: Modifier = Modifier,
    viewModel: PermisoNotificacionesViewModel = koinViewModel()
) {
    val ui by viewModel.estado.collectAsStateWithLifecycle()

    // Al volver de Ajustes el estado pudo cambiar sin que la pantalla lo sepa.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refrescar() }

    val estado = ui.estado ?: return
    val titulo: String
    val detalle: String
    val accion: String?
    when (estado) {
        EstadoPermiso.Concedido -> {
            titulo = "Avisos activados"
            detalle = "Te avisaremos si un cambio no se puede enviar."
            accion = null
        }
        EstadoPermiso.Denegado -> {
            titulo = "Avisos desactivados"
            detalle = "La aplicación sigue funcionando; solo verás el estado aquí."
            accion = "Activar avisos"
        }
        EstadoPermiso.DenegadoParaSiempre -> {
            titulo = "Avisos bloqueados"
            detalle = "Para recibirlos hay que activarlos en los ajustes del sistema."
            accion = "Abrir ajustes"
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (accion != null) {
            TextButton(onClick = viewModel::alPulsarActivar) { Text(accion) }
        }
    }

    when (ui.dialogo) {
        DialogoPermiso.EXPLICAR -> AlertDialog(
            onDismissRequest = viewModel::alCerrarDialogo,
            title = { Text("¿Activar los avisos?") },
            text = {
                Text(
                    "Usaremos las notificaciones solo para decirte que un cambio guardado " +
                        "sin conexión no pudo enviarse al servidor."
                )
            },
            confirmButton = { TextButton(onClick = viewModel::alConfirmarExplicacion) { Text("Continuar") } },
            dismissButton = { TextButton(onClick = viewModel::alCerrarDialogo) { Text("Ahora no") } }
        )
        DialogoPermiso.IR_A_AJUSTES -> AlertDialog(
            onDismissRequest = viewModel::alCerrarDialogo,
            title = { Text("Permiso bloqueado") },
            text = {
                Text(
                    "El sistema ya no mostrará el aviso de permiso. Abre los ajustes de la " +
                        "aplicación y activa las notificaciones."
                )
            },
            confirmButton = { TextButton(onClick = viewModel::alIrAAjustes) { Text("Abrir ajustes") } },
            dismissButton = { TextButton(onClick = viewModel::alCerrarDialogo) { Text("Cancelar") } }
        )
        DialogoPermiso.NINGUNO -> Unit
    }
}
