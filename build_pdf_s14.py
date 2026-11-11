"""Genera docs/entrega/RodriguezBazan_Julio_Sesion14_Informe.pdf (guia practica 14 + actividad autonoma 14)."""
import os

from PIL import Image as PILImage
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (BaseDocTemplate, Frame, Image, PageBreak, PageTemplate, Paragraph, Preformatted,
                                Spacer, Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

RAIZ = os.path.dirname(os.path.abspath(__file__))
CAP = os.path.join(RAIZ, "docs", "s14", "capturas")
TMP = os.path.join(os.environ.get("TEMP", RAIZ), "s14_jpg")
SALIDA = os.path.join(RAIZ, "docs", "entrega", "RodriguezBazan_Julio_Sesion14_Informe.pdf")
COMMIT = "fb255614f52ebec4764aedaadcb882bbe17ff353"
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
H2 = ParagraphStyle("H2", parent=N, fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=NAVY, spaceBefore=6,
                    spaceAfter=3)
HS = ParagraphStyle("HS", parent=C, textColor=colors.white, fontName="Helvetica-Bold")
COD = ParagraphStyle("COD", fontName="Courier", fontSize=7.6, leading=9.4, backColor=colors.HexColor("#F3F3F3"),
                     borderPadding=4, spaceAfter=8)


def jpg(ruta):
    destino = os.path.join(TMP, os.path.basename(ruta).replace(".png", ".jpg"))
    im = PILImage.open(ruta).convert("RGB")
    im.thumbnail((640, 1400))
    im.save(destino, quality=82)
    return destino, im.size


def captura(nombre, ancho, pie):
    destino, (w, h) = jpg(os.path.join(CAP, nombre))
    img = Image(destino, width=ancho * cm, height=ancho * cm * h / w)
    t = Table([[img], [Paragraph(pie, PIE)]], colWidths=[ancho * cm])
    t.setStyle(TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("BOX", (0, 0), (0, 0), 0.4, colors.HexColor("#9DC3E6")),
                           ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                           ("TOPPADDING", (0, 1), (0, 1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 0)]))
    return t


def fila(items, ancho):
    t = Table([[captura(n, ancho, p) for n, p in items]], colWidths=[(ancho + 0.3) * cm] * len(items))
    t.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0)]))
    return t


def tabla(filas, anchos):
    datos = [[Paragraph(str(c), HS if i == 0 else C) for c in f] for i, f in enumerate(filas)]
    t = Table(datos, colWidths=[a * cm for a in anchos], repeatRows=1)
    est = [("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#9DC3E6")), ("VALIGN", (0, 0), (-1, -1), "TOP"),
           ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
           ("BACKGROUND", (0, 0), (-1, 0), NAVY)]
    est += [("BACKGROUND", (0, i), (-1, i), ALT) for i in range(2, len(datos), 2)]
    t.setStyle(TableStyle(est))
    return t


class Doc(BaseDocTemplate):
    def __init__(self, *a, **k):
        super().__init__(*a, **k)
        self.addPageTemplates([PageTemplate(id="p", frames=[Frame(self.leftMargin, self.bottomMargin, self.width,
                                                                  self.height, id="f")], onPage=self.pie)])

    def pie(self, canvas, doc):
        if doc.page > 1:
            canvas.saveState()
            canvas.setFont("Helvetica", 7.5)
            canvas.setFillColor(colors.grey)
            canvas.drawCentredString(letter[0] / 2, 1.0 * cm,
                                     "Guía Práctica y Actividad Autónoma N.º 14 · Rodríguez Bazán, Julio Fabián · Página %d" % doc.page)
            canvas.restoreState()

    def afterFlowable(self, f):
        if isinstance(f, Paragraph) and f.style.name in ("H1", "H2"):
            self.notify("TOCEntry", (0 if f.style.name == "H1" else 1, f.getPlainText(), self.page))


E = []
# ------------------------------------------------------------ Portada
E += [Spacer(1, 2.4 * cm),
      Paragraph("Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas",
                ParagraphStyle("a", parent=N, alignment=TA_CENTER, textColor=NAVY)),
      Spacer(1, 0.8 * cm),
      Paragraph("DESARROLLO DE APLICACIONES MÓVILES",
                ParagraphStyle("b", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=19, leading=24,
                               textColor=NAVY)),
      Paragraph("Informe de la Sesión 14",
                ParagraphStyle("c", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=24, leading=30,
                               textColor=NAVY)),
      Paragraph("Builds firmados, pruebas en dispositivos, guía de instalación y checklist de entrega",
                ParagraphStyle("d", parent=N, alignment=TA_CENTER, fontSize=12.5, leading=17)),
      Spacer(1, 1.2 * cm),
      tabla([["Dato", "Detalle"],
             ["Estudiante", "Julio Fabián Rodríguez Bazán"],
             ["Código / Grupo", "202410354 · Grupo 1"],
             ["Docente", "Benjamin David Reyna Barreto"],
             ["Ciclo / Semestre", "VI · 2026-2"],
             ["Unidad / Sesión", "Unidad 3 · Sesión 14"],
             ["Proyecto", "PharmaMobil (KMP) sobre PharmaSoft"],
             ["Repositorio", REPO],
             ["Rama / etiqueta", "feature/builds-14 · v1.0-final"],
             ["Commit del build", COMMIT[:7]],
             ["Entrega de builds", "Lunes 16 de noviembre de 2026"]], [4.6, 12.8]),
      PageBreak()]

toc = TableOfContents()
toc.levelStyles = [ParagraphStyle("t0", fontName="Helvetica-Bold", fontSize=10.5, leading=15, leftIndent=0),
                   ParagraphStyle("t1", fontName="Helvetica", fontSize=9.3, leading=12.5, leftIndent=16)]
E += [Paragraph("Índice", ParagraphStyle("ti", parent=H1, fontSize=17)), toc, Spacer(1, 10),
      Paragraph("Resumen", H2),
      Paragraph("Se firmó la aplicación con un certificado propio, se generó el paquete de aplicación (.aab) y el instalable de "
                "revisión (.apk) con reducción de código (R8) activada, se verificó la firma y se recorrió la versión de entrega "
                "en un teléfono real y en dos emuladores. Quedan declaradas tres limitaciones: iOS no se pudo firmar (sin Mac), "
                "la matriz tiene un solo teléfono real y la copia del almacén de claves fuera del equipo está pendiente.", N),
      PageBreak()]

# ------------------------------------------------------------ 1 Ficha
E += [Paragraph("1. Ficha de integración cerrada y alcance congelado", H1),
      Paragraph("A partir del commit de este paso no entra funcionalidad nueva, ni cambios de arquitectura, librerías, "
                "rediseños ni migraciones de esquema.", N),
      tabla([["N.º", "Hueco (ficha de la sesión 13)", "Clasificación", "Motivo"],
             ["1-5", "Listado vacío, fila pendiente indistinguible, cola sin límite, sin permisos, sin telemetría", "Cerrado (sesión 13)", "Estado vacío, distintivo, intentos y espera, GestorPermisos, Telemetria"],
             ["6", "Token vencido pierde el formulario", "Limitación", "Exige guardar borradores por formulario (función nueva)"],
             ["7", "Cierre de sesión deja datos locales y el usuario escrito", "Limitación", "Borrar la base exige avisar si hay pendientes; no se puede probar a fondo hoy"],
             ["8", "Reintento tras tiempo agotado puede duplicar", "Limitación", "Requiere clave de operación en PharmaSoft"],
             ["9", "El 400/409 no llega al campo; el formulario dice «registrado correctamente»", "Limitación", "Rediseño del flujo offline del formulario"],
             ["10", "La cola local no pertenece a un usuario (403 al cambiar de sesión)", "Limitación", "Requiere columna de propietario: migración de esquema, prohibida al congelar"],
             ["11", "iOS: GestorPermisosIos mínimo y sin ejecución", "Limitación", "Requiere un Mac con Xcode"],
             ["12", "Nombre de producto dado de baja bloquea uno nuevo (409)", "Descartado", "Regla del backend PharmaSoft"],
             ["13", "Módulo Pedidos «en construcción»", "Descartado (trabajo futuro)", "Nunca estuvo en el alcance comprometido"]],
            [1.1, 6.6, 3.0, 6.7]),
      Spacer(1, 4),
      Paragraph("El identificador de la aplicación se mantiene como <b>pe.edu.upeu.pharmamobil</b>: la guía indica el patrón "
                "pe.upeu.&lt;proyecto&gt;, pero cambiarlo invalidaría el google-services.json registrado en Firebase.", N)]

# ------------------------------------------------------------ 2 Firma
E += [Paragraph("2. Firma de entrega y generación de los artefactos", H1),
      Paragraph("Almacén de claves y firma sin exponer secretos", H2),
      Paragraph("El almacén <b>pharmamobil.jks</b> se creó una sola vez, fuera del repositorio, con keytool (RSA 2048, 10 000 días, "
                "alias pharmamobil). Las credenciales viven en keystore.properties, ignorado por Git junto con *.jks y *.keystore. "
                "El archivo de compilación lee las propiedades solo si existen, de modo que quien clone el proyecto puede compilar "
                "en depuración sin el archivo; solo la versión de entrega exige las credenciales.", N),
      Preformatted("keytool -genkeypair -v -keystore pharmamobil.jks -storetype JKS -keyalg RSA -keysize 2048 \\\n"
                   "        -validity 10000 -alias pharmamobil          # contraseñas: solo en keystore.properties\n"
                   "./gradlew :androidApp:bundleRelease     # -> .aab firmado (entregable)\n"
                   "./gradlew :androidApp:assembleRelease   # -> .apk de revisión\n"
                   "apksigner verify --print-certs Equipo1_ProyectoFinal_Android_revision.apk", COD),
      Paragraph("Verificación de la firma", H2),
      Image(os.path.join(CAP, "firma_verificacion.png"), width=16.8 * cm, height=16.8 * cm * PILImage.open(os.path.join(CAP, "firma_verificacion.png")).size[1] / PILImage.open(os.path.join(CAP, "firma_verificacion.png")).size[0]),
      Paragraph("apksigner: titular del certificado y huellas SHA-256, SHA-1 y MD5 (la contraseña no se transcribe).", PIE),
      Spacer(1, 6),
      Paragraph("Ficha de versión y firma", H2),
      tabla([["Dato", "Valor"],
             ["versionCode / versionName", "1 / 1.0.0"],
             ["applicationId", "pe.edu.upeu.pharmamobil"],
             ["Etiqueta de Git", "v1.0-final (commit " + COMMIT[:7] + ")"],
             ["Paquete firmado", "Equipo1_ProyectoFinal_Android.aab (8,3 MB)"],
             ["Instalable de revisión", "Equipo1_ProyectoFinal_Android_revision.apk (4,2 MB)"],
             ["Titular", "CN=Julio Fabian Rodriguez Bazan, OU=EP Ingenieria de Sistemas, O=Universidad Peruana Union, L=Lima, C=PE"],
             ["SHA-256 del certificado", "2e11e5284ea53623960822fe6e50e7e5340cd29329dd2b523e48163bc53f109f"],
             ["Validez", "10 000 días desde el 11/11/2026 (aprox. 27 años)"],
             ["Reducción de código", "R8 activada; reglas en androidApp/proguard-rules.pro (serialización y Koin)"]],
            [4.6, 12.8]),
      Spacer(1, 4),
      Paragraph("<b>Minificación:</b> se dejó activada. Al recorrer la versión de entrega la lista se carga (serialización) y las "
                "pantallas con inyección abren sin cierres, así que no hizo falta desactivarla. Se añadieron reglas mínimas para "
                "kotlinx.serialization y Koin; no se añadió ninguna a ciegas.", N),
      PageBreak()]

# ------------------------------------------------------------ 3 iOS
E += [Paragraph("3. iOS · límite documentado", H1),
      tabla([["Punto", "Detalle"],
             ["Estado", "No se generó el archivo de iOS ni el .ipa."],
             ["Qué lo impide", "El equipo de desarrollo es Windows 11. La firma, Product · Archive y el simulador requieren macOS y Xcode; "
                               "no hay un Mac disponible."],
             ["Evidencia", "./gradlew :shared:compileKotlinIosSimulatorArm64 termina con BUILD SUCCESSFUL: el módulo compartido y "
                           "iosMain (AlmacenSeguroIos, CompartidorIos, GestorPermisosIos, Firebase) compilan a klib. Lo que no se "
                           "puede hacer aquí es enlazar el framework y ejecutar la app."],
             ["Qué haría falta", "Mac con Xcode; equipo de firma e identificador del proyecto en Signing & Capabilities; destino "
                                 "«Any iOS Device»; Product · Archive; distribuir (cuenta gratuita: perfil de 7 días, sin .ipa ad hoc); "
                                 "añadir GoogleService-Info.plist y los pods de Firebase; completar GestorPermisosIos."],
             ["Plan", "Compilar y archivar en un Mac del laboratorio antes de la sustentación (17/11/2026)."]], [3.0, 14.4]),
      Spacer(1, 6)]

# ------------------------------------------------------------ 4 Matriz
E += [Paragraph("4. Matriz de pruebas en dispositivos", H1),
      Paragraph("Se instaló el <b>.apk firmado y minificado</b> desde el archivo (adb install), sin Android Studio conectado. "
                "<b>Limitación:</b> solo hubo un teléfono real; el segundo dispositivo real que exige la guía no estuvo disponible. "
                "Se completó la matriz con dos emuladores. Los fallos de batería y de señal intermitente no se reproducen en emulador.", N),
      tabla([["Dispositivo", "Tipo", "Sistema", "Cómo se conectó"],
             ["A · Samsung Galaxy A12 (SM-A127M)", "<b>Real</b>", "Android 13 (API 33), 720x1600", "Depuración inalámbrica + adb reverse al backend"],
             ["B · Pixel 8", "Emulador", "Android 17 (API 37.1)", "adb"],
             ["C · Android SDK built for x86", "Emulador (gama baja)", "Android 7.0 (API 24)", "adb"]],
            [5.4, 3.3, 4.2, 4.5]),
      Spacer(1, 6),
      tabla([["N.º", "Paso del recorrido", "A real", "B emu.", "C emu.", "Observación"],
             ["1", "Instalación desde el archivo firmado", "OK", "OK", "OK", "Nombre e icono definitivos"],
             ["2", "Login correcto e incorrecto", "OK", "OK", "OK", "«Usuario o contraseña incorrectos» distingue credencial de falta de conexión"],
             ["3", "CRUD con rechazo de validación", "OK", "OK", "OK (crear)", "Lista cargada con R8; 400 «nombre entre 3 y 150» (ver D-01)"],
             ["4", "Operación sin privilegio", "—", "OK", "—", "vendedor: 403 «Sin permiso para esta operación», sesión abierta (D-02)"],
             ["5", "Modo avión: abrir, leer y crear", "—", "OK", "OK", "«Sin conexión · 1 cambio por enviar»; fila pendiente"],
             ["6", "Recuperar conexión y sincronizar", "—", "OK", "OK", "«Todo sincronizado», sin duplicados"],
             ["7", "Permiso: conceder, denegar, denegar para siempre", "—", "OK", "n/a", "API 24 no tiene POST_NOTIFICATIONS; los tres estados se vieron en B"],
             ["8", "Cerrar sesión y reabrir", "—", "OK", "OK", "Vuelve al login, token borrado"]],
            [0.8, 4.4, 1.4, 1.4, 1.7, 7.7]),
      Paragraph("«—» = no ejecutado en ese dispositivo. En el teléfono real el recorrido se cortó tras el paso 3 porque la "
                "conexión inalámbrica se perdió; los pasos 4 a 8 quedan cubiertos por los emuladores y deben repetirse en un teléfono.", N),
      Spacer(1, 4),
      fila([("C_03_catalogo.png", "Teléfono real (A): catálogo con 8 productos"),
            ("C_05_validacion_rechazada.png", "Teléfono real (A): rechazo 400 en el campo nombre"),
            ("A_06_modo_avion_pendiente.png", "Pixel 8 (B): modo avión, 1 cambio por enviar"),
            ("B_07_sync_recuperada.png", "API 24 (C): tras reconectar, todo sincronizado")], 3.95),
      Spacer(1, 6),
      Paragraph("Registro de defectos", H2),
      tabla([["ID", "Disp.", "Pasos para reproducir", "Esperado", "Obtenido", "Sev.", "Estado"],
             ["D-01", "A, B", "1. Login admin. 2. Inventario: nombre «AB», precio 2, stock 10. 3. Registrar", "El error del servicio llega al campo nombre", "El formulario dice «registrado correctamente»; el motivo aparece en Catálogo, Revisar", "Media", "Pendiente (limitación 9)"],
             ["D-02", "B", "1. Sin red, admin crea un producto. 2. Recuperar red, cerrar sesión. 3. Entrar como vendedor. 4. Abrir Catálogo", "La cola no se envía con otro usuario", "Se envía con el usuario nuevo y el servicio responde 403", "Alta", "Pendiente (limitación 10)"],
             ["D-03", "A, B, C", "Cerrar sesión como admin y volver a abrir", "Usuario vacío y base limpia", "El usuario queda escrito y la base conserva los datos", "Media", "Pendiente (limitación 7)"],
             ["D-04", "—", "Probar en un segundo teléfono real", "Matriz con dos teléfonos", "Solo un teléfono real disponible", "Media", "Pendiente: repetir con teléfonos reales"],
             ["D-05", "A", "Recorrer los pasos 4 a 8 en el teléfono real", "Recorrido completo", "La conexión inalámbrica se perdió tras el paso 3", "Baja", "Pendiente: repetir"]],
            [1.0, 1.1, 4.7, 2.9, 3.9, 1.2, 2.6]),
      PageBreak()]

# ------------------------------------------------------------ 5 Guia de instalacion
E += [Paragraph("5. Guía de instalación", H1),
      Paragraph("Escrita para alguien que no conoce el proyecto. <b>No fue probada por un compañero de otro equipo</b>: la prueba "
                "cruzada que pide la actividad queda pendiente.", N),
      Paragraph("A. Requisitos", H2),
      tabla([["Elemento", "Detalle"],
             ["Teléfono", "Android 7.0 o superior (minSdk 24)"],
             ["Servicio", "PharmaSoft en ejecución con Oracle (contenedor gvenzl/oracle-free, puerto 1522) y accesible desde el teléfono"],
             ["PC", "Android platform-tools (adb); JDK 21 para compilar"],
             ["Archivo", "Equipo1_ProyectoFinal_Android_revision.apk (carpeta entregable/s14 del repositorio)"]], [3.0, 14.4]),
      Paragraph("B. Instalar el instalable de revisión", H2),
      Paragraph("1. En el teléfono: Ajustes · Opciones de desarrollador · Depuración inalámbrica (o USB). 2. Conectar con "
                "<i>adb pair</i> y <i>adb connect</i> (o cable). 3. Instalar:", N),
      Preformatted("adb install -r Equipo1_ProyectoFinal_Android_revision.apk\n"
                   "adb reverse tcp:8080 tcp:8080     # el teléfono alcanza el backend de la PC en localhost:8080", COD),
      Paragraph("Si el archivo se copia al teléfono, hay que permitir «instalar apps desconocidas» para el explorador de archivos. "
                "El .aab no se instala: es el formato de la tienda.", N),
      Paragraph("C. Levantar el servicio", H2),
      Preformatted("docker start pharma-oracle          # esperar «DATABASE IS READY»\n"
                   "cd pharmaSoft-master && ./mvnw spring-boot:run     # escucha en el puerto 8080", COD),
      Paragraph("En un emulador no hace falta adb reverse: la app usa 10.0.2.2. Usuarios de desarrollo: admin, vendedor y almacen "
                "(contraseñas en la migración V3 del backend).", N),
      Paragraph("D. Compilar desde el código", H2),
      Paragraph("Clonar la rama feature/builds-14. La depuración compila sin más: ./gradlew :androidApp:assembleDebug. Para la versión de "
                "entrega se necesita keystore.properties (ver README) con la ruta y las claves del almacén del equipo.", N),
      Paragraph("E. Problemas frecuentes", H2),
      tabla([["Síntoma", "Causa", "Solución"],
             ["La lista no carga y dice «Sin conexión»", "El teléfono no alcanza el backend", "adb reverse tcp:8080 tcp:8080 y comprobar que PharmaSoft corre"],
             ["«La aplicación no se instaló» / conflicto de firma", "Hay una versión firmada con otra clave", "Desinstalar la anterior (adb uninstall pe.edu.upeu.pharmamobil)"],
             ["Los avisos no aparecen", "Android 13+ exige permiso de notificaciones", "Activar avisos en la tarjeta del Catálogo o en Ajustes"],
             ["El puerto de depuración inalámbrica cambia", "Android lo rota al reconectar", "Leer el puerto actual en Depuración inalámbrica y repetir adb connect"]],
            [5.0, 5.0, 7.4]),
      PageBreak()]

# ------------------------------------------------------------ 6 Checklist
E += [Paragraph("6. Checklist de entrega", H1),
      Paragraph("Lista con la que se revisa, punto por punto, antes de subir los entregables del Proyecto Final.", N),
      tabla([["N.º", "Punto de revisión", "Estado"],
             ["1", "Rama feature/builds-14 con un commit por paso y sin cambios sin subir", "Sí"],
             ["2", "Etiqueta v1.0-final sobre el commit compilado", "Sí"],
             ["3", "versionCode 1, versionName 1.0.0 y applicationId declarados y coinciden con el archivo", "Sí"],
             ["4", ".aab firmado con la nomenclatura Equipo1_ProyectoFinal_Android.aab", "Sí"],
             ["5", ".apk de revisión firmado", "Sí"],
             ["6", "Firma verificada con apksigner (titular y huella)", "Sí"],
             ["7", "Ni almacén ni contraseñas en el repositorio; .gitignore los excluye", "Sí"],
             ["8", "El proyecto compila en depuración sin keystore.properties", "Sí"],
             ["9", "La versión de entrega se recorrió completa (serialización e inyección OK)", "Sí"],
             ["10", "Matriz de pruebas con defectos y estados", "Sí (un teléfono real)"],
             ["11", "Segundo teléfono real", "<b>Pendiente</b>"],
             ["12", "Copia del almacén de claves fuera del equipo de un solo integrante", "<b>Pendiente</b>"],
             ["13", "iOS: archivo o límite documentado", "Límite documentado"],
             ["14", "README con keystore.properties documentado, sin contraseñas", "Sí"],
             ["15", "Guía de instalación probada por un compañero de otro equipo", "<b>Pendiente</b>"],
             ["16", "Este informe en PDF con el enlace al repositorio", "Sí"]], [1.0, 13.0, 3.4]),
      Spacer(1, 8),
      Paragraph("7. Repositorio y commit evaluado", H1),
      tabla([["Elemento", "Enlace"],
             ["Repositorio", REPO],
             ["Rama", REPO + "/tree/feature/builds-14"],
             ["Etiqueta", REPO + "/releases/tag/v1.0-final"],
             ["Commit del build", REPO + "/commit/" + COMMIT]], [3.4, 14.0]),
      Spacer(1, 8),
      Paragraph("Lista de cotejo de la guía práctica", H2),
      tabla([["N.º", "Criterio", "Cumple"],
             ["1", "Ficha de integración cerrada y alcance congelado", "Sí"],
             ["2", "Almacén de claves existe y hay copia fuera del equipo", "Parcial (falta la copia)"],
             ["3", "Ni el almacén ni sus contraseñas están en el repositorio", "Sí"],
             ["4", "El proyecto compila sin el archivo de propiedades", "Sí"],
             ["5", "Paquete de aplicación firmado generado", "Sí"],
             ["6", "Instalable generado e instalado en un dispositivo real", "Sí"],
             ["7", "Verificación de la firma con titular y huella", "Sí"],
             ["8", "Versión de entrega recorrida; serialización e inyección OK", "Sí"],
             ["9", "Minificación desactivada: decisión escrita", "No aplica (activada)"],
             ["10", "iOS: firma y archivo, o límite documentado", "Límite documentado"],
             ["11", "Al menos dos dispositivos reales distintos", "No (uno real y dos emuladores)"],
             ["12", "Cada defecto con id, pasos, severidad y estado", "Sí"],
             ["13", "versionCode, versionName e identificador coinciden", "Sí"],
             ["14", "Commits con convención y distribuidos", "Sí"]], [1.0, 11.4, 5.0])]

os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
Doc(SALIDA, pagesize=letter, leftMargin=2.0 * cm, rightMargin=2.0 * cm, topMargin=1.8 * cm, bottomMargin=1.7 * cm,
    title="Informe Sesión 14 - Rodríguez Bazán", author="Julio Fabián Rodríguez Bazán").multiBuild(E)
print(SALIDA, round(os.path.getsize(SALIDA) / 1e6, 2), "MB")
