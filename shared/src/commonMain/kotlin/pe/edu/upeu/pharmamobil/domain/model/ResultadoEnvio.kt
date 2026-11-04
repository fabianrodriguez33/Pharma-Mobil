package pe.edu.upeu.pharmamobil.domain.model

/** Resultado de enviar una fila pendiente; el dominio no conoce codigos HTTP. */
sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio

    /** Sin red, tiempo agotado o servicio no disponible: se reintenta con espera creciente. */
    data class Transitorio(val motivo: String) : ResultadoEnvio

    /** Validacion, recurso inexistente, conflicto o falta de privilegio: reintentar da lo mismo. */
    data class Permanente(val motivo: String) : ResultadoEnvio
}
