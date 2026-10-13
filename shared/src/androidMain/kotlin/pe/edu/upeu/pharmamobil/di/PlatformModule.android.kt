package pe.edu.upeu.pharmamobil.di

import android.os.Build
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    // Emulador: 10.0.2.2 es el localhost de la maquina anfitriona.
    // Celular fisico: localhost + `adb reverse tcp:8080 tcp:8080`.
    single(BaseUrlApi) {
        if (esEmulador()) "http://10.0.2.2:8080/" else "http://localhost:8080/"
    }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
}

// Build.* es null en los tests de JVM, de ahi el orEmpty().
private fun esEmulador(): Boolean =
    Build.FINGERPRINT.orEmpty().contains("generic", ignoreCase = true) ||
        Build.HARDWARE.orEmpty().let { it.contains("ranchu") || it.contains("goldfish") } ||
        Build.MODEL.orEmpty().contains("Emulator", ignoreCase = true) ||
        Build.PRODUCT.orEmpty().contains("sdk", ignoreCase = true)
