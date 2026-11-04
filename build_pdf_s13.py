"""Genera docs/entrega/S13_GuiaPractica_RodriguezBazan.pdf (guia practica 13 resuelta)."""
import os

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (Image, PageBreak, Paragraph, Preformatted,
                                SimpleDocTemplate, Spacer, Table, TableStyle)

RAIZ = os.path.dirname(os.path.abspath(__file__))
CAP = os.path.join(RAIZ, "docs", "s13", "capturas")
SALIDA = os.path.join(RAIZ, "docs", "entrega", "S13_GuiaPractica_RodriguezBazan.pdf")
NAVY = colors.HexColor("#1F3864")
ALT = colors.HexColor("#EAF3FB")

ss = getSampleStyleSheet()
N = ParagraphStyle("N", parent=ss["Normal"], fontName="Helvetica", fontSize=10, leading=14, spaceAfter=5)
C = ParagraphStyle("C", parent=N, fontSize=8.5, leading=11, spaceAfter=0)
H1 = ParagraphStyle("H1", parent=N, fontName="Helvetica-Bold", fontSize=15, textColor=NAVY, spaceBefore=10, spaceAfter=8)
H2 = ParagraphStyle("H2", parent=N, fontName="Helvetica-Bold", fontSize=11.5, textColor=NAVY, spaceBefore=8, spaceAfter=4)
PIE = ParagraphStyle("PIE", parent=N, fontSize=8, leading=10, alignment=TA_CENTER, textColor=colors.grey)
COD = ParagraphStyle("COD", fontName="Courier", fontSize=7.8, leading=9.6, backColor=colors.HexColor("#F3F3F3"),
                     borderPadding=4, spaceAfter=8)


def p(t, st=N):
    return Paragraph(t, st)


def tabla(filas, anchos, cab=True):
    HS = ParagraphStyle("hs", parent=C, textColor=colors.white, fontName="Helvetica-Bold")
    datos = [[p(str(c), HS if (cab and i == 0) else C) for c in f] for i, f in enumerate(filas)]
    t = Table(datos, colWidths=[a * cm for a in anchos], repeatRows=1 if cab else 0)
    est = [("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#9DC3E6")),
           ("VALIGN", (0, 0), (-1, -1), "TOP"),
           ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3)]
    if cab:
        est += [("BACKGROUND", (0, 0), (-1, 0), NAVY)]
        for i in range(1, len(datos)):
            if i % 2 == 0:
                est.append(("BACKGROUND", (0, i), (-1, i), ALT))
    t.setStyle(TableStyle(est))
    return t


def captura(nombre, ancho=4.6):
    ruta = os.path.join(CAP, nombre)
    alto = ancho * 2400 / 1080
    return Image(ruta, width=ancho * cm, height=alto * cm)


def fila_capturas(items, ancho=4.4):
    celdas = [[captura(n, ancho) for n, _ in items], [p(t, C) for _, t in items]]
    t = Table(celdas, colWidths=[(17.0 / len(items)) * cm] * len(items))
    t.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("ALIGN", (0, 0), (-1, 0), "CENTER")]))
    return t


def pie(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.grey)
    canvas.drawCentredString(A4[0] / 2, 1.0 * cm, "Guía Práctica N.º 13 · Desarrollo de Aplicaciones Móviles · Página %d" % doc.page)
    canvas.restoreState()


E = []

# ---------------- Portada ----------------
E += [Spacer(1, 3 * cm),
      p("Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas", ParagraphStyle("a", parent=N, alignment=TA_CENTER, textColor=NAVY)),
      Spacer(1, 1 * cm),
      p("DESARROLLO DE APLICACIONES MÓVILES", ParagraphStyle("b", parent=H1, alignment=TA_CENTER, fontSize=20, leading=26)),
      p("Guía Práctica N.º 13", ParagraphStyle("c", parent=H1, alignment=TA_CENTER, fontSize=24, leading=30)),
      p("Proyecto integrador: refinamiento offline-first, Firebase y permisos nativos",
        ParagraphStyle("d", parent=N, alignment=TA_CENTER, fontSize=13, leading=17)),
      Spacer(1, 1.5 * cm),
      tabla([["Dato", "Detalle"],
             ["Estudiante", "Julio Fabián Rodríguez Bazán"],
             ["Docente", "Benjamin David Reyna Barreto"],
             ["Ciclo / Semestre", "VI · 2026-2"],
             ["Unidad / Sesión", "Unidad 3 · Sesión 13"],
             ["Proyecto", "PharmaMobil (KMP) sobre PharmaSoft"],
             ["Repositorio", "github.com/fabianrodriguez33/Pharma-Mobil"],
             ["Rama", "feature/integrador-13"],
             ["Fecha", "4 de noviembre de 2026"]], [4.5, 12.5]),
      PageBreak()]

# ---------------- Resumen ----------------
E += [p("1. Resumen de lo realizado", H1),
      p("Se convirtió la aplicación PharmaMobil en un producto entregable: la cola de sincronización tiene reintentos "
        "acotados con espera creciente, el estado de la sincronización se ve en la pantalla principal, el permiso de "
        "notificaciones se resuelve desde el código común con sus tres estados y Firebase se integra detrás de una "
        "interfaz del dominio. Se trabajó en la rama <b>feature/integrador-13</b> con un commit por paso."),
      tabla([["Paso", "Qué se hizo", "Commit"],
             ["1", "Recorrido del flujo y tabla de huecos con decisión", "docs(s13): tabla de huecos del recorrido completo..."],
             ["2", "Columnas intentos y ultimoError, migración 2.sqm y consultas de la cola", "feat(s13): columnas intentos y ultimoError con migracion..."],
             ["3", "SincronizarUseCase con ResultadoEnvio: transitorio o permanente, espera 1-16 s, máximo 5", "feat(s13): sincronizacion con espera creciente..."],
             ["4", "Pendientes, fallidas y antigüedad en el Catálogo; distintivo por fila", "feat(s13): mostrar pendientes, fallidas y antiguedad..."],
             ["5", "GestorPermisos común, implementación Android y tarjeta de notificaciones", "feat(s13): gestor de permisos en comun..."],
             ["6", "Telemetria (dominio) + TelemetriaFirebase (datos) y evento de sincronización", "feat(s13): telemetria con Firebase detras de la interfaz..."],
             ["7", "Pruebas de commonTest: sincronización, permisos y textos", "test(s13): pruebas de sincronizacion, permisos..."]],
            [1.2, 9.3, 6.5])]

# ---------------- Paso 1 ----------------
E += [p("2. Paso 1 · Recorrido del flujo y huecos", H1),
      p("Recorrido: instalación desde cero, inicio de sesión, creación de un registro, pérdida de conexión, "
        "sincronización, error de validación y cierre de sesión."),
      tabla([["N.º", "Momento", "Qué se observó", "Grav.", "Decisión"],
             ["1", "Primera apertura, sin sesión", "Solo se ve el login; tiene salida.", "Baja", "Sin hueco"],
             ["2", "Credenciales incorrectas", "El mensaje distingue credencial inválida de fallo de red.", "Baja", "Sin hueco"],
             ["3", "Listado con base vacía", "Estado vacío sin acción y con texto que parece error.", "Media", "Se cierra hoy: texto propio y botón Sincronizar ahora"],
             ["4", "Creación en modo avión", "Se guarda pendiente pero el catálogo no la distingue.", "Alta", "Se cierra hoy: distintivo por fila y contador"],
             ["5", "Sincronización al recuperar señal", "Un pendiente que falla se reintentaba sin límite ni memoria.", "Alta", "Se cierra hoy: intentos, espera creciente, fila fallida visible"],
             ["6", "Error de validación del servidor", "Con offline-first el formulario dice «registrado correctamente» y el mensaje del servidor (400/409) aparece después, en el diálogo de cambios fallidos, no en el campo.", "Media", "Sesión 14: validar contra las reglas del servidor o mostrar el motivo en el formulario"],
             ["7", "Token vencido en un formulario", "Se vuelve al login y se pierde lo escrito.", "Media", "Sesión 14: guardar borrador"],
             ["8", "Cierre de sesión", "El token se borra, pero la base local conserva datos del usuario anterior.", "Media", "Sesión 14: avisar si hay pendientes antes de limpiar"],
             ["9", "Permisos del sistema", "No se resolvía ningún permiso.", "Alta", "Se cierra hoy: GestorPermisos"],
             ["10", "Observabilidad", "Un fallo en el teléfono no deja rastro.", "Media", "Se cierra hoy: Telemetria con Firebase"],
             ["11", "Creación reintentada tras tiempo agotado", "Si el servidor guardó pero la respuesta se perdió, el reintento duplica.", "Alta", "Sesión 14: requiere clave de operación en PharmaSoft"]],
            [0.8, 3.2, 5.6, 1.2, 6.2]),
      Spacer(1, 4),
      p("<b>Prioridad de hoy:</b> (1) cola de pendientes acotada y visible; (2) permiso de notificaciones con sus tres "
        "estados; (3) estado vacío con acción y telemetría.")]

# ---------------- Paso 2 ----------------
E += [p("3. Paso 2 · Base local con el estado de envío", H1),
      p("La tabla de la guía se llama <i>labor</i>; en este proyecto es <i>producto</i>. La migración es <b>2.sqm</b> porque "
        "1.sqm ya existía (tabla de sincronización). El esquema final queda en Producto.sq."),
      Preformatted(
          "-- 2.sqm\n"
          "ALTER TABLE producto ADD COLUMN intentos INTEGER NOT NULL DEFAULT 0;\n"
          "ALTER TABLE producto ADD COLUMN ultimoError TEXT;\n\n"
          "-- Producto.sq (consultas nuevas)\n"
          "pendientesPorEnviar: SELECT * FROM producto WHERE pendiente = 1\n"
          "                     AND intentos < :maxIntentos ORDER BY id DESC;\n"
          "contarPendientes:    SELECT COUNT(*) ... pendiente = 1 AND intentos <  :maxIntentos;\n"
          "contarFallidas:      SELECT COUNT(*) ... pendiente = 1 AND intentos >= :maxIntentos;\n"
          "sumarIntento:        UPDATE producto SET intentos = intentos + 1, ultimoError = ? WHERE id = ?;\n"
          "marcarFallida:       UPDATE producto SET intentos = :maxIntentos, ultimoError = :motivo WHERE id = :id;\n"
          "reintentarFallidas:  UPDATE producto SET intentos = 0, ultimoError = NULL WHERE pendiente = 1\n"
          "                     AND intentos >= :maxIntentos;", COD)]

# ---------------- Paso 3 ----------------
E += [p("4. Paso 3 · Sincronización refinada", H1),
      p("La traducción de errores a <b>ResultadoEnvio</b> ocurre en la capa de datos (EnvioRepositorioRest). El caso de uso "
        "queda sin ninguna referencia a códigos HTTP."),
      tabla([["Error de la API", "Resultado", "Reintento"],
             ["SinConexion, TiempoAgotado, Servidor (503)", "Transitorio", "Sí, con espera creciente"],
             ["Validacion (400), NoEncontrado (404), Conflicto (409), SinPermiso (403), NoAutenticado (401)", "Permanente", "No: la fila se marca y se muestra"]],
            [9.0, 3.2, 4.8]),
      Spacer(1, 4),
      Preformatted(
          "class SincronizarUseCase(local, remoto, esperar = { delay(it) }) {\n"
          "  suspend operator fun invoke(conEspera: Boolean = true): ResumenSync {\n"
          "    local.pendientes(MAX_INTENTOS).forEach { fila ->\n"
          "      when (val r = remoto.enviar(fila.producto)) {\n"
          "        is Exito      -> local.confirmar(fila.id, r.idServidor)\n"
          "        is Permanente -> local.marcarFallida(fila.id, r.motivo, MAX_INTENTOS)\n"
          "        is Transitorio -> { local.sumarIntento(fila.id, r.motivo)\n"
          "                            if (conEspera) esperar(espera(fila.intentos)) }\n"
          "      }\n"
          "    }\n"
          "    // Solo despues de subir se baja: invertirlo borra lo pendiente.\n"
          "    local.conciliar(remoto.listar())\n"
          "    return ResumenSync(enviadas, fallidas, local.contarPendientes(MAX_INTENTOS))\n"
          "  }\n"
          "  fun espera(intentos: Int) = 1_000L shl intentos.coerceIn(0, 4)  // 1, 2, 4, 8, 16 s\n"
          "  const val MAX_INTENTOS = 5\n"
          "}", COD),
      p("Al guardar un producto sin red la sincronización inmediata se hace con <i>conEspera = false</i>, para no bloquear "
        "la pantalla.")]

# ---------------- Paso 4 ----------------
E += [p("5. Paso 4 · Estado de la sincronización en la pantalla principal", H1),
      p("El ViewModel arma <b>SincronizacionUiState</b> (enCurso, pendientes, fallidas, ultimaSync, sinConexion) combinando "
        "consultas observables de la base local; nunca consulta la red. El Catálogo muestra «Sin conexión · 1 cambio por "
        "enviar», «Actualizado hace 1 minuto», un distintivo «Pendiente de envío» por fila y, si hay filas fallidas, "
        "«1 cambio no se pudo enviar» con las acciones Revisar y Reintentar ahora."),
      fila_capturas([("09_catalogo_pendiente_sin_red.png", "Modo avión: 1 cambio por enviar y fila pendiente"),
                     ("10_tras_reconectar.png", "Tras reconectar y sincronizar: todo sincronizado")], 5.2)]

# ---------------- Paso 5 ----------------
E += [PageBreak(), p("6. Paso 5 · Gestor de permisos", H1),
      p("<b>GestorPermisos</b> se declara como interfaz en commonMain (estado, solicitar, abrirAjustes) con los estados "
        "Concedido, Denegado y DenegadoParaSiempre. La implementación de Android (GestorPermisosAndroid + PuentePermisos) "
        "recuerda si el permiso ya se pidió, para distinguir «primera vez» de «denegado definitivamente». Se registra con "
        "Koin en platformModule; por eso vive en shared/androidMain y no en androidApp. El manifiesto declara "
        "POST_NOTIFICATIONS, CAMERA y ACCESS_FINE_LOCATION; Info.plist incluye las claves de cámara y ubicación en español. "
        "El permiso real resuelto en la aplicación es el de <b>notificaciones</b>."),
      tabla([["Estado", "Qué ve el usuario", "Cómo se recupera la función"],
             ["Concedido", "«Avisos activados»", "Continúa normal"],
             ["Denegado", "«Avisos desactivados» + botón Activar avisos; antes de pedir, un diálogo explica para qué", "Se vuelve a pedir tras la explicación"],
             ["Denegado para siempre", "«Avisos bloqueados» + diálogo «Permiso bloqueado»", "Botón Abrir ajustes; al volver, el estado se refresca solo"]],
            [3.2, 7.0, 6.8]),
      Spacer(1, 6),
      fila_capturas([("02_catalogo.png", "Estado inicial: denegado (primera vez)"),
                     ("03_explicacion.png", "La app explica antes de pedir"),
                     ("04_dialogo_sistema.png", "Diálogo del sistema")], 4.6),
      Spacer(1, 6),
      fila_capturas([("06_denegado_para_siempre.png", "Segunda negativa: denegado para siempre"),
                     ("06b_bloqueados.png", "Tarjeta «Avisos bloqueados»"),
                     ("07_concedido.png", "Permiso concedido")], 4.6)]

# ---------------- Paso 6 ----------------
E += [PageBreak(), p("7. Paso 6 · Firebase detrás de una interfaz del dominio", H1),
      p("El dominio declara <b>Telemetria</b> (registrarError, registrarEvento). Solo la clase <b>TelemetriaFirebase</b> "
        "(data/firebase) importa el SDK de GitLive 2.7.0 (crashlytics, analytics y messaging); se registra en Koin con "
        "<i>single&lt;Telemetria&gt; { TelemetriaFirebase() }</i>. Se registra únicamente contexto técnico (pantalla, "
        "operación, tipo de error), nunca datos personales ni el token."),
      p("google-services.json se descarga de la consola de Firebase y no se versiona (.gitignore). El plugin de Google "
        "Services se aplica solo si el archivo existe, de modo que la aplicación compila y corre también sin él, con la "
        "telemetría inactiva. Evento registrado: <b>sincronizacion_completada</b> (parámetro pantalla = catalogo)."),
      Image(os.path.join(CAP, "12_firebase_debugview.png"), width=16.5 * cm, height=16.5 * cm * 877 / 1912),
      p("Consola de Firebase, proyecto pharmamobil: Analytics, DebugView.", PIE)]

# ---------------- Paso 7 ----------------
E += [p("8. Paso 7 · Pruebas de commonTest", H1),
      p("Se usan la base SQLite en memoria y el motor de red simulado (MockEngine); el gestor de permisos se prueba con una "
        "implementación falsa. Resultado de <i>./gradlew :shared:testAndroidHostTest</i>: <b>100 pruebas, 0 fallas</b> "
        "(20 nuevas)."),
      tabla([["Prueba", "Comprueba"],
             ["unFalloTransitorioSumaIntentoYNoMarcaFallida", "503: intentos = 1, sigue pendiente, no es fallida"],
             ["unFalloPermanenteMarcaLaFilaYNoLaReintenta", "409: fila fallida y un solo POST aunque se sincronice dos veces"],
             ["laFilaQueAgotaSusIntentosQuedaVisibleYDejaDeReintentarse", "Tras 5 intentos queda fallida y deja de enviarse"],
             ["laEsperaCreceConCadaIntentoYTieneTope", "1, 2, 4, 8, 16, 16 s"],
             ["laSubidaOcurreAntesQueLaBajadaYNadaSePierde", "Orden POST luego GET; el pendiente sobrevive a la bajada"],
             ["reintentarLasFallidasLasDevuelveALaColaYLasEnvia", "La acción del usuario devuelve la fila a la cola"],
             ["PermisoNotificacionesViewModelTest (6)", "Concedido, denegado con explicación, ahora no, denegado para siempre, Ajustes, refresco"],
             ["TextoEstadoSincronizacionTest (6)", "Mensajes «Sin conexión · 3 cambios por enviar», singular, fallidas"]],
            [8.2, 8.8])]

# ---------------- Cotejo ----------------
E += [p("9. Lista de cotejo", H1),
      tabla([["N.º", "Criterio observable", "Cumple"],
             ["1", "Recorrí el flujo completo y registré por escrito los huecos encontrados.", "Sí"],
             ["2", "La tabla local guarda los intentos y el último error de cada fila pendiente.", "Sí"],
             ["3", "La sincronización distingue el fallo transitorio del permanente.", "Sí"],
             ["4", "La espera entre intentos crece y existe un número máximo de intentos.", "Sí"],
             ["5", "Una fila que agota los intentos queda marcada y visible para el usuario.", "Sí"],
             ["6", "La subida ocurre antes de la bajada y nada se pierde al sincronizar.", "Sí"],
             ["7", "La pantalla principal muestra pendientes, fallidas y antigüedad de los datos.", "Sí"],
             ["8", "El gestor de permisos está declarado como interfaz en el código común.", "Sí"],
             ["9", "La implementación de Android distingue los tres estados del permiso.", "Sí"],
             ["10", "El permiso está declarado en el manifiesto y, si aplica, la clave está en Info.plist.", "Sí"],
             ["11", "La interfaz explica antes de pedir y ofrece ir a Ajustes cuando corresponde.", "Sí"],
             ["12", "El SDK de Firebase no aparece fuera de la capa de datos.", "Sí"],
             ["13", "Las pruebas de commonTest se ejecutan y pasan.", "Sí"],
             ["14", "Los commits siguen la convención y están distribuidos a lo largo de la sesión.", "Sí"]],
            [1.2, 13.8, 2.0]),
      Spacer(1, 8),
      p("<b>Notas.</b> (1) GestorPermisosIos es mínimo (solo abre Ajustes) y no se compiló en este equipo; los diálogos "
        "nativos de iOS quedan para la sesión 14 con un Mac. (2) La fila fallida se verificó con pruebas automáticas.")]

os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
SimpleDocTemplate(SALIDA, pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm, topMargin=2 * cm, bottomMargin=1.8 * cm,
                  title="Guía Práctica 13 - Rodríguez Bazán", author="Julio Fabián Rodríguez Bazán").build(
    E, onFirstPage=lambda c, d: None, onLaterPages=pie)
print(SALIDA)
