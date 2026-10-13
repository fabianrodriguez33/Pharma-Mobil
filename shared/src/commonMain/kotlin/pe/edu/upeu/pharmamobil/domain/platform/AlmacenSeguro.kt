package pe.edu.upeu.pharmamobil.domain.platform

/** Almacen cifrado por plataforma (Keystore en Android, Keychain en iOS). */
interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?

    /** Datos no secretos de la sesion (usuario, permisos) para restaurarla al reabrir la app. */
    fun guardarPerfil(json: String)
    fun leerPerfil(): String?

    /** Borra token y perfil. */
    fun borrarToken()
}
