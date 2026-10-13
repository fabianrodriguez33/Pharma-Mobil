package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.AuthApi
import pe.edu.upeu.pharmamobil.data.remote.EventosSesion
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositoryRest
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerSesionUseCase
import pe.edu.upeu.pharmamobil.presentation.login.LoginViewModel
import pe.edu.upeu.pharmamobil.presentation.login.SesionViewModel
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositoryRest
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel

/** URL base de PharmaSoft; cada plataforma la aporta desde su platformModule. */
val BaseUrlApi = named("baseUrlApi")

val networkModule = module {
    single { EventosSesion() }
    single {
        val eventos = get<EventosSesion>()
        crearHttpClient(get(), get(BaseUrlApi), get<AlmacenSeguro>()) { eventos.notificarExpirada() }
    }
    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositoryRest(get(), get(), get()) }
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { ObtenerSesionUseCase(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SesionViewModel(get(), get(), get()) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositoryRest(get()) }
    viewModel { ProductosViewModel(get(), get()) }
}
