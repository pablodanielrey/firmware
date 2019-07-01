class Oficina:

    def __init__(self, nombre)
        self.nombre = nombre
    

class Luz:

    def __init__(self, oficina)
        self.oficina = oficina
        self.estado = False

    def encender(self):
        pass

    def apagar(self):
        pass


class Mensaje:
    topico = ""
    payload = ""
    def enviar(self):
        pass


ditesi = Oficina("ditesi")
cerradura_ditesi = Cerradura(ditesi)
luz_ditesi = Luz(ditesi)