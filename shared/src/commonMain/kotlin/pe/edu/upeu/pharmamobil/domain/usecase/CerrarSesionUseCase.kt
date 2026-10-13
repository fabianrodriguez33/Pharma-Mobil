package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class CerrarSesionUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.cerrarSesion()
}
