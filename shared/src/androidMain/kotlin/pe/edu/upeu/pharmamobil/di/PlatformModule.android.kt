package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    // 10.0.2.2 es el localhost de la maquina anfitriona vista desde el emulador
    single(BaseUrlApi) { "http://10.0.2.2:8080/" }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
