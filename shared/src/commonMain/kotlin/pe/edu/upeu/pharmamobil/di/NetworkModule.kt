package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositoryRest
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel

/** URL base de PharmaSoft; cada plataforma la aporta desde su platformModule. */
val BaseUrlApi = named("baseUrlApi")

val networkModule = module {
    single { crearHttpClient(get(), get(BaseUrlApi)) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositoryRest(get()) }
    viewModel { ProductosViewModel(get(), get()) }
}
