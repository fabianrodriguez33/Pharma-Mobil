"""Genera docs/entrega/RodriguezBazan_JulioFabian_Autonoma14.pdf (actividad autonoma 14).

Reutiliza estilos y utilidades de build_pdf_s14.py (todo lo anterior a la lista E)."""
import os

_base = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "build_pdf_s14.py"), encoding="utf-8").read()
_head = _base[:_base.index("E = []")]
_head = _head.replace("RodriguezBazan_Julio_Sesion14_Informe.pdf", "RodriguezBazan_JulioFabian_Autonoma14.pdf")
_head = _head.replace("Guía Práctica y Actividad Autónoma N.º 14", "Actividad Autónoma N.º 14")
_head = _head.replace("Informe Sesión 14", "Actividad Autónoma 14")
_head = _head.replace('def captura(nombre, ancho, pie):\n',
                      'FIG = [0]\n\n\ndef captura(nombre, ancho, pie):\n    FIG[0] += 1\n    pie = "<b>Figura %d.</b> %s" % (FIG[0], pie)\n')
exec(compile(_head, "head", "exec"))

R = "J. F. Rodríguez Bazán · 11/11/2026"
FIRMA = os.path.join(CAP, "firma_verificacion.png")
_fw, _fh = PILImage.open(FIRMA).size
SHA = "2e11e5284ea53623960822fe6e50e7e5340cd29329dd2b523e48163bc53f109f"

E = []
# ------------------------------------------------------------ Portada
E += [Spacer(1, 2.4 * cm),
      Paragraph("Facultad de Ingeniería y Arquitectura · EP Ingeniería de Sistemas",
                ParagraphStyle("a", parent=N, alignment=TA_CENTER, textColor=NAVY)),
      Spacer(1, 0.8 * cm),
      Paragraph("DESARROLLO DE APLICACIONES MÓVILES",
                ParagraphStyle("b", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=19, leading=24,
                               textColor=NAVY)),
      Paragraph("Actividad Autónoma N.º 14",
                ParagraphStyle("c", parent=N, alignment=TA_CENTER, fontName="Helvetica-Bold", fontSize=24, leading=30,
                               textColor=NAVY)),
      Paragraph("Guía de instalación y checklist de entrega del producto",
                ParagraphStyle("d", parent=N, alignment=TA_CENTER, fontSize=13, leading=17)),
      Spacer(1, 1.2 * cm),
      tabla([["Dato", "Detalle"],
             ["Estudiante", "Julio Fabián Rodríguez Bazán"],
             ["Código / Grupo", "202410354 · Grupo 1"],
             ["Docente", "Benjamin David Reyna Barreto"],
             ["Ciclo / Semestre", "VI · 2026-2"],
             ["Unidad / Sesión", "Unidad 3 · Sesión 14"],
             ["Proyecto", "PharmaMobil (KMP) sobre PharmaSoft"],
             ["Etiqueta / commit", "v1.0-final · " + COMMIT[:7]],
             ["Entrega", "Lunes 16 de noviembre de 2026, 23:59"]], [4.6, 12.8]),
      PageBreak()]

toc = TableOfContents()
toc.levelStyles = [ParagraphStyle("t0", fontName="Helvetica-Bold", fontSize=10.5, leading=15, leftIndent=0),
                   ParagraphStyle("t1", fontName="Helvetica", fontSize=9.3, leading=12.5, leftIndent=16)]
E += [Paragraph("Índice", ParagraphStyle("ti", parent=H1, fontSize=17)), toc, Spacer(1, 10),
      Paragraph("Este documento reúne los cuatro productos: A guía de instalación y uso, B checklist de entrega, C matriz de "
                "pruebas y D ficha de versión y firma. Las capturas están numeradas (Figura 1 a 13) y se citan desde el texto. "
                "No contiene contraseñas: ni la del almacén de claves, ni las de la base de datos o de los usuarios del servicio.", N),
      PageBreak()]

# ------------------------------------------------------------ Producto A
E += [Paragraph("1. Producto A · Guía de instalación y uso", H1),
      Paragraph("1.1 Requisitos (versiones con las que se probó)", H2),
      tabla([["Componente", "Versión probada"],
             ["Java (JDK)", "21.0.9 LTS"],
             ["Gradle (wrapper del proyecto)", "9.1.0"],
             ["Android Gradle Plugin / Kotlin", "9.0.1 / 2.4.10"],
             ["Android", "compileSdk 36, minSdk 24 (Android 7.0 o superior)"],
             ["Android Studio", "Una versión que acepte AGP 9.0.1 (se compiló por línea de comandos con ./gradlew)"],
             ["Servicio PharmaSoft", "Spring Boot 4.0.7, Java 21"],
             ["Base de datos", "Oracle Database Free en Docker (imagen gvenzl/oracle-free:slim), Docker 29.9.2. <b>No se usa PostgreSQL</b>"],
             ["Xcode (iOS)", "No probado: no hubo Mac (ver 1.5)"]], [5.6, 11.8]),
      Paragraph("1.2 Levantar el servicio", H2),
      Paragraph("1. Iniciar la base de datos y esperar el mensaje «DATABASE IS READY». Si el contenedor se reinicia, abrir la PDB "
                "(alter pluggable database all open). 2. Arrancar PharmaSoft; Flyway crea el esquema y los datos semilla. "
                "3. Comprobar que responde: una petición sin token a la API devuelve 401 (el servicio está vivo y protegido).", N),
      Preformatted("docker start pharma-oracle\n"
                   "cd pharmaSoft-master\n"
                   "./mvnw spring-boot:run                      # escucha en el puerto 8080\n"
                   "curl -i http://localhost:8080/api/v1/productos     # esperado: HTTP 401", COD),
      Paragraph("La cadena de conexión está en src/main/resources/application.yaml (perfil de desarrollo, puerto 1522 de la "
                "máquina anfitriona). En desarrollo no requiere variables de entorno.", N),
      Paragraph("1.3 Datos de prueba", H2),
      tabla([["Usuario", "Rol", "Privilegios"],
             ["admin", "ADMIN", "Todos (catálogo, clientes, ventas, reportes, usuarios)"],
             ["vendedor", "VENDEDOR", "Leer catálogo; leer y escribir clientes; leer y registrar ventas"],
             ["almacen", "ALMACENERO", "Leer y escribir catálogo"]], [3.0, 3.4, 11.0]),
      Paragraph("Las migraciones V1 a V3 de db/migration crean el esquema, los roles y estos tres usuarios. Las contraseñas de prueba "
                "están en el comentario de cabecera de V3__datos_iniciales_seguridad.sql y no se transcriben aquí.", N),
      Paragraph("1.4 Instalar en Android", H2),
      Paragraph("Archivo: Equipo1_ProyectoFinal_Android_revision.apk (carpeta entregable/s14). El .aab no se instala: es el formato "
                "de la tienda.", N),
      Paragraph("<b>Opción 1, con depuración (recomendada).</b> En el teléfono: Opciones de desarrollador, Depuración inalámbrica "
                "o USB. Con la PC en la misma red Wi-Fi: <i>adb pair IP:PUERTO</i> (código de 6 dígitos) y <i>adb connect IP:PUERTO</i>. "
                "El puerto de conexión cambia cada vez que se reactiva la depuración inalámbrica: leerlo siempre en la pantalla principal.", N),
      Preformatted("adb install -r Equipo1_ProyectoFinal_Android_revision.apk\n"
                   "adb reverse tcp:8080 tcp:8080", COD),
      Paragraph("<b>Opción 2, copiando el archivo.</b> Pasar el .apk al teléfono y abrirlo; Android pide autorizar «instalar apps "
                "desconocidas» para el explorador o el navegador que lo abre. Aceptar y volver a abrir el archivo.", N),
      Paragraph("<b>Importante:</b> con cualquiera de las dos opciones la aplicación abre, pero mostrará «Sin conexión con el servidor» "
                "hasta que se ejecute <i>adb reverse tcp:8080 tcp:8080</i> con el teléfono conectado por adb. Es el único paso que "
                "hizo tropezar a quien probó esta guía.", N),
      Paragraph("<b>Cómo apuntar la aplicación al servicio desde un dispositivo real.</b> En un teléfono físico la aplicación usa "
                "localhost:8080; <i>adb reverse</i> lo redirige al puerto 8080 de la PC, así no hace falta conocer la IP de la red ni abrir "
                "el cortafuegos. (Un emulador usa 10.0.2.2 y no necesita adb reverse.) Sin <i>adb reverse</i> la lista no carga y la "
                "pantalla muestra «Sin conexión». El servicio es HTTP y la aplicación ya declara el tráfico sin cifrar en su manifiesto.", N),
      Paragraph("1.5 Instalar en iOS", H2),
      Paragraph("No fue posible: el equipo de desarrollo es Windows y la firma, Product · Archive y el simulador requieren macOS y "
                "Xcode. Evidencia alternativa: <i>./gradlew :shared:compileKotlinIosSimulatorArm64</i> termina con BUILD SUCCESSFUL, es "
                "decir, el módulo compartido y el código de iosMain compilan a klib. Con un Mac: abrir iosApp en Xcode, elegir el equipo "
                "en Signing &amp; Capabilities, destino «Any iOS Device», Product · Archive; con cuenta gratuita solo se instala en "
                "dispositivos propios (perfil de 7 días). Faltan además GoogleService-Info.plist y los pods de Firebase.", N),
      Paragraph("1.6 Recorrido guiado del flujo principal", H2),
      tabla([["N.º", "Pantalla / acción", "Qué se espera ver"],
             ["1", "Abrir la aplicación sin sesión (Figura 1)", "Solo el inicio de sesión (Usuario, Contraseña, Ingresar)"],
             ["2", "Ingresar con una credencial incorrecta (Figura 2)", "«Usuario o contraseña incorrectos»; no se confunde con un fallo de red"],
             ["3", "Ingresar con una cuenta de prueba y abrir Catálogo (Figura 3)", "Productos descargados, «Todo sincronizado» y la hora de la última sincronización"],
             ["4", "Inventario: registrar un producto (Figura 4)", "Mensaje «registrado correctamente» y el producto en la lista"],
             ["5", "Registrar con nombre «AB», abrir Catálogo y pulsar Revisar (Figura 5)", "El servicio rechaza (400): «El nombre debe tener entre 3 y 150 caracteres»"],
             ["6", "Modo avión y registrar otro producto (Figura 6)", "«Sin conexión · 1 cambio por enviar» y el producto marcado «Pendiente de envío»"],
             ["7", "Recuperar la señal y pulsar Sincronizar (Figura 7)", "«Todo sincronizado»; el pendiente sube y no se duplica"],
             ["8", "Entrar como vendedor con un pendiente de otra sesión (Figura 8)", "403: «Sin permiso para esta operación»; la sesión sigue abierta"],
             ["9", "Catálogo: Activar avisos (Figuras 9 a 11)", "Explicación, diálogo del sistema y los tres estados del permiso"],
             ["10", "Cerrar sesión y reabrir (Figura 12)", "Vuelve al inicio de sesión"]], [0.9, 7.2, 9.3]),
      Spacer(1, 6),
      fila([("C_01_sin_sesion.png", "Inicio de sesión (teléfono real)"),
            ("C_02_login_incorrecto.png", "Credencial incorrecta (teléfono real)"),
            ("C_03_catalogo.png", "Catálogo sincronizado (teléfono real)"),
            ("C_04_crud_crear.png", "Producto registrado (teléfono real)")], 3.95),
      Spacer(1, 6),
      fila([("C_05_validacion_rechazada.png", "Rechazo 400 del servicio (teléfono real)"),
            ("A_06_modo_avion_pendiente.png", "Modo avión: 1 cambio por enviar (Pixel 8)"),
            ("B_07_sync_recuperada.png", "Tras reconectar: todo sincronizado (API 24)"),
            ("A_07_sin_privilegio_403.png", "Vendedor: 403 sin permiso (Pixel 8)")], 3.95),
      Spacer(1, 6),
      fila([("A_08_permiso_denegado.png", "Permiso denegado (Pixel 8)"),
            ("A_09b_avisos_bloqueados.png", "Denegado para siempre (Pixel 8)"),
            ("A_10_permiso_concedido.png", "Permiso concedido (Pixel 8)"),
            ("A_11_cerrar_y_reabrir.png", "Tras cerrar sesión y reabrir (Pixel 8)")], 3.95),
      Paragraph("1.7 Problemas frecuentes", H2),
      tabla([["Síntoma", "Causa", "Solución"],
             ["La lista no carga y dice «Sin conexión»", "El teléfono no alcanza el servicio", "adb reverse tcp:8080 tcp:8080 y comprobar que PharmaSoft corre"],
             ["El servicio falla con ORA-12514 al arrancar", "La PDB FREEPDB1 no está abierta tras reiniciar Oracle", "alter pluggable database all open; y reintentar"],
             ["El puerto de depuración inalámbrica ya no responde", "Android lo rota al reconectar", "Leer el puerto actual en Depuración inalámbrica y repetir adb connect"],
             ["«Conflicto de firma» al instalar", "Hay una versión firmada con otra clave", "adb uninstall pe.edu.upeu.pharmamobil y volver a instalar"]],
            [5.0, 5.0, 7.4]),
      Paragraph("1.8 Registro de la prueba por un tercero", H2),
      Paragraph("La guía fue seguida por un compañero que no forma parte de ningún equipo del proyecto, en su propio teléfono, "
                "sin ayuda más allá de la guía. Instaló el .apk, conectó la aplicación al servicio y la usó correctamente.", N),
      tabla([["Probó", "Fecha", "Dispositivo", "Pasos que se corrigieron"],
             ["Fabian (compañero, sin equipo)", "11/11/2026", "Samsung Galaxy A12, Android 13",
              "Al abrir la aplicación aparecía «Sin conexión con el servidor»: faltaba la conexión con el servicio. Se resaltó en "
              "1.4 que el teléfono necesita adb reverse tcp:8080 tcp:8080 antes de abrir la app (ver también 1.7)."]],
            [3.6, 2.0, 3.8, 8.0]),
      PageBreak()]

# ------------------------------------------------------------ Producto B
E += [Paragraph("2. Producto B · Checklist de entrega", H1),
      Paragraph("Cada punto se marca solo si se comprobó; la columna de evidencia indica cómo. Lo que no se pudo comprobar queda "
                "«Pendiente» con su motivo.", N),
      tabla([["Bloque", "Punto verificado", "Estado", "Responsable y fecha", "Evidencia / observación"],
             ["Repositorios", "Rama feature/builds-14 subida y sin cambios pendientes", "OK", R, "git status limpio; push a origin"],
             ["", "Etiqueta v1.0-final sobre el commit del build (app)", "OK", R, "git rev-parse v1.0-final = " + COMMIT[:7]],
             ["", "Etiqueta en el repositorio del servicio (PharmaSoft)", "Pendiente", "—", "La copia local de PharmaSoft no es un repositorio Git"],
             ["", "Commits propios de los tres integrantes", "No aplica", "—", "Trabajo individual: un solo integrante"],
             ["Secretos", "Almacén de claves y contraseñas fuera del repositorio", "OK", R, "git ls-files sin *.jks ni keystore.properties"],
             ["", "Sin almacén, claves ni google-services.json en el historial", "OK", R, "git log --all --diff-filter=A: ningún resultado"],
             ["", "Cuentas semilla de desarrollo con su contraseña en README.md", "Observación", R, "Son cuentas de desarrollo de las migraciones, no credenciales de firma; no se transcriben en este documento"],
             ["Builds", ".aab firmado con la nomenclatura acordada", "OK", R, "Equipo1_ProyectoFinal_Android.aab (8,3 MB)"],
             ["", ".apk de revisión firmado", "OK", R, "apksigner verify (Figura 13)"],
             ["", "La huella declarada coincide con la del archivo", "OK", R, "SHA-256 2e11e528…f109f (sección 4)"],
             ["Pruebas", "Matriz con tres dispositivos", "Parcial", R, "Dos teléfonos reales del mismo modelo y dos emuladores; falta iOS (sección 3)"],
             ["", "Recorrido completo en cada dispositivo", "Parcial", R, "En el teléfono real se cortó tras el paso 3 (D-05)"],
             ["", "Cada defecto con estado", "OK", R, "D-01 a D-05 con estado"],
             ["Documentación", "Informe técnico con la plantilla del Proyecto Final", "Pendiente", "—", "La plantilla se entrega aparte de este documento"],
             ["", "Guía de instalación probada por un tercero", "OK", "Fabian · 11/11/2026", "Instaló y conectó en su teléfono; registro en 1.8"],
             ["", "Limitaciones declaradas", "OK", R, "iOS, dispositivos reales, copia del almacén, huecos 6 a 11"],
             ["Sustentación", "Cada integrante sabe qué módulo defiende", "Pendiente", "—", "Por definir antes del 17/11/2026"],
             ["", "Plan si falla la red en la demostración", "Pendiente", "—", "Propuesta: modo avión con datos locales; backend en la misma PC con adb reverse"]],
            [2.2, 5.3, 1.9, 3.1, 4.9]),
      Spacer(1, 4),
      Paragraph("<b>Abiertos al cierre:</b> un teléfono real de otro modelo o versión, recorrido completo en el teléfono real, copia "
                "del almacén de claves fuera del equipo, etiqueta del servicio y plan de sustentación.", N),
      PageBreak()]

# ------------------------------------------------------------ Producto C
E += [Paragraph("3. Producto C · Matriz de pruebas completa", H1),
      Paragraph("Build probado en todos los casos: <b>Equipo1_ProyectoFinal_Android_revision.apk</b>, versión 1.0.0 (versionCode 1), "
                "firmado y con reducción de código activada. Los ocho pasos del recorrido son: (1) instalación desde el archivo, "
                "(2) login correcto e incorrecto, (3) CRUD con rechazo de validación, (4) operación sin privilegio, (5) modo avión, "
                "(6) recuperar conexión y sincronizar, (7) permiso en sus tres estados, (8) cerrar sesión y reabrir.", N),
      Paragraph("3.1 Dispositivos", H2),
      tabla([["Dispositivo", "Plataforma", "Pasos ejecutados", "Resultado", "Responsable y fecha"],
             ["A · Samsung Galaxy A12 (SM-A127M), Android 13, <b>real</b>", "Android", "1, 2 y 3. Los pasos 4 a 8 no se ejecutaron: la conexión inalámbrica se perdió", "Correcto con observaciones (D-01, D-05)", R],
             ["B · Pixel 8, Android 17 (API 37.1), emulador", "Android", "1 a 8", "Correcto con observaciones (D-01, D-02, D-03)", R],
             ["C · Android SDK built for x86, Android 7.0 (API 24), emulador de gama baja", "Android", "1, 2, 3 (crear), 5, 6 y 8. El 4 no se ejecutó; el 7 no aplica (sin POST_NOTIFICATIONS en API 24)", "Correcto", R],
             ["E · Samsung Galaxy A12, Android 13, <b>real</b> (de Fabian, compañero sin equipo)", "Android", "Instalación desde el .apk, conexión al servicio y uso de la aplicación (sin recorrido de los ocho pasos)", "Correcto tras conectar el servicio (adb reverse)", "Fabian · 11/11/2026"],
             ["F · iPhone / simulador de iOS", "iOS", "Ninguno", "No ejecutado: limitación declarada", "—"]],
            [4.6, 1.8, 5.2, 3.3, 2.5]),
      Spacer(1, 4),
      Paragraph("<b>Limitación de iOS.</b> Qué se intentó: compilar el código iOS en el equipo disponible. Con qué cuenta: ninguna; no "
                "hay un Mac ni una cuenta de Apple configurada. Qué lo impidió: Xcode y el enlazado del framework solo existen en macOS. "
                "Evidencia alternativa: <i>compileKotlinIosSimulatorArm64</i> con BUILD SUCCESSFUL (el código común, GestorPermisosIos y "
                "las dependencias de Firebase compilan a klib). No hay archivo de Xcode, organizador ni simulador recorrido. "
                "<b>Tampoco se cumple</b> el mínimo de tres dispositivos reales distintos: hay dos teléfonos reales, ambos Samsung Galaxy A12 con "
                "Android 13 (mismo modelo y versión, por lo que cuentan como un solo tipo de dispositivo), y dos emuladores.", N),
      Paragraph("3.2 Defectos", H2),
      tabla([["ID", "Disp.", "Pasos para reproducir", "Esperado", "Obtenido", "Severidad", "Estado"],
             ["D-01", "A, B", "1. Entrar como admin. 2. Inventario: nombre «AB», precio 2, stock 10. 3. Registrar. 4. Catálogo, Revisar", "El error del servicio llega al campo nombre", "El formulario dice «registrado correctamente»; el motivo solo aparece en Catálogo, Revisar", "Media", "Pendiente (limitación)"],
             ["D-02", "B", "1. Sin red, admin registra un producto. 2. Recuperar red y cerrar sesión. 3. Entrar como vendedor. 4. Abrir Catálogo", "La cola de otro usuario no se envía", "Se envía con la sesión nueva y el servicio responde 403", "Alta", "Pendiente (limitación)"],
             ["D-03", "A, B, C", "Cerrar sesión como admin y volver a abrir", "Usuario vacío y base local limpia", "El usuario queda escrito y la base conserva los datos", "Media", "Pendiente (limitación)"],
             ["D-04", "—", "Repetir la matriz en dos teléfonos reales más", "Tres dispositivos reales", "Solo hay un teléfono real", "Media", "Pendiente"],
             ["D-05", "A", "Ejecutar los pasos 4 a 8 en el teléfono real", "Recorrido completo", "La conexión inalámbrica se perdió tras el paso 3", "Baja", "Pendiente: repetir"]],
            [1.0, 1.1, 4.6, 3.0, 3.9, 1.3, 2.5]),
      Spacer(1, 4),
      Paragraph("Resultado de la reducción de código: con R8 activo la lista se carga (serialización) y las pantallas con inyección "
                "abren sin cierres en los tres dispositivos Android; no hizo falta desactivarla.", N),
      PageBreak()]

# ------------------------------------------------------------ Producto D
E += [Paragraph("4. Producto D · Ficha de versión y firma", H1),
      tabla([["Dato", "Valor"],
             ["Identificador de la aplicación", "pe.edu.upeu.pharmamobil (se mantiene; cambiarlo invalida el google-services.json registrado)"],
             ["versionName y versionCode", "1.0.0 · 1"],
             ["Etiqueta y commit", "v1.0-final · " + COMMIT[:7] + " (repositorio de la aplicación). Repositorio del servicio: sin etiqueta (ver checklist)"],
             ["Archivos entregados", "Equipo1_ProyectoFinal_Android.aab (8,3 MB) · Equipo1_ProyectoFinal_Android_revision.apk (4,2 MB). Sin .ipa (iOS)"],
             ["Titular del certificado", "CN=Julio Fabian Rodriguez Bazan, OU=EP Ingenieria de Sistemas, O=Universidad Peruana Union, L=Lima, C=PE"],
             ["Huella SHA-256", SHA],
             ["Validez del certificado", "Emitido el 11/11/2026; caduca en 10 000 días (marzo de 2054)"],
             ["Minificación", "Activada (R8). Reglas mínimas para serialización y Koin en androidApp/proguard-rules.pro"],
             ["Contraseñas", "Ninguna se transcribe en este documento"]], [4.6, 12.8]),
      Spacer(1, 6),
      Image(FIRMA, width=16.8 * cm, height=16.8 * cm * _fh / _fw),
      Paragraph("<b>Figura 13.</b> Verificación con apksigner: titular y huellas del certificado del equipo.", PIE),
      Spacer(1, 10),
      Paragraph("5. Repositorios y commit etiquetado", H1),
      tabla([["Elemento", "Enlace / valor"],
             ["Repositorio de la aplicación", REPO],
             ["Rama", REPO + "/tree/feature/builds-14"],
             ["Etiqueta", REPO + "/releases/tag/v1.0-final"],
             ["Commit etiquetado", COMMIT],
             ["Repositorio del servicio (PharmaSoft)", "https://github.com/dreyna/pharmaSoft (copia local sin Git; sin etiqueta propia)"]],
            [4.8, 12.6]),
      Spacer(1, 8),
      Paragraph("Lista de cotejo", H2),
      tabla([["N.º", "Criterio", "Cumple"],
             ["1", "La guía indica las versiones con las que se probó", "Sí"],
             ["2", "La guía explica cómo apuntar la app al servicio desde un dispositivo real", "Sí"],
             ["3", "La guía fue seguida por un compañero de otro equipo y el registro está incluido", "Sí (Fabian, sin equipo; 1.8)"],
             ["4", "El checklist cubre repositorios, secretos, builds, pruebas, documentación y sustentación", "Sí"],
             ["5", "Cada punto tiene responsable y fecha de verificación", "Sí (los pendientes, sin responsable)"],
             ["6", "La matriz registra tres dispositivos reales, con iOS o su limitación documentada", "Parcial (uno real; iOS documentado)"],
             ["7", "Cada defecto con identificador, dispositivo, pasos, esperado, severidad y estado", "Sí"],
             ["8", "La ficha declara identificador, versión, etiqueta, commit y huella", "Sí"],
             ["9", "Ninguna contraseña aparece en el documento", "Sí"],
             ["10", "Capturas numeradas y referidas desde el texto", "Sí"],
             ["11", "Nomenclatura pedida y entrega en plazo", "Archivo nombrado; falta subirlo"]], [1.0, 11.2, 5.2])]

SALIDA = os.path.join(RAIZ, "docs", "entrega", "RodriguezBazan_JulioFabian_Autonoma14.pdf")
os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
Doc(SALIDA, pagesize=letter, leftMargin=2.0 * cm, rightMargin=2.0 * cm, topMargin=1.8 * cm, bottomMargin=1.7 * cm,
    title="Actividad Autónoma 14 - Rodríguez Bazán", author="Julio Fabián Rodríguez Bazán").multiBuild(E)
print(SALIDA, round(os.path.getsize(SALIDA) / 1e6, 2), "MB")
