@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package pe.edu.upeu.pharmamobil.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Guarda token y perfil como entradas genericas del Keychain de iOS. */
class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) = guardar(CLAVE_TOKEN, token)
    override fun leerToken(): String? = leer(CLAVE_TOKEN)
    override fun guardarPerfil(json: String) = guardar(CLAVE_PERFIL, json)
    override fun leerPerfil(): String? = leer(CLAVE_PERFIL)

    override fun borrarToken() {
        eliminar(CLAVE_TOKEN)
        eliminar(CLAVE_PERFIL)
    }

    private fun consulta(cuenta: String, extra: Map<Any?, Any?> = emptyMap()): CFDictionaryRef? {
        val base = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICIO,
            kSecAttrAccount to cuenta
        )
        base.putAll(extra)
        @Suppress("UNCHECKED_CAST")
        return CFBridgingRetain(base) as CFDictionaryRef?
    }

    private fun guardar(cuenta: String, valor: String) {
        eliminar(cuenta)
        val datos = NSString.create(string = valor).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val dict = consulta(
            cuenta,
            mapOf(
                kSecValueData to datos,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )
        )
        SecItemAdd(dict, null)
        CFBridgingRelease(dict)
    }

    private fun leer(cuenta: String): String? = memScoped {
        val dict = consulta(cuenta, mapOf(kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne))
        val resultado = alloc<CFTypeRefVar>()
        val estado = SecItemCopyMatching(dict, resultado.ptr)
        CFBridgingRelease(dict)
        if (estado != errSecSuccess) return@memScoped null
        val datos = CFBridgingRelease(resultado.value) as? NSData ?: return@memScoped null
        NSString.create(data = datos, encoding = NSUTF8StringEncoding)?.toString()
    }

    private fun eliminar(cuenta: String) {
        val dict = consulta(cuenta)
        SecItemDelete(dict)
        CFBridgingRelease(dict)
    }

    private companion object {
        const val SERVICIO = "pe.edu.upeu.pharmamobil"
        const val CLAVE_TOKEN = "token"
        const val CLAVE_PERFIL = "perfil"
    }
}
