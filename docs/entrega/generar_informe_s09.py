# Genera S09_ActividadAutonoma_RodriguezBazan.docx a partir de las evidencias reales de docs/entrega/s08.
import glob
import os
import re
import subprocess

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
EV = os.path.join(AQUI, "s09")
TMP = os.path.join(EV, "_miniaturas")
os.makedirs(TMP, exist_ok=True)
AZUL = RGBColor(0x1F, 0x4E, 0x79)
AZUL_HEX, CELESTE_HEX, GRIS_HEX = "1F4E79", "9DC3E6", "EAF2FA"
FUENTE = "Calibri"

doc = Document()
sec = doc.sections[0]
sec.page_width, sec.page_height = Cm(21), Cm(29.7)
sec.left_margin = sec.right_margin = Cm(2.2)
sec.top_margin = sec.bottom_margin = Cm(2.2)

for nombre, tam, color in (("Normal", 10.5, None), ("Heading 1", 16, AZUL), ("Heading 2", 13, AZUL), ("Heading 3", 11, AZUL)):
    st = doc.styles[nombre]
    st.font.name = FUENTE
    st.font.size = Pt(tam)
    st.element.rPr.rFonts.set(qn("w:eastAsia"), FUENTE)
    st.element.rPr.rFonts.set(qn("w:ascii"), FUENTE)
    st.element.rPr.rFonts.set(qn("w:hAnsi"), FUENTE)
    if color:
        st.font.color.rgb = color
        st.font.bold = True


def parrafo(texto="", negrita=False, tam=None, centrado=False, color=None, cursiva=False, espacio=4):
    p = doc.add_paragraph()
    r = p.add_run(texto)
    r.bold, r.italic = negrita, cursiva
    if tam:
        r.font.size = Pt(tam)
    if color:
        r.font.color.rgb = color
    if centrado:
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(espacio)
    return p


def sombrear(celda, hexcolor):
    tcPr = celda._tc.get_or_add_tcPr()
    sh = OxmlElement("w:shd")
    sh.set(qn("w:val"), "clear")
    sh.set(qn("w:color"), "auto")
    sh.set(qn("w:fill"), hexcolor)
    tcPr.append(sh)


def bordes(tabla_):
    b = OxmlElement("w:tblBorders")
    for lado in ("top", "left", "bottom", "right", "insideH", "insideV"):
        e = OxmlElement("w:" + lado)
        e.set(qn("w:val"), "single")
        e.set(qn("w:sz"), "6")
        e.set(qn("w:color"), CELESTE_HEX)
        b.append(e)
    tabla_._tbl.tblPr.append(b)


def tabla(encabezados, filas, anchos=None, tam=9):
    t = doc.add_table(rows=1, cols=len(encabezados))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    bordes(t)
    for i, h in enumerate(encabezados):
        c = t.rows[0].cells[i]
        c.text = ""
        r = c.paragraphs[0].add_run(h)
        r.bold = True
        r.font.size = Pt(tam)
        r.font.color.rgb = RGBColor(255, 255, 255)
        sombrear(c, AZUL_HEX)
    for n, fila in enumerate(filas):
        cs = t.add_row().cells
        for i, v in enumerate(fila):
            cs[i].text = ""
            r = cs[i].paragraphs[0].add_run(str(v))
            r.font.size = Pt(tam)
            if n % 2 == 1:
                sombrear(cs[i], GRIS_HEX)
    if anchos:
        t.autofit = False
        for fila in t.rows:
            for i, a in enumerate(anchos):
                fila.cells[i].width = Cm(a)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def ficha(pares):
    t = doc.add_table(rows=0, cols=2)
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    bordes(t)
    for k, v in pares:
        cs = t.add_row().cells
        cs[0].text = ""
        cs[1].text = ""
        r = cs[0].paragraphs[0].add_run(k)
        r.bold = True
        r.font.size = Pt(9)
        sombrear(cs[0], GRIS_HEX)
        r = cs[1].paragraphs[0].add_run(v)
        r.font.size = Pt(9)
        cs[0].width, cs[1].width = Cm(4.2), Cm(12.4)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def imagen(nombre, leyenda, ancho=4.3):
    im = Image.open(os.path.join(EV, nombre)).convert("RGB")
    im.thumbnail((620, 1380))
    mini = os.path.join(TMP, nombre.replace(".png", ".jpg"))
    im.save(mini, quality=82)
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(mini, width=Cm(ancho))
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.keep_with_next = True
    parrafo(leyenda, cursiva=True, tam=8.5, centrado=True, color=RGBColor(0x59, 0x59, 0x59), espacio=8)


def codigo(texto, tam=7.5):
    for linea in texto.strip("\n").splitlines():
        p = doc.add_paragraph()
        r = p.add_run(linea[:150])
        r.font.name = "Consolas"
        r.font.size = Pt(tam)
        r._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
        p.paragraph_format.space_after = Pt(0)
        sh = OxmlElement("w:shd")
        sh.set(qn("w:val"), "clear")
        sh.set(qn("w:color"), "auto")
        sh.set(qn("w:fill"), "F2F2F2")
        p._p.get_or_add_pPr().append(sh)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def salto():
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)


# ------------------------------------------------------------ encabezado y pie
hr = sec.header.paragraphs[0].add_run(
    "Universidad Peruana Unión · EP Ingeniería de Sistemas · DAM 2026-2 · Actividad Autónoma N.º 09")
hr.font.size = Pt(8)
hr.font.color.rgb = AZUL
fp = sec.footer.paragraphs[0]
fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
fr = fp.add_run("Julio Fabián Rodríguez Bazán · PharmaMobil · 12/10/2026")
fr.font.size = Pt(8)


# ------------------------------------------------------------ portada
parrafo()
parrafo()
parrafo("UNIVERSIDAD PERUANA UNIÓN", True, 22, True, AZUL)
parrafo("Facultad de Ingeniería y Arquitectura", False, 13, True)
parrafo("EP Ingeniería de Sistemas", False, 13, True, espacio=24)
parrafo("Desarrollo de Aplicaciones Móviles (DAM)", True, 14, True)
parrafo("Ciclo VI · Semestre 2026-2", False, 12, True, espacio=30)
parrafo("ACTIVIDAD AUTÓNOMA N.º 09", True, 20, True, AZUL, espacio=6)
parrafo("DIFERENCIAS POR PLATAFORMA Y EVIDENCIA EN IOS Y ANDROID", True, 14, True, AZUL, espacio=36)
ficha([
    ("Estudiante / Autor", "Julio Fabián Rodríguez Bazán"),
    ("Repositorio GitHub", "https://github.com/fabianrodriguez33/Pharma-Mobil"),
    ("Sesión", "Sesión 09 · Diferencias por plataforma y evidencia en iOS y Android"),
    ("Rama", "feature/expect-actual-rodriguez · https://github.com/fabianrodriguez33/Pharma-Mobil/tree/feature/expect-actual-rodriguez"),
    ("Fecha de entrega", "12/10/2026"),
])
salto()

# ------------------------------------------------------------ producto 1
doc.add_heading("Producto 1: Inventario de capacidades nativas", 1)
tabla(["Capacidad", "Declaración (commonMain)", "Android (androidMain)", "iOS (iosMain)"], [
    ["Formato de moneda", "expect fun formatearSoles(valor: Double): String · commonMain/.../platform/Formato.kt",
     "NumberFormat con Locale(\"es\", \"PE\") en androidMain/.../Formato.android.kt",
     "NSNumberFormatter con NSLocale(\"es_PE\") en iosMain/.../Formato.ios.kt"],
    ["Compartir producto", "interface Compartidor (domain) + Koin · commonMain/.../domain/platform/Compartidor.kt",
     "Intent.ACTION_SEND en androidMain/.../CompartidorAndroid.kt; registrado en PlatformModule.android.kt con androidContext()",
     "UIActivityViewController en iosMain/.../CompartidorIos.kt; registrado en PlatformModule.ios.kt"],
    ["Información del dispositivo", "expect class InfoDispositivo · commonMain/.../platform/InfoDispositivo.kt",
     "Build.VERSION.RELEASE y Build.MODEL en androidMain/.../InfoDispositivo.android.kt",
     "UIDevice.currentDevice en iosMain/.../InfoDispositivo.ios.kt"],
], anchos=[2.8, 4.2, 5.0, 4.6], tam=8.5)
salto()

# ------------------------------------------------------------ producto 2
doc.add_heading("Producto 2: Informe comparativo de diferencias", 1)
INFORME = [
    ("1. expect/actual frente a interfaz + Koin", [
        "En PharmaMobil usé dos estrategias. Para formatearSoles y InfoDispositivo elegí expect/actual: son funciones o "
        "clases puras, sin estado compartido ni dependencias del sistema operativo más allá de una API estática "
        "(NumberFormat, NSNumberFormatter, Build, UIDevice). El compilador exige una contrapartida por plataforma y el "
        "código común las invoca directamente, sin inyección.",
        "Compartidor, en cambio, necesita un Context en Android para lanzar un Intent. Eso es una dependencia con ciclo "
        "de vida, imposible de expresar en un expect sin argumentos. Por eso es una interfaz en domain, con "
        "CompartidorAndroid y CompartidorIos registrados en cada platformModule de Koin. La ventaja decisiva es la "
        "testabilidad: en commonTest puedo sustituirlo por un fake que guarde el texto recibido y comprobar "
        "ProductosViewModel sin tocar ningún Intent. Una función expect no se puede sustituir así sin añadir otra capa.",
        'En términos de diseño, expect/actual acopla el código común a una implementación fija por destino, mientras que la interfaz permite elegir la implementación en tiempo de ejecución, algo útil para pruebas, previsualizaciones o futuras plataformas como escritorio.']),
    ("2. Qué ocurre cuando falta un actual", [
        "Para observarlo eliminé temporalmente Formato.android.kt y compilé con ./gradlew :shared:compileAndroidMain. "
        "El compilador respondió, literalmente: «e: file:///D:/DAM/pharmaMobil-master/shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/platform/Formato.kt:7:1 Expected formatearSoles has no actual declaration in module <commonMain> for JVM» "
        "y la tarea terminó en BUILD FAILED (s09/compilador_falta_actual.log). Es un error de compilación, no de ejecución: "
        "el proyecto no genera el artefacto hasta que exista el actual. Restauré el archivo y el build volvió a pasar.",
        "Esta es la gran ventaja de expect/actual: la omisión se detecta antes de entregar la app. Con una interfaz + "
        "Koin, en cambio, el olvido aparecería en ejecución, como NoDefinitionFoundException al resolver Compartidor. "
        "Por eso las pruebas de módulo (AppModuleTest) resultan imprescindibles para ese enfoque.",
        'Cada source set debe aportar exactamente un actual con la misma firma, incluidos nombres y tipos de las propiedades de InfoDispositivo.']),
    ("3. Contexto en Android frente a iOS", [
        "En Android, startActivity pertenece a Context. CompartidorAndroid recibe androidContext() de Koin, que es el "
        "contexto de aplicación, no de una Activity; por eso el selector se lanza con Intent.FLAG_ACTIVITY_NEW_TASK, "
        "pues sin una Activity la tarea no existe y el sistema lanzaría una excepción.",
        "En iOS no hay Context. UIKit es un conjunto de singletons: CompartidorIos obtiene "
        "UIApplication.sharedApplication.keyWindow?.rootViewController y presenta el UIActivityViewController encima. "
        "Ese constructor sin argumentos explica por qué se registra sin parámetros en PlatformModule.ios.kt.",
        'La consecuencia de diseño es que Android obliga a propagar una dependencia de plataforma por Koin, mientras que iOS accede directamente a la jerarquía de ventanas. Además, keyWindow puede ser nulo si no hay ventana activa, por lo que uso el operador seguro y la llamada simplemente no hace nada en ese caso.']),
    ("4. Interoperabilidad Kotlin-Swift", [
        "Al exportar el framework, Kotlin se traduce así: String → String (NSString puenteado); Double → Double; "
        "suspend fun → función async/await o completion handler, según la versión de Swift; sealed class → clase base "
        "con subclases (no un enum nativo, aunque el patrón es equivalente); tipos nulables → Optional (String? → "
        "String?); Unit → Void. Por eso mantengo firmas simples en los límites de plataforma.",
        'En Objective-C los genéricos y los tipos primitivos nulables pierden información, así que en la frontera conviene exponer tipos simples. Mis tres capacidades usan solo String y Double, lo que evita estas pérdidas.']),
    ("5. Diferencias visuales al compartir", [
        "En Android, Intent.createChooser muestra una hoja inferior propia del sistema con aplicaciones destino, vista "
        "previa del texto y accesos de copiado (ver captura del Producto 4). Cada fabricante la adapta. En iOS, "
        "UIActivityViewController presenta la hoja de actividades con fila de contactos, iconos de apps y acciones "
        "como «Copiar». Ambas son nativas y el usuario las reconoce; mi código común solo entrega el texto, sin "
        "decidir su apariencia.",
        'Una diferencia práctica: en Android el texto llega como ACTION_SEND con tipo text/plain y el sistema filtra las aplicaciones capaces de recibirlo; en iOS los activityItems se ofrecen a las extensiones de compartir instaladas. En ambos casos el usuario conserva el control final de dónde se envía el contenido.']),
]
total = 0
for tit, ps in INFORME:
    doc.add_heading(tit, 2)
    for p in ps:
        parrafo(p)
        total += len(p.split())
    total += len(tit.split())
print("Palabras del informe:", total)
assert 600 <= total <= 900, total

doc.add_heading("Glosario Kotlin → Swift", 3)
tabla(["Kotlin", "Swift", "Nota"], [
    ["String", "String", "Puenteado con NSString"],
    ["Double", "Double", "Tipo primitivo directo"],
    ["suspend fun", "async/await o completion handler", "Según versión de Swift"],
    ["sealed class", "Clase base con subclases", "No se exporta como enum"],
    ["Nullability (T?)", "Optional (T?)", "Se conserva"],
    ["Unit", "Void", "Sin valor de retorno"],
], anchos=[3.6, 5.6, 7.4], tam=9)
salto()

# ------------------------------------------------------------ producto 3
doc.add_heading("Producto 3: Tercera capacidad nativa (InfoDispositivo)", 1)
parrafo("commonMain: platform/InfoDispositivo.kt", True)
codigo("""package pe.edu.upeu.pharmamobil.platform

expect class InfoDispositivo() {
    val sistemaOperativo: String
    val versionSistema: String
    val modeloDispositivo: String
}""")
parrafo("androidMain: platform/InfoDispositivo.android.kt", True)
codigo("""actual class InfoDispositivo actual constructor() {
    actual val sistemaOperativo: String = "Android"
    actual val versionSistema: String = Build.VERSION.RELEASE
    actual val modeloDispositivo: String = "${Build.MANUFACTURER} ${Build.MODEL}"
}""")
parrafo("iosMain: platform/InfoDispositivo.ios.kt", True)
codigo("""actual class InfoDispositivo actual constructor() {
    actual val sistemaOperativo: String = UIDevice.currentDevice.systemName
    actual val versionSistema: String = UIDevice.currentDevice.systemVersion
    actual val modeloDispositivo: String = UIDevice.currentDevice.model
}""")
parrafo("Integración en la interfaz: en presentation/productos/ProductosScreen.kt, el botón «Acerca del dispositivo» "
        "del catálogo abre un AlertDialog que muestra sistema, versión y modelo a partir de "
        "remember { InfoDispositivo() }. El único import es pe.edu.upeu.pharmamobil.platform.InfoDispositivo "
        "(paquete común).")
salto()

# ------------------------------------------------------------ producto 4
doc.add_heading("Producto 4: Evidencias, aislamiento y README", 1)
parrafo("Mensaje del compilador sin el actual: ver pregunta 2 del Producto 2 (s09/compilador_falta_actual.log).")
parrafo("Capturas del emulador Pixel 8 (Android 17) contra el backend PharmaSoft real (10.0.2.2:8080):")
imagen("01_catalogo_soles.png", "Figura 1. Catálogo con precio en soles (S/ 12.50) formateado por formatearSoles.", 4.6)
imagen("02_compartir.png", "Figura 2. Selector de compartir nativo de Android (Intent.createChooser).", 4.6)
imagen("03_info_dispositivo.png", "Figura 3. Tarjeta «Acerca del dispositivo» con InfoDispositivo.", 4.6)
doc.add_heading("Justificación de la ejecución en Windows/Android", 2)
parrafo("La compilación de iosMain y el simulador de iOS requieren macOS y Xcode; el equipo de desarrollo es Windows "
        "11, por lo que el destino iOS no se compiló ni se ejecutó. Conforme a la sección 3.4 de la guía autónoma, la "
        "evidencia en ejecución corresponde a Android, y los actual de iOS se entregan escritos y revisados contra la "
        "API de UIKit/Foundation, sin captura de simulador.")
doc.add_heading("Aislamiento verificado", 2)
parrafo("Búsqueda de importaciones prohibidas en shared/src/commonMain (import android.* y import platform.UIKit.*): "
        "0 coincidencias. Verificación: ./gradlew testAndroidHostTest assembleDebug → BUILD SUCCESSFUL.")
doc.add_heading("README.md", 2)
parrafo("Se añadió la sección «Código específico de plataforma» con las tres capacidades (formato de moneda, compartir "
        "e información del dispositivo) y su ubicación por source set.")

salida = os.path.join(AQUI, "S09_ActividadAutonoma_RodriguezBazan.docx")
doc.save(salida)
print(salida, os.path.getsize(salida))
