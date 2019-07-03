import sys

class Oficina:
    def __init__(self, nombre):
        self.nombre = nombre
        self.personas = {}

    def personaEnOficina(self):
        pass
              

class Cerradura:
    def __init__(self, oficina, identificador):
        self.identificador = identificador
        self.oficina = oficina
        self.estado = False

    def abrir(self):
        print("Abrir")

    def cerrar(self):
        print("Cerrar")

    def cambiarEstado(self):
        if self.estado:
            self.estado = False
            self.cerrar()
        else:
            self.estado = True
            self.abrir()
            

class Mensaje:
    def __init__(self, topico, payload):
        self.topico = topico
        self.payload = payload
        

    def enviar(self):
        print(self.topico)
        print(self.payload)


ditesi = Oficina("ditesi")
cerraduraDitesi = Cerradura("cerradura01ditesi")
mensajeCerradura01Ditesi = Mensaje(cerraduraDitesi.identificador, cerraduraDitesi.cambiarEstado())