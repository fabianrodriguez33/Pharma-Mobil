"""Genera docs/entrega/S10_ActividadAutonoma_RodriguezBazan.docx con python-docx."""
import os

from docx import Document
from docx.enum.section import WD_ORIENT
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

AQUI = os.path.dirname(os.path.abspath(__file__))
SALIDA = os.path.join(AQUI, "..", "entrega", "S10_ActividadAutonoma_RodriguezBazan.docx")
NAVY = RGBColor(0x1F, 0x38, 0x64)
CELESTE = "9DC3E6"
ALT = "EAF3FB"
HEAD = "1F3864"

doc = Document()
sec = doc.sections[0]
sec.page_width, sec.page_height = Cm(21.0), Cm(29.7)
sec.left_margin = sec.right_margin = Cm(2.2)
sec.top_margin, sec.bottom_margin = Cm(2.2), Cm(2.0)

st = doc.styles["Normal"]
st.font.name = "Calibri"
st.font.size = Pt(11)
st.element.rPr.rFonts.set(qn("w:eastAsia"), "Calibri")
for nombre, size in (("Heading 1", 17), ("Heading 2", 13.5), ("Heading 3", 11.5)):
    h = doc.styles[nombre]
    h.font.name = "Calibri"
    h.font.size = Pt(size)
    h.font.bold = True
    h.font.color.rgb = NAVY
    h.element.rPr.rFonts.set(qn("w:eastAsia"), "Calibri")


def p(texto="", negrita=False, cursiva=False, size=None, align=None, after=6, color=None):
    par = doc.add_paragraph()
    par.paragraph_format.space_after = Pt(after)
    if align:
        par.alignment = align
    r = par.add_run(texto)
    r.bold, r.italic = negrita, cursiva
    if size:
        r.font.size = Pt(size)
    if color:
        r.font.color.rgb = color
    return par


def mixto(*partes, after=6):
    par = doc.add_paragraph()
    par.paragraph_format.space_after = Pt(after)
    for texto, negrita in partes:
        par.add_run(texto).bold = negrita
    return par


def vineta(texto):
    par = doc.add_paragraph(style="List Bullet")
    par.paragraph_format.space_after = Pt(2)
    par.add_run(texto)
    return par


def sombrear(celda, hexa):
    tcPr = celda._tc.get_or_add_tcPr()
    sh = OxmlElement("w:shd")
    sh.set(qn("w:val"), "clear")
    sh.set(qn("w:color"), "auto")
    sh.set(qn("w:fill"), hexa)
    tcPr.append(sh)


def bordes(tabla):
    tblPr = tabla._tbl.tblPr
    b = OxmlElement("w:tblBorders")
    for lado in ("top", "left", "bottom", "right", "insideH", "insideV"):
        e = OxmlElement(f"w:{lado}")
        e.set(qn("w:val"), "single")
        e.set(qn("w:sz"), "6")
        e.set(qn("w:color"), CELESTE)
        b.append(e)
    tblPr.append(b)


def tabla(encabezados, filas, anchos, centrar_desde=99, size=9.5):
    t = doc.add_table(rows=1, cols=len(encabezados))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    bordes(t)
    for i, e in enumerate(encabezados):
        c = t.rows[0].cells[i]
        c.width = Cm(anchos[i])
        sombrear(c, HEAD)
        par = c.paragraphs[0]
        r = par.add_run(e)
        r.bold, r.font.size = True, Pt(size)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        if i >= centrar_desde:
            par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for n, fila in enumerate(filas):
        cells = t.add_row().cells
        for i, v in enumerate(fila):
            cells[i].width = Cm(anchos[i])
            if n % 2:
                sombrear(cells[i], ALT)
            par = cells[i].paragraphs[0]
            r = par.add_run(str(v))
            r.font.size = Pt(size)
            if i >= centrar_desde:
                par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    doc.add_paragraph().paragraph_format.space_after = Pt(4)
    return t


def borde_imagen(run):
    pic = run._r.xpath(".//pic:spPr")[0]
    ln = OxmlElement("a:ln")
    ln.set("w", "19050")
    fill = OxmlElement("a:solidFill")
    clr = OxmlElement("a:srgbClr")
    clr.set("val", "1F3864")
    fill.append(clr)
    ln.append(fill)
    pic.append(ln)


def figura(archivo, leyenda, ancho_cm=None, alto_cm=None):
    par = doc.add_paragraph()
    par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    par.paragraph_format.space_after = Pt(2)
    par.paragraph_format.keep_with_next = True
    run = par.add_run()
    ruta = os.path.join(AQUI, archivo)
    if alto_cm:
        run.add_picture(ruta, height=Cm(alto_cm))
    else:
        run.add_picture(ruta, width=Cm(ancho_cm))
    borde_imagen(run)
    c = p(leyenda, cursiva=True, size=9, align=WD_ALIGN_PARAGRAPH.CENTER, after=8, color=RGBColor(0x44, 0x44, 0x44))
    return c


def fila_figuras(items, alto_cm=9.0):
    t = doc.add_table(rows=1, cols=len(items))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, (archivo, leyenda) in enumerate(items):
        c = t.rows[0].cells[i]
        par = c.paragraphs[0]
        par.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = par.add_run()
        run.add_picture(os.path.join(AQUI, archivo), height=Cm(alto_cm))
        borde_imagen(run)
        q = c.add_paragraph()
        q.alignment = WD_ALIGN_PARAGRAPH.CENTER
        rr = q.add_run(leyenda)
        rr.italic, rr.font.size = True, Pt(8.5)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def salto():
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)


def ficha(num, nombre, fecha, pasos, http, mensaje, ui, conclusion):
    doc.add_heading(f"Escenario {num}: {nombre}", level=3)
    tabla(["Campo", "Registro"], [
        ["Fecha / hora", fecha],
        ["Pasos ejecutados", pasos],
        ["Código HTTP", http],
        ["Mensaje literal (JSON message)", mensaje],
        ["Reacción de la UI", ui],
        ["Conclusión técnica", conclusion],
    ], [4.6, 12.0], size=9.5)


# ------------------------------------------------------------------ portada
for _ in range(2):
    p("")
p("UNIVERSIDAD PERUANA UNIÓN", negrita=True, size=22, align=WD_ALIGN_PARAGRAPH.CENTER, color=NAVY, after=4)
p("Facultad de Ingeniería y Arquitectura", size=14, align=WD_ALIGN_PARAGRAPH.CENTER, after=2)
p("E.P. Ingeniería de Sistemas", size=14, align=WD_ALIGN_PARAGRAPH.CENTER, after=30)
p("Curso: Desarrollo de Aplicaciones Móviles (DAM) · Ciclo VI / 2026-2", size=12, align=WD_ALIGN_PARAGRAPH.CENTER, after=40)
p("ACTIVIDAD AUTÓNOMA N.º 10", negrita=True, size=20, align=WD_ALIGN_PARAGRAPH.CENTER, color=NAVY, after=8)
p("VALIDACIÓN DE EXPIRACIÓN, ALMACENAMIENTO SEGURO Y CIERRE DE SESIÓN JWT EN PHARMAMOBIL",
  negrita=True, size=15, align=WD_ALIGN_PARAGRAPH.CENTER, after=50)
for k, v in (("Estudiante", "Julio Fabián Rodríguez Bazán"),
             ("Sesión", "10 · Sesión y token"),
             ("Repositorio Git", "https://github.com/fabianrodriguez33/Pharma-Mobil"),
             ("Rama entregada", "https://github.com/fabianrodriguez33/Pharma-Mobil/tree/feature/auth-jwt-rodriguez"),
             ("Fecha", "19/10/2026")):
    mixto((f"{k}: ", True), (v, False), after=3).alignment = WD_ALIGN_PARAGRAPH.CENTER
salto()

# ------------------------------------------------------------------ 1
doc.add_heading("1. Resumen y entorno de verificación", level=1)
p("Este informe documenta los cuatro productos exigidos por la Actividad Autónoma N.º 10: la bitácora de seis "
  "escenarios de token provocados, la evidencia del almacenamiento seguro, la verificación del cierre de sesión y la "
  "matriz de permisos por rol. Todo se ejecutó contra PharmaSoft real.")
tabla(["Elemento", "Detalle"], [
    ["App", "PharmaMobil (Kotlin Multiplatform + Ktor), rama feature/auth-jwt-rodriguez"],
    ["Backend", "PharmaSoft: Spring Boot + Spring Security JWT, Oracle Free 23ai (Docker, puerto 1522)"],
    ["Dispositivo", "Emulador Android Pixel 8, URL base http://10.0.2.2:8080/"],
    ["Usuarios semilla", "admin / Admin123 · vendedor / Vendedor123 · almacen / Almacen123"],
    ["Compilación", "./gradlew testAndroidHostTest assembleDebug → BUILD SUCCESSFUL (66 pruebas, 0 fallidas)"],
], [4.2, 12.4])
mixto(("Nota sobre los mensajes: ", True),
      ("los textos literales son los que devolvió el backend en esta ejecución. En el escenario 6 el backend responde "
       "«No tiene permisos para realizar esta operación» (el enunciado menciona «No tiene permisos suficientes»); "
       "se registra el literal real.", False))
mixto(("Nota sobre el gancho de prueba: ", True),
      ("para los escenarios 3 y 4 se usó un gancho temporal en AlmacenSeguroAndroid.leerToken() (alterar el último "
       "carácter de la firma o devolver null) activado por un archivo en el almacenamiento privado de la app. El gancho "
       "no forma parte del código entregado: se revirtió y la APK final se compiló sin él.", False))

# ------------------------------------------------------------------ 2 Producto 1
doc.add_heading("2. Producto 1: bitácora de 6 escenarios de token", level=1)
tabla(["N.º", "Escenario", "HTTP", "Mensaje del servidor", "Reacción de la app"], [
    ["1", "Token válido", "200", "—", "Lista el catálogo"],
    ["2", "Token expirado", "401", "El token ha expirado. Inicie sesión nuevamente", "Borra el token y va a Login con aviso"],
    ["3", "Token manipulado", "401", "El token no es válido", "Borra el token y va a Login con aviso"],
    ["4", "Token ausente", "401", "Debe iniciar sesión para acceder a este recurso", "Va a Login con aviso"],
    ["5", "Prefijo incorrecto", "401", "Debe iniciar sesión para acceder a este recurso", "No aplica a la UI (ver ficha)"],
    ["6a", "Sin permiso", "403", "No tiene permisos para realizar esta operación", "Mantiene la sesión y avisa en pantalla"],
    ["6b", "Usuario deshabilitado", "401", "El usuario está deshabilitado", "Borra el token y va a Login con aviso"],
], [1.1, 3.2, 1.3, 6.4, 4.6], centrar_desde=2, size=9)

ficha("1", "Token válido", "2026-10-13 00:11:27 (login) – 00:12:04 (consulta); curl 2026-10-12 23:56:32",
      "1) Login con admin / Admin123 en la app. 2) Abrir la pestaña Inventario. 3) El plugin Auth adjunta la cabecera "
      "y el backend responde.",
      "200 OK (Logcat: RESPONSE 200; curl: 200)", "No aplica (respuesta correcta)",
      "El inventario se lista con formulario y acciones de edición (rol admin).",
      "Línea base: con un token vigente y un rol con permiso, el flujo completo (login, almacén, cabecera automática, respuesta) funciona sin código manual de autenticación.")
figura("s1-token-valido.png", "Figura 1. Escenario 1: catálogo cargado con token válido", alto_cm=9.5)

ficha("2", "Token expirado", "Backend con 1 min: login 2026-10-13 00:08:55; consulta 00:09:50 (+55 s). curl: 2026-10-12 23:57:47 (+75 s)",
      "1) app.jwt.expiracion-minutos: 1 en application-dev.yaml y reinicio del backend. 2) Login con admin. 3) Esperar "
      "más de un minuto. 4) Abrir Inventario para forzar una consulta.",
      "401 Unauthorized (Logcat: RESPONSE 401)", "El token ha expirado. Inicie sesión nuevamente",
      "Vuelve a LoginScreen con «Tu sesión expiró. Inicia sesión nuevamente.».",
      "La expiración es un hecho temporal: el servidor lo detecta con la claim exp aunque la firma sea correcta. El cliente no puede saberlo de antemano (no valida la fecha), por eso la detección ocurre en la primera respuesta 401 y no al abrir la app.")
figura("s2-token-expirado.png", "Figura 2. Escenario 2: sesión expirada, redirección a Login con aviso", alto_cm=9.5)

ficha("3", "Token manipulado", "UI 2026-10-13 00:16:53; curl 00:18:07",
      "1) Login con admin. 2) Con el gancho temporal se altera el último carácter de la firma del token leído del "
      "almacén. 3) Abrir Inventario. 4) Revisar el archivo almacen_seguro.xml.",
      "401 Unauthorized", "El token no es válido",
      "Vuelve a LoginScreen con aviso; almacen_seguro.xml queda vacío (<map />).",
      "Aquí falla la integridad, no el tiempo: el HMAC no coincide, el mensaje cambia a «no es válido» y el token jamás se podría reutilizar; por eso es indispensable limpiar el almacén y no reintentar con él.")
figura("s3-token-manipulado.png", "Figura 3. Escenario 3: token manipulado, sesión cerrada", alto_cm=9.5)

ficha("4", "Token ausente / sin sesión", "UI 2026-10-13 00:17:43; curl 00:18:08",
      "1) Login con admin. 2) Con el gancho temporal el almacén devuelve null. 3) Abrir Inventario: la petición sale "
      "sin cabecera Authorization.",
      "401 Unauthorized", "Debe iniciar sesión para acceder a este recurso",
      "Vuelve a LoginScreen con aviso.",
      "Es un fallo de credencial, no de token: el filtro ni siquiera intenta validar nada y responde con el mensaje genérico de autenticación requerida; muestra que la guardia de la UI y la del servidor son independientes y se refuerzan.")
figura("s4-token-ausente.png", "Figura 4. Escenario 4: petición sin token, redirección a Login", alto_cm=9.5)

ficha("5", "Prefijo incorrecto o mal formado", "curl 2026-10-13 00:18:08",
      "1) Obtener un token válido. 2) Enviar «authorization: bearer <token>» (minúscula), «Bearer » sin token, el token "
      "sin prefijo y el esquema Basic.",
      "401 Unauthorized (las cuatro variantes)", "Debe iniciar sesión para acceder a este recurso",
      "No se reproduce en la UI: el plugin Auth siempre envía «Bearer »; la prueba es a nivel HTTP.",
      "El filtro solo reconoce el esquema «Bearer » exacto; cualquier variante equivale a no enviar credencial. Delegar la cabecera al plugin Auth evita este error de raíz, que sí es frecuente al escribirla a mano.")
figura("curl-bitacora.png", "Figura 5. Escenarios 5 y 6: salida de curl contra PharmaSoft (bitácora y matriz)", ancho_cm=15.5)

ficha("6a", "Acción sin permiso (403)", "UI 2026-10-13 00:20:28; curl con vendedor 00:18:11",
      "1) curl con vendedor: POST y DELETE de producto. 2) En la UI el vendedor no tiene formulario (acciones ocultas). "
      "3) Con una sesión de almacen abierta se le cambió el rol a VENDEDOR en la base y se intentó registrar un producto.",
      "403 Forbidden", "No tiene permisos para realizar esta operación",
      "La sesión sigue activa; se muestra «No tienes permiso para realizar esta acción» sobre la misma pantalla.",
      "Autenticado no es autorizado: el 403 indica que la identidad es válida pero el permiso no alcanza, por eso la sesión se conserva; cerrarla castigaría al usuario por un error de permisos y la UI ya debería haber ocultado la acción.")
fila_figuras([("s6b-vendedor-sin-formulario.png", "Figura 6a. vendedor: inventario sin formulario ni acciones"),
              ("s6a-403-sesion-activa.png", "Figura 6b. 403 con la sesión abierta")], alto_cm=9.0)

ficha("6b", "Usuario deshabilitado", "UI 2026-10-13 00:20:43; curl 00:20:43",
      "1) Sesión de almacen abierta. 2) UPDATE usuarios SET estado = 0. 3) Abrir Catálogo (nueva consulta). "
      "4) Se restauró estado = 1.",
      "401 Unauthorized", "El usuario está deshabilitado",
      "Vuelve a LoginScreen con aviso.",
      "El JWT no es suficiente por sí solo: el backend consulta el estado del usuario en cada petición, de modo que un administrador puede revocar el acceso al instante sin esperar a que el token expire.")
figura("s6c-usuario-deshabilitado.png", "Figura 7. Escenario 6b: usuario deshabilitado, sesión cerrada", alto_cm=9.5)

# ------------------------------------------------------------------ 3 Producto 2
doc.add_heading("3. Producto 2: evidencia del almacenamiento seguro", level=1)
mixto(("Implementación: ", True),
      ("AlmacenSeguroAndroid cifra con AES-256 en modo GCM usando una clave del Android Keystore (alias "
       "pharmamobil_token_key, no exportable). El IV de 12 bytes viaja antepuesto al texto cifrado y el resultado se "
       "guarda en Base64 en SharedPreferences (archivo almacen_seguro.xml). Los nombres de clave también se ofuscan "
       "con SHA-256. Se implementó con javax.crypto/Keystore y no con EncryptedSharedPreferences de Jetpack.", False))
doc.add_heading("Comprobación 1: almacén cifrado en disco", level=3)
p("Contenido real de shared_prefs/almacen_seguro.xml (leído con adb run-as, equivalente al Device Explorer). Ni el "
  "token ni el perfil aparecen en texto plano y los nombres de clave son resúmenes SHA-256.")
figura("p2-almacen_seguro.png", "Figura 8. almacen_seguro.xml: valores cifrados (AES-GCM) y claves ofuscadas", ancho_cm=15.5)
doc.add_heading("Comprobación 2: registro con cabecera saneada", level=3)
p("Con Ktor Logging en nivel HEADERS y sanitizeHeader sobre Authorization, Logcat imprime «Authorization: ***». "
  "El token nunca se escribe en el registro (el formato impreso es *** en lugar de «Bearer ***»).")
figura("p2-logcat.png", "Figura 9. Logcat: Authorization: *** en la petición autenticada", ancho_cm=15.0)
doc.add_heading("Comprobación 3: persistencia tras reinicio", level=3)
p("Se inició sesión, se mató el proceso (am force-stop) y se reabrió la app: entra directamente a la aplicación sin "
  "pedir login (2026-10-13 00:12:20).")
figura("p2-persistencia.png", "Figura 10. La app reabre con la sesión restaurada desde el almacén cifrado", alto_cm=9.5)

# ------------------------------------------------------------------ 4 Producto 3
doc.add_heading("4. Producto 3: cierre de sesión completo", level=1)
tabla(["Efecto", "Implementación", "Evidencia"], [
    ["1. Borrado físico del token", "AuthRepositoryRest.cerrarSesion() → almacen.borrarToken()", "Figura 11: almacen_seguro.xml pasa a <map />; prueba CerrarSesionTest"],
    ["2. Estado en memoria a null", "_sesion.value = null (StateFlow sesionActual)", "CerrarSesionTest: sesionActual y obtenerSesion() == null"],
    ["3. Redirección a LoginScreen", "App.kt: sesion == null → LoginScreen (SesionViewModel.salir())", "Figuras 12 y 13: Login inmediato y tras reiniciar"],
    ["4. Invalidar caché del plugin Auth", "client.limpiarTokenEnCache() → authProvider<BearerAuthProvider>()?.clearToken()", "CerrarSesionTest: la petición posterior sale sin cabecera Authorization"],
], [4.2, 6.6, 5.8], size=9)
figura("p3-almacen.png", "Figura 11. Efecto 1: almacén antes y después de cerrarSesion()", ancho_cm=15.5)
fila_figuras([("p3-0-antes-sesion-activa.png", "Figura 12a. Antes: sesión activa"),
              ("p3-1-login-tras-cierre.png", "Figura 12b. Después: LoginScreen"),
              ("p3-2-reinicio-sigue-en-login.png", "Figura 13. Reinicio: sigue en Login")], alto_cm=8.0)
p("La prueba automatizada usa un MockEngine: inicia sesión, verifica que la petición lleva «Bearer jwt-de-prueba», "
  "llama a cerrarSesion() y comprueba que el almacén queda vacío, la sesión es null y la siguiente petición ya no "
  "envía Authorization (la caché del plugin se invalidó).")
figura("p3-test.png", "Figura 14. Resultado de las pruebas: CerrarSesionTest y LoginViewModelTest en verde", ancho_cm=15.0)

# ------------------------------------------------------------------ 5 Producto 4
doc.add_heading("5. Producto 4: matriz de permisos por rol", level=1)
p("Resultados reales de 6 operaciones × 3 usuarios (HTTP contra PharmaSoft, 2026-10-13 00:18) y lo que ofrece la UI "
  "de PharmaMobil.")
tabla(["Operación", "admin", "vendedor", "almacen"], [
    ["Listar productos", "200 OK (UI completa)", "200 OK (solo lectura)", "200 OK (lectura/escritura)"],
    ["Registrar/Editar productos", "201 Created", "403 Forbidden (formulario oculto)", "201 Created* (formulario visible)"],
    ["Listar clientes", "200 OK", "200 OK", "403 Forbidden (pestaña oculta)"],
    ["Registrar venta**", "400 (autorizado)", "400 (autorizado)", "403 Forbidden (sección oculta)"],
    ["Ver reportes", "200 OK", "403 Forbidden", "403 Forbidden"],
    ["Consultar /auth/me", "200 OK", "200 OK", "200 OK"],
], [4.2, 4.0, 4.2, 4.2], centrar_desde=1, size=9)
p("* En la ejecución de la matriz el primer intento de almacen devolvió 409 porque el nombre ya existía (autorizado); "
  "con un nombre distinto devolvió 201. ** Cuerpo vacío: 400 de validación indica permiso concedido; 403, denegado. "
  "La app aún no tiene módulo de ventas ni de reportes, por lo que esas filas se verificaron por HTTP.", size=9, after=8)
doc.add_heading("Configuración restaurada", level=3)
vineta("app.jwt.expiracion-minutos: 60 en application-dev.yaml de PharmaSoft (backend reiniciado).")
vineta("Usuario almacen con estado = true y rol ALMACENERO tras las pruebas de deshabilitado y de cambio de rol.")
vineta("README.md: sección «Seguridad y Gestión de Tokens JWT» agregada en el repositorio.")

# ------------------------------------------------------------------ 6
doc.add_heading("6. Limitaciones", level=1)
vineta("iOS: AlmacenSeguroIos (Keychain) está implementado pero no se compiló ni probó (requiere Mac con Xcode).")
vineta("Los escenarios 3 y 4 en la UI requirieron un gancho temporal de prueba (no incluido en el código entregado).")
vineta("El escenario 5 se evidencia a nivel HTTP: la app no puede emitir un prefijo distinto de «Bearer».")
vineta("El aviso 403 se muestra como texto en la pantalla del formulario, no como Snackbar.")
vineta("La implementación usa Keystore + AES-GCM directos; no emplea EncryptedSharedPreferences.")

doc.save(SALIDA)
print("ok", os.path.abspath(SALIDA))
