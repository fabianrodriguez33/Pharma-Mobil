package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.repository.ProductoLocalRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRemotoRepository

/**
 * Sube lo pendiente y despues baja lo actualizado. Reintenta solo el fallo
 * transitorio, con espera creciente y un maximo de intentos por fila.
 */
class SincronizarUseCase(
    private val local: ProductoLocalRepository,
    private val remoto: ProductoRemotoRepository,
    private val esperar: suspend (Long) -> Unit = { delay(it) }
) {

    /** [conEspera] = false evita bloquear al usuario cuando el envio es inmediato (al guardar). */
    suspend operator fun invoke(conEspera: Boolean = true): ResumenSync {
        var enviadas = 0
        var fallidas = 0
        local.pendientes(MAX_INTENTOS).forEach { fila ->
            when (val r = remoto.enviar(fila.producto)) {
                is ResultadoEnvio.Exito -> {
                    local.confirmar(fila.id, r.idServidor)
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.id, r.motivo, MAX_INTENTOS)
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.id, r.motivo)
                    if (conEspera) esperar(espera(fila.intentos))
                }
            }
        }
        // Solo despues de subir se baja: invertirlo borra lo pendiente.
        local.conciliar(remoto.listar())
        return ResumenSync(enviadas, fallidas, local.contarPendientes(MAX_INTENTOS))
    }

    companion object {
        const val MAX_INTENTOS = 5

        /** 1 s, 2 s, 4 s, 8 s, 16 s. */
        fun espera(intentos: Int): Long = 1_000L shl intentos.coerceIn(0, 4)
    }
}
