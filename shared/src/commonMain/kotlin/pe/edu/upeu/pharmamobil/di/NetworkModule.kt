package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.presentation.productos.ProductosViewModel

/** Calificador del repositorio remoto; el binding sin calificar sigue siendo el de memoria. */
val RepositorioRemoto = named("remoto")

val networkModule = module {
    single { crearHttpClient(get()) }
    single { ProductoApi(get()) }
    single<ProductoRepository>(RepositorioRemoto) { ProductoRepositoryImpl(get()) }
    viewModel { ProductosViewModel(get(RepositorioRemoto)) }
}
