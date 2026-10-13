package pe.edu.upeu.pharmamobil.platform

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import pe.edu.upeu.pharmamobil.domain.platform.AlmacenSeguro

/** Cifra con AES-GCM usando una clave del Android Keystore; el texto cifrado va a SharedPreferences. */
class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val prefs = contexto.getSharedPreferences("almacen_seguro", Context.MODE_PRIVATE)

    override fun guardarToken(token: String) = guardar(CLAVE_TOKEN, token)
    override fun leerToken(): String? = leer(CLAVE_TOKEN)
    override fun guardarPerfil(json: String) = guardar(CLAVE_PERFIL, json)
    override fun leerPerfil(): String? = leer(CLAVE_PERFIL)

    override fun borrarToken() {
        prefs.edit().remove(id(CLAVE_TOKEN)).remove(id(CLAVE_PERFIL)).apply()
    }

    /** Los nombres de clave tambien se ofuscan (SHA-256): el XML no revela que hay un "token". */
    private fun id(clave: String): String =
        MessageDigest.getInstance("SHA-256").digest(clave.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun guardar(clave: String, valor: String) {
        val cifrado = Cipher.getInstance(TRANSFORMACION).apply { init(Cipher.ENCRYPT_MODE, claveSecreta()) }
        val bytes = cifrado.iv + cifrado.doFinal(valor.toByteArray(Charsets.UTF_8))
        prefs.edit().putString(id(clave), Base64.encodeToString(bytes, Base64.NO_WRAP)).apply()
    }

    private fun leer(clave: String): String? {
        val guardado = prefs.getString(id(clave), null) ?: return null
        return try {
            val bytes = Base64.decode(guardado, Base64.NO_WRAP)
            val cifrado = Cipher.getInstance(TRANSFORMACION).apply {
                init(Cipher.DECRYPT_MODE, claveSecreta(), GCMParameterSpec(128, bytes, 0, IV_BYTES))
            }
            String(cifrado.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
        } catch (e: Exception) {
            // Clave invalidada o dato corrupto: se descarta y el usuario vuelve a iniciar sesion.
            borrarToken()
            null
        }
    }

    private fun claveSecreta(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generador.generateKey()
    }

    private companion object {
        const val ALIAS = "pharmamobil_token_key"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val CLAVE_TOKEN = "token"
        const val CLAVE_PERFIL = "perfil"
    }
}
