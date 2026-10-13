package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion

interface AuthRepository {
    /** Sesion vigente; null cuando no hay usuario autenticado. */
    val sesionActual: StateFlow<Sesion?>

    suspend fun iniciarSesion(credenciales: Credenciales): Result<Sesion>

    fun obtenerSesion(): Sesion?

    fun cerrarSesion()
}
