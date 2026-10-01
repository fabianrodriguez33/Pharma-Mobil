package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    // 10.0.2.2 es el localhost de la maquina anfitriona vista desde el emulador
    single(BaseUrlApi) { "http://10.0.2.2:8080/" }
}
