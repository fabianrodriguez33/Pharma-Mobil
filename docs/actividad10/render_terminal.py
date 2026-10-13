"""Convierte salidas de consola reales (adb, curl, gradle) en imagenes PNG para el informe."""
import os
import sys
import textwrap

from PIL import Image, ImageDraw, ImageFont

AQUI = os.path.dirname(os.path.abspath(__file__))
FUENTE = r"C:\Windows\Fonts\consola.ttf"


def render(titulo, lineas, destino, ancho_car=96):
    font = ImageFont.truetype(FUENTE, 17)
    bold = ImageFont.truetype(r"C:\Windows\Fonts\consolab.ttf", 17)
    filas = []
    for l in lineas:
        filas += textwrap.wrap(l, ancho_car, subsequent_indent="    ") or [""]
    alto_l = 24
    w = 16 + ancho_car * 10 + 16
    h = 44 + len(filas) * alto_l + 16
    img = Image.new("RGB", (w, h), "#0F1B2D")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w, 32], fill="#1F3B63")
    d.text((14, 6), titulo, font=bold, fill="#DCEBFF")
    for i, f in enumerate(filas):
        color = "#E8F1FF"
        if f.startswith("--") or f.startswith("=="):
            color = "#7FD6FF"
        elif "401" in f or "403" in f:
            color = "#FFD479"
        elif "200" in f or "201" in f or "PASSED" in f:
            color = "#9CE6A5"
        d.text((16, 44 + i * alto_l), f, font=font, fill=color)
    img.save(destino)


def leer(nombre):
    with open(os.path.join(AQUI, nombre), encoding="utf-8", errors="replace") as fh:
        return [x.rstrip("\n") for x in fh]


if __name__ == "__main__":
    cut = lambda ls: [l if len(l) < 150 else l[:60] + " ... [cifrado AES-GCM, " + str(len(l)) + " caracteres Base64] ... " + l[-12:] for l in ls]
    render("adb shell run-as pe.edu.upeu.pharmamobil cat shared_prefs/almacen_seguro.xml",
           cut(leer("p2-almacen_seguro.xml.txt")), os.path.join(AQUI, "p2-almacen_seguro.png"), 110)
    render("adb logcat (System.out) - Ktor Logging nivel HEADERS", leer("p2-logcat.txt"),
           os.path.join(AQUI, "p2-logcat.png"), 90)
    render("almacen_seguro.xml antes y despues de cerrarSesion()", cut(leer("p3-almacen.txt")),
           os.path.join(AQUI, "p3-almacen.png"), 110)
    render("curl contra PharmaSoft - bitacora y matriz", leer("curl-bitacora.txt"),
           os.path.join(AQUI, "curl-bitacora.png"), 96)
    if os.path.exists(os.path.join(AQUI, "p3-test.txt")):
        render("gradlew testAndroidHostTest - CerrarSesionTest", leer("p3-test.txt"),
               os.path.join(AQUI, "p3-test.png"), 90)
    print("ok")
