"""Genera docs/entrega/RodriguezBazan_Julio_Sesion13_Evidencias.pdf (actividad autonoma 13)."""
import os
import sys

from PIL import Image as PILImage
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (BaseDocTemplate, Frame, Image, KeepTogether, PageBreak, PageTemplate, Paragraph,
                                Spacer, Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

RAIZ = os.path.dirname(os.path.abspath(__file__))
AUT = os.path.join(RAIZ, "docs", "s13", "autonoma")
CAP = os.path.join(RAIZ, "docs", "s13", "capturas")
TMP = os.path.join(os.environ.get("TEMP", RAIZ), "s13_jpg")
SALIDA = os.path.join(RAIZ, "docs", "entrega", "RodriguezBazan_Julio_Sesion13_Evidencias.pdf")
COMMIT = "ac97f0d270807ef852b61ed6146a5027b4918383"
REPO = "https://github.com/fabianrodriguez33/Pharma-Mobil"
NAVY = colors.HexColor("#1F3864")
ALT = colors.HexColor("#EAF3FB")
os.makedirs(TMP, exist_ok=True)

ss = getSampleStyleSheet()
N = ParagraphStyle("N", parent=ss["Normal"], fontName="Helvetica", fontSize=9.6, leading=13, spaceAfter=4)
C = ParagraphStyle("C", parent=N, fontSize=8.3, leading=10.8, spaceAfter=0)
PIE = ParagraphStyle("PIE", parent=N, fontSize=7.4, leading=9, alignment=TA_CENTER, textColor=colors.HexColor("#444444"),
                     spaceAfter=0)
H1 = ParagraphStyle("H1", parent=N, fontName="Helvetica-Bold", fontSize=15, leading=19, textColor=NAVY, spaceBefore=4,
                    spaceAfter=7)
H2 = ParagraphStyle("H2", parent=N, fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=NAVY, spaceBefore=4,
                    spaceAfter=3)
HS = ParagraphStyle("HS", parent=C, textColor=colors.white, fontName="Helvetica-Bold")


def jpg(ruta):
    destino = os.path.join(TMP, os.path.basename(ruta).replace(".png", ".jpg"))
    im = PILImage.open(ruta).convert("RGB")
    im.thumbnail((620, 1380))
    im.save(destino, quality=82)
    return destino, im.size


def captura(ruta, ancho, pie):
    destino, (w, h) = jpg(ruta)
    img = Image(destino, width=ancho * cm, height=ancho * cm * h / w)
    t = Table([[img], [Paragraph(pie, PIE)]], colWidths=[ancho * cm])
    t.setStyle(TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("BOX", (0, 0), (0, 0), 0.4, colors.HexColor("#9DC3E6")),
                           ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                           ("TOPPADDING", (0, 1), (0, 1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 0)]))
    return t


def tabla(filas, anchos, cab=True):
    datos = [[Paragraph(str(c), HS if (cab and i == 0) else C) for c in f] for i, f in enumerate(filas)]
    t = Table(datos, colWidths=[a * cm for a in anchos], repeatRows=1 if cab else 0)
    est = [("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#9DC3E6")), ("VALIGN", (0, 0), (-1, -1), "TOP"),
           ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3)]
    if cab:
        est.append(("BACKGROUND", (0, 0), (-1, 0), NAVY))
        est += [("BACKGROUND", (0, i), (-1, i), ALT) for i in range(2, len(datos), 2)]
    t.setStyle(TableStyle(est))
    return t


class Doc(BaseDocTemplate):
    def __init__(self, *a, **k):
        super().__init__(*a, **k)
        marco = Frame(self.leftMargin, self.bottomMargin, self.width, self.height, id="f")
        self.addPageTemplates([PageTemplate(id="p", frames=[marco], onPage=self.pie)])

    def pie(self, canvas, doc):
        if doc.page > 1:
            canvas.saveState()
            canvas.setFont("Helvetica", 7.5)
            canvas.setFillColor(colors.grey)
            canvas.drawCentredString(letter[0] / 2, 1.0 * cm,
                                     "Actividad Autónoma N.º 13 · Rodríguez Bazán, Julio Fabián · Página %d" % doc.page)
            canvas.restoreState()

    def afterFlowable(self, f):
        if isinstance(f, Paragraph) and f.style.name in ("H1", "H2"):
            self.notify("TOCEntry", (0 if f.style.name == "H1" else 1, f.getPlainText(), self.page))


def paso(n, titulo, imagenes, estado, nota=None, ancho=3.9):
    """Un paso del mapa: capturas a la izquierda; texto y estado del sistema a la derecha."""
    celdas = [captura(r, ancho, p) for r, p in imagenes]
    izq = Table([celdas], colWidths=[(ancho + 0.25) * cm] * len(celdas))
    izq.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0),
                             ("RIGHTPADDING", (0, 0), (-1, -1), 4)]))
    texto = [Paragraph("<b>Paso %d · %s</b>" % (n, titulo), H2),
             Paragraph("<b>Estado del sistema:</b> " + estado, N)]
    if nota:
        texto.append(Paragraph("<b>Observación:</b> " + nota, N))
    ancho_izq = (ancho + 0.25) * len(imagenes)
    t = Table([[izq, texto]], colWidths=[ancho_izq * cm, (17.4 - ancho_izq) * cm])
    t.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0),
                           ("RIGHTPADDING", (0, 0), (-1, -1), 0), ("BOTTOMPADDING", (0, 0), (-1, -1), 8)]))
    return KeepTogether([t])


A = lambda n: os.path.join(AUT, n)
K = lambda n: os.path.join(CAP, n)
E = []

# ------------------------------------------------------------ Portada
E += [Spacer(1, 2.6 * cm),
      Paragraph("Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas",
                ParagraphStyle("a", parent=N, alignment=TA_CENTER, textColor=NAVY)),
      Spacer(1, 0.9 * cm),
      Paragraph("DESARROLLO DE APLICACIONES MÓVILES",
                ParagraphStyle("b", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=19, leading=24,
                               textColor=NAVY)),
      Paragraph("Actividad Autónoma N.º 13",
                ParagraphStyle("c", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=24, leading=30,
                               textColor=NAVY)),
      Paragraph("Documentar el flujo completo y las evidencias por plataforma",
                ParagraphStyle("d", parent=N, alignment=TA_CENTER, fontSize=13, leading=17)),
      Spacer(1, 1.3 * cm),
      tabla([["Dato", "Detalle"],
             ["Estudiante", "Julio Fabián Rodríguez Bazán"],
             ["Código", "202410354"],
             ["Grupo", "Grupo 1"],
             ["Docente", "Benjamin David Reyna Barreto"],
             ["Ciclo / Semestre", "VI · 2026-2"],
             ["Unidad / Sesión", "Unidad 3 · Sesión 13"],
             ["Proyecto", "PharmaMobil (KMP) sobre PharmaSoft"],
             ["Repositorio", REPO],
             ["Rama / commit evaluado", "feature/integrador-13 · " + COMMIT[:7]],
             ["Entrega", "Lunes 9 de noviembre de 2026, 23:59"]], [4.6, 12.8]),
      PageBreak()]

# ------------------------------------------------------------ Índice
toc = TableOfContents()
toc.levelStyles = [ParagraphStyle("t0", fontName="Helvetica-Bold", fontSize=10.5, leading=15, leftIndent=0),
                   ParagraphStyle("t1", fontName="Helvetica", fontSize=9.3, leading=12.5, leftIndent=16)]
E += [Paragraph("Índice", ParagraphStyle("ti", parent=H1, fontSize=17)), toc, Spacer(1, 10),
      Paragraph("Entorno de las pruebas", H2),
      tabla([["Elemento", "Detalle"],
             ["Dispositivo", "Emulador Android Pixel 8 · Android 17 (API 37.1) · app de depuración instalada desde cero (pm clear)"],
             ["Backend", "PharmaSoft (Spring Boot) en localhost:8080 con Oracle 23ai Free en Docker; la app lo alcanza por 10.0.2.2"],
             ["Usuarios usados", "admin (ADMIN) y vendedor (VENDEDOR), cuentas de desarrollo de las migraciones del backend"],
             ["Cómo se obtuvo el estado", "Base local (pharma.db) y almacén seguro leídos con adb run-as; respuesta del servicio con el cliente HTTP"],
             ["Plataformas", "Android compilado y probado; iOS sin compilar en este equipo (ver Producto B)"]],
            [4.0, 13.4])]
E += [Spacer(1, 6),
      Paragraph("Todas las capturas son pantallas del emulador, sin recorte del monitor; cada una lleva su pie.", N),
      PageBreak()]

# ------------------------------------------------------------ Producto A
E += [Paragraph("1. Producto A · Mapa del flujo completo", H1),
      Paragraph("Recorrido de un usuario nuevo, en orden, con la captura y el estado del sistema en cada paso. "
                "La hora de cada sincronización sale de la tabla <i>sincronizacion</i> de la base local.", N)]

E.append(paso(1, "Primera apertura, sin sesión",
              [(A("p01_sin_sesion.png"), "Login tras instalar desde cero")],
              "base local inexistente (el archivo pharma.db aún no se crea) y sin token guardado "
              "(almacen_seguro.xml no existe). Solo es accesible la pantalla de inicio de sesión: guardia de sesión."))
E.append(paso(2, "Inicio de sesión correcto (admin)",
              [(A("p02_login_ok.png"), "Inicio tras ingresar como admin")],
              "token JWT guardado cifrado (AES-GCM con clave del Android Keystore) en almacen_seguro.xml, con dos "
              "entradas (token y perfil) cuyos nombres son hashes SHA-256. Rol <b>ADMIN</b>; privilegios: CATALOGO_LEER, "
              "CATALOGO_ESCRIBIR, CLIENTES_LEER, CLIENTES_ESCRIBIR, VENTAS_LEER, VENTAS_REGISTRAR, REPORTES_VER, "
              "USUARIOS_ADMINISTRAR. El token vence a los 3600 s."))
E.append(paso(3, "Listado tras la primera sincronización",
              [(A("p03_primera_sync.png"), "Catálogo recién sincronizado")],
              "<b>4 filas</b> descargadas (ids 1, 43, 44 y 65), todas con pendiente = 0 e intentos = 0. Hora de la "
              "sincronización: <b>04/11/2026 15:06:47</b>."))
E.append(paso(4, "Creación de un registro con conexión",
              [(A("p04_creado_con_red.png"), "Inventario con Cetirizina ya creada")],
              "producto «Cetirizina» con identificador <b>66</b> asignado por el servicio; pendiente = 0; sincronizada a "
              "las 15:08:10.",
              "la lista también muestra la fila «AB», creada en el paso 7; la captura se tomó después."))
E.append(paso(5, "Creación de un registro con modo avión",
              [(A("p05_creado_modo_avion.png"), "Modo avión: 1 cambio por enviar")],
              "«Omeprazol» guardada con <b>identificador local provisional -1</b>, pendiente = 1, intentos = 2 y "
              "ultimoError = «Sin conexión con el servidor». El Catálogo muestra «Sin conexión · 1 cambio por enviar»."))
E.append(paso(6, "Sincronización al recuperar la señal",
              [(A("p06_sync_recuperada.png"), "Tras reconectar y sincronizar")],
              "pendientes <b>antes: 1</b> (id -1) y <b>después: 0</b>. Identificador definitivo recibido: <b>67</b>. "
              "Sincronización a las 15:08:49; la fila local -1 ya no existe."))
E.append(paso(7, "Error de validación provocado a propósito",
              [(A("p07b_validacion_400.png"), "400: nombre de 2 letras (mínimo 3)"),
               (A("p_revisar_fallida.png"), "409: «Aspirina» ya existe en el servidor")],
              "con el nombre «AB» el servicio devolvió <b>400</b> con el mensaje del campo <b>nombre</b>: «El nombre debe "
              "tener entre 3 y 150 caracteres». La fila quedó pendiente = 1, intentos = 5 (agotados) y el motivo guardado. "
              "Con «Aspirina» devolvió <b>409</b> («Ya existe un producto con el nombre Aspirina»): ese nombre pertenece a "
              "un producto dado de baja (id 42).",
              "el formulario dijo «registrado correctamente» y el mensaje del servicio no llega al campo: aparece después, "
              "en el diálogo «Cambios que no se pudieron enviar». Es un hueco abierto de la ficha (N.º 9).", ancho=3.5))
E.append(paso(8, "Operación sin privilegio suficiente",
              [(A("p08_sin_privilegio.png"), "Vendedor: 1 cambio no se pudo enviar"),
               (A("p08b_detalle_403.png"), "Detalle: sin permiso (403)")],
              "se creó «Pantoprazol» sin red como admin, se cerró sesión y se ingresó como <b>vendedor</b> (solo "
              "CATALOGO_LEER, CLIENTES_*, VENTAS_*). Al sincronizar, el servicio respondió <b>403</b>; la fila quedó "
              "fallida (intentos = 5) con «Sin permiso para esta operación». La interfaz no se bloquea: muestra "
              "«1 cambio no se pudo enviar» con Revisar y Reintentar ahora.",
              "la cola local no pertenece a un usuario: la sesión nueva intentó enviar lo del usuario anterior. "
              "Hueco abierto N.º 10.", ancho=3.5))
E.append(paso(9, "Token vencido",
              [(A("p09_token_vencido.png"), "Login con aviso de sesión expirada")],
              "se invalidó el token reiniciando el backend con otra clave JWT y se pulsó Sincronizar: el servicio "
              "respondió <b>401</b>. La app borró el token (almacen_seguro.xml queda vacío), volvió al login con el aviso "
              "«Tu sesión expiró. Inicia sesión nuevamente.» y conservó el usuario escrito. Los cambios pendientes siguen "
              "guardados en la base local."))
E.append(paso(10, "Cierre de sesión",
              [(A("p10_tras_cerrar_sesion.png"), "Login tras cerrar sesión como admin")],
              "token <b>borrado</b> (almacen_seguro.xml queda vacío), pero la <b>base local conserva las 5 filas</b>, "
              "incluida la pendiente, y el campo Usuario queda con «admin».",
              "el equipo no queda limpio para otro usuario (hueco N.º 7 de la ficha)."))
E.append(PageBreak())

# ------------------------------------------------------------ Producto B
E += [Paragraph("2. Producto B · Evidencias por plataforma", H1),
      Paragraph("Android se documentó de principio a fin en el apartado anterior. Para iOS no hay evidencia propia: se "
                "indica el estado real y la comparación se basa en el código (marcado como «por código»).", N),
      tabla([["Aspecto", "Android (verificado)", "iOS (por código, no ejecutado)"],
             ["Almacenamiento del token",
              "AES-GCM con clave del Android Keystore; el texto cifrado va a SharedPreferences. Tras borrar los datos de "
              "la app no queda token (comprobado en el paso 1). Si el Keystore invalida la clave, el token se descarta.",
              "Llavero (Keychain) mediante AlmacenSeguroIos. El Llavero puede conservar datos al reinstalar: "
              "comportamiento por confirmar en un iPhone."],
             ["Permisos",
              "Diálogo del sistema «Allow PharmaMobil to send you notifications?» (texto del sistema, en inglés por "
              "el idioma del emulador). Se muestra 2 veces; tras la segunda negativa el sistema ya no lo muestra y solo "
              "queda Ajustes (botón «Abrir ajustes»).",
              "El sistema pregunta una sola vez; si se deniega solo queda Ajustes. El texto lo da la clave de "
              "Info.plist (NSCameraUsageDescription y NSLocationWhenInUseUsageDescription, ya declaradas en español)."],
             ["Comportamiento sin red",
              "Con modo avión: se guarda con id negativo y distintivo «Pendiente de envío»; el Catálogo dice «Sin "
              "conexión · N cambios por enviar»; al volver la señal se pulsa Sincronizar (pasos 5 y 6).",
              "Mismo código común (SQLDelight + Ktor); sin diferencias esperadas. Falta comprobarlo en ejecución."],
             ["Notificaciones",
              "Android 13+ exige POST_NOTIFICATIONS (declarado). Si se deniega, la app sigue usable y la tarjeta ofrece "
              "reintentar o ir a Ajustes. Aún no se emiten notificaciones: llegan con FCM en la sesión 14.",
              "GestorPermisosIos solo abre Ajustes; los diálogos nativos de notificación no están implementados."],
             ["Diferencias visuales",
              "Pantalla de borde a borde, barras del sistema transparentes y pantalla de arranque con el logo.",
              "No observadas (sin ejecución)."]],
            [3.0, 7.4, 7.0]),
      Spacer(1, 8),
      Paragraph("Estado real del objetivo de iOS (04/11/2026)", H2),
      tabla([["Punto", "Detalle"],
             ["Qué falta", "Enlazar el framework Shared y ejecutar la app en un simulador o iPhone con Xcode (sin ejecución no hay evidencia visual)."],
             ["Qué lo impide", "Este equipo es Windows: Xcode, el enlazado del framework Shared y el simulador de iOS "
                               "solo existen en macOS. No hay un Mac disponible."],
             ["Qué se hizo", "El código iOS existe (AlmacenSeguroIos, CompartidorIos, GestorPermisosIos, Info.plist con "
                              "claves de uso). Compila a klib en este equipo (ver resultado abajo)."],
             ["Plan", "Sesión 14 (10/11/2026): usar un Mac del laboratorio, compilar, añadir GoogleService-Info.plist y los "
                      "pods de Firebase, completar GestorPermisosIos y repetir el recorrido en el simulador."]],
            [3.0, 14.4]),
      Spacer(1, 4),
      Paragraph("<b>Resultado de la compilación iOS (04/11/2026):</b> la tarea compileKotlinIosSimulatorArm64 terminó con BUILD SUCCESSFUL en 2 min 25 s, es decir, todo el código común y el de iosMain (incluido GestorPermisosIos y las dependencias de Firebase) compila a klib en este equipo. Lo que no se puede hacer aquí es enlazar el framework y ejecutar la app.", N),
      PageBreak()]

# ------------------------------------------------------------ Producto C
E += [Paragraph("3. Producto C · Bitácora de permisos", H1),
      Paragraph("Permiso usado: <b>POST_NOTIFICATIONS</b> (Permiso.NOTIFICACIONES en el código común), resuelto por "
                "GestorPermisos con los estados Concedido, Denegado y DenegadoParaSiempre. La interfaz siempre explica "
                "antes de pedir. CAMERA y ACCESS_FINE_LOCATION están declarados en el manifiesto, pero la app aún no los "
                "solicita, por eso no figuran en la bitácora.", N),
      Table([[captura(K("07_concedido.png"), 3.9, "Concedido"),
              captura(K("02_catalogo.png"), 3.9, "Denegado (se puede volver a pedir)"),
              captura(K("03_explicacion.png"), 3.9, "Explicación antes de pedir"),
              captura(K("06_denegado_para_siempre.png"), 3.9, "Denegado para siempre")]],
            colWidths=[4.35 * cm] * 4, style=[("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0)]),
      Spacer(1, 8),
      tabla([["Permiso", "Estado", "Qué ve el usuario", "Cómo recupera la función"],
             ["POST_NOTIFICATIONS", "Concedido", "Tarjeta «Avisos activados: te avisaremos si un cambio no se puede "
                                                  "enviar». Sin botones.", "No hace falta; si lo revoca en Ajustes, la "
                                                                           "tarjeta cambia al refrescar."],
             ["", "Denegado", "«Avisos desactivados» con el botón «Activar avisos». Al pulsarlo, un diálogo explica para "
                              "qué se usan y, al continuar, aparece el del sistema.",
              "Pulsar «Activar avisos» y aceptar el diálogo del sistema (se puede pedir una vez más)."],
             ["", "Denegado para siempre", "Tarjeta «Avisos bloqueados» y diálogo «Permiso bloqueado»: el sistema ya no "
                                            "mostrará el aviso.",
              "Botón <b>Abrir ajustes</b> (pantalla de detalles de la app en Ajustes); al volver, la app refresca el "
              "estado sola. La app sigue funcionando sin los avisos."]],
            [3.6, 2.5, 6.2, 5.1]),
      Spacer(1, 6),
      Paragraph("Cómo se llega al estado definitivo en Android: el sistema devuelve «no mostrar explicación» tanto antes de la "
                "primera solicitud como tras una negativa definitiva; por eso GestorPermisosAndroid recuerda si el permiso "
                "ya se pidió. Primera vez y negativa única se ven como Denegado; la segunda negativa pasa a DenegadoParaSiempre "
                "(captura «Denegado para siempre», diálogo «Permiso bloqueado»).", N),
      PageBreak()]

# ------------------------------------------------------------ Producto D
E += [Paragraph("4. Producto D · Ficha de integración", H1),
      Paragraph("Huecos del recorrido, ya con decisión. «Cerrado hoy» = 04/11/2026; «Sesión 14» = 10/11/2026.", N),
      tabla([["N.º", "Hueco detectado", "Gravedad", "Decisión", "Fecha de cierre"],
             ["1", "Listado vacío sin acción y con texto que parece error", "Media", "Cerrado hoy", "04/11/2026"],
             ["2", "Fila pendiente indistinguible de las sincronizadas", "Alta", "Cerrado hoy (distintivo y contador)", "04/11/2026"],
             ["3", "Cola sin límite ni memoria de intentos; fila atascada para siempre", "Alta", "Cerrado hoy (intentos, espera, fallidas)", "04/11/2026"],
             ["4", "Ningún permiso resuelto", "Alta", "Cerrado hoy (GestorPermisos)", "04/11/2026"],
             ["5", "Sin telemetría de fallos", "Media", "Cerrado hoy (Telemetria + Firebase)", "04/11/2026"],
             ["6", "Token vencido a mitad de un formulario pierde lo escrito", "Media", "Sesión 14", "10/11/2026"],
             ["7", "Cierre de sesión deja datos locales y el usuario escrito", "Media", "Sesión 14", "10/11/2026"],
             ["8", "Reintento tras tiempo agotado puede duplicar la creación (sin clave de operación)", "Alta", "Sesión 14 (requiere cambio en PharmaSoft)", "10/11/2026"],
             ["9", "El error 400/409 del servicio no llega al campo; el formulario dice «registrado correctamente»", "Media", "Sesión 14", "10/11/2026"],
             ["10", "La cola local no pertenece a un usuario: otra sesión envía lo del anterior (403)", "Alta", "Sesión 14", "10/11/2026"],
             ["11", "GestorPermisosIos mínimo; iOS sin compilar", "Media", "Sesión 14 (requiere Mac)", "10/11/2026"],
             ["12", "Un nombre de producto dado de baja sigue bloqueando uno nuevo (409)", "Baja", "Descartado: es regla del backend PharmaSoft, no de la app", "—"],
             ["13", "Módulo Pedidos «en construcción»", "Baja", "Descartado: está planificado para otra sesión del curso", "—"]],
            [0.9, 6.6, 2.1, 5.3, 2.5]),
      Spacer(1, 6),
      Paragraph("Los huecos 9, 10 y 11 se descubrieron al ejecutar el recorrido de esta actividad; no estaban en la tabla "
                "del paso 1 de la guía práctica.", N),
      PageBreak()]

# ------------------------------------------------------------ Cierre
E += [Paragraph("5. Repositorio y commit evaluado", H1),
      tabla([["Elemento", "Enlace"],
             ["Repositorio", REPO],
             ["Rama", REPO + "/tree/feature/integrador-13"],
             ["Commit evaluado", REPO + "/commit/" + COMMIT]], [4.0, 13.4]),
      Spacer(1, 10),
      Paragraph("Lista de cotejo", H2),
      tabla([["N.º", "Criterio", "Cumple"],
             ["1", "Portada con nombre, código y grupo", "Sí"],
             ["2", "Mapa del flujo: los diez pasos con captura y estado del sistema", "Sí"],
             ["3", "Capturas de la pantalla del emulador, con pie explicativo", "Sí"],
             ["4", "Recorrido en Android de principio a fin", "Sí"],
             ["5", "iOS: motivo explicado (no compila en este equipo)", "Sí"],
             ["6", "Bitácora con los tres estados del permiso usado", "Sí"],
             ["7", "Camino a Ajustes para el estado denegado de forma definitiva", "Sí"],
             ["8", "Ficha con gravedad, decisión y fecha", "Sí"],
             ["9", "Cada hueco descartado con su justificación", "Sí"],
             ["10", "Enlace a la rama y al commit evaluado", "Sí"],
             ["11", "Nombre del archivo y peso menor de 20 MB", "Sí"]], [1.0, 14.4, 2.0])]

os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
doc = Doc(SALIDA, pagesize=letter, leftMargin=2.0 * cm, rightMargin=2.0 * cm, topMargin=1.8 * cm, bottomMargin=1.7 * cm,
          title="Actividad Autónoma 13 - Rodríguez Bazán", author="Julio Fabián Rodríguez Bazán")
doc.multiBuild(E)
print(SALIDA, round(os.path.getsize(SALIDA) / 1e6, 2), "MB")
