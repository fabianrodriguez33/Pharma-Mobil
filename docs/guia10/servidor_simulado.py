import json, sys, os
BASE=os.path.dirname(os.path.abspath(__file__))
REVOKE=os.path.join(BASE,"revoke.flag"); EXPIRE=os.path.join(BASE,"expire.flag")
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
USERS = {
 "admin": ("Admin123", "Carlos Ramirez Torres", ["CATALOGO_ESCRIBIR","CATALOGO_LEER","CLIENTES_ESCRIBIR","CLIENTES_LEER","REPORTES_VER","USUARIOS_ADMINISTRAR","VENTAS_LEER","VENTAS_REGISTRAR"]),
 "vendedor": ("Vendedor123", "Laura Mendoza", ["CATALOGO_LEER","CLIENTES_ESCRIBIR","CLIENTES_LEER","VENTAS_LEER","VENTAS_REGISTRAR"]),
 "almacen": ("Almacen123", "Pedro Quispe", ["CATALOGO_ESCRIBIR","CATALOGO_LEER"]),
}
PRODUCTOS = [{"id":i,"nombre":n,"descripcion":"","precio":p,"stock":s,"estado":True,"categoriaId":1,"categoriaNombre":"General","imagen":""} for i,(n,p,s) in enumerate([("Paracetamol 500 mg",3.5,120),("Ibuprofeno 400 mg",5.2,8),("Amoxicilina 500 mg",12.0,60)],1)]
class H(BaseHTTPRequestHandler):
    def send(self, code, body=None):
        self.send_response(code); self.send_header("Content-Type","application/json")
        data = json.dumps(body).encode() if body is not None else b""
        self.send_header("Content-Length", str(len(data))); self.end_headers(); self.wfile.write(data)
    def user(self):
        a = self.headers.get("Authorization","")
        print("AUTH:", self.command, self.path, "->", "Bearer ..." if a.startswith("Bearer ") else "(sin cabecera)", flush=True)
        if a.startswith("Bearer mock-"): return a[len("Bearer mock-"):]
        return None
    def do_POST(self):
        n = int(self.headers.get("Content-Length",0)); raw = self.rfile.read(n)
        if self.path.endswith("auth/login"):
            self.user(); d = json.loads(raw)
            u = USERS.get(d.get("username"))
            if not u or u[0] != d.get("password"): return self.send(401, {"message":"Credenciales invalidas"})
            return self.send(200, {"token":"mock-"+d["username"],"tipo":"Bearer","expiraEnSegundos":3600,"username":d["username"],"nombreCompleto":u[1],"roles":[],"permisos":u[2]})
        u = self.user()
        if not u: return self.send(401, {"message":"El token no es valido"})
        if os.path.exists(REVOKE) or "CATALOGO_ESCRIBIR" not in USERS.get(u, ("","",[]))[2]: return self.send(403, {"message":"No tiene permiso"})
        self.send(201, PRODUCTOS[0])
    do_PUT = do_POST
    def do_DELETE(self):
        u = self.user()
        if not u: return self.send(401, {"message":"El token no es valido"})
        if "CATALOGO_ESCRIBIR" not in USERS.get(u, ("","",[]))[2]: return self.send(403, {"message":"No tiene permiso"})
        self.send(204)
    def do_GET(self):
        u = self.user()
        if not u or u not in USERS or os.path.exists(EXPIRE): return self.send(401, {"message":"El token no es valido"})
        self.send(200, {"contenido":PRODUCTOS,"pagina":0,"tamanio":20,"totalElementos":3,"totalPaginas":1,"ultima":True})
    def log_message(self, *a): pass
ThreadingHTTPServer(("0.0.0.0", 8080), H).serve_forever()
