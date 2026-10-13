package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(credenciales: Credenciales): Result<Sesion> =
        repository.iniciarSesion(credenciales)
}
