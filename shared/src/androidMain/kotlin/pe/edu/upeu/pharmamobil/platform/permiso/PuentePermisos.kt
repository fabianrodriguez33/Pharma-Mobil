package pe.edu.upeu.pharmamobil.platform.permiso

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred

/**
 * Conecta la corrutina con el lanzador de la Activity. Se crea en onCreate
 * (antes de que la Activity pase a STARTED) y convierte la respuesta del
 * dialogo del sistema en el valor que la funcion de suspension esperaba.
 */
class PuentePermisos(val actividad: ComponentActivity) {
    private var pendiente: CompletableDeferred<Boolean>? = null
    private val lanzador = actividad.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> pendiente?.complete(concedido) }

    suspend fun pedir(nombre: String): Boolean {
        val espera = CompletableDeferred<Boolean>()
        pendiente = espera
        lanzador.launch(nombre)
        return espera.await()
    }

    /** La Activity se destruye (p. ej. al rotar): nadie queda esperando una respuesta. */
    fun liberar() {
        pendiente?.complete(false)
        pendiente = null
    }
}
