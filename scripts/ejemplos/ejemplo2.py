personas = {}
def enviar_mensaje(topico, mensaje):
    pass

def abrir_puerta(oficina, persona):
    topico = oficina 
    payload = 'abrir,puerta'
    enviar_mensaje(topico, payload)
    if persona is not personas:
        personas[persona]= "adentro"
    if persona in personas:
        if personas[persona]=="adentro":
            personas[persona]= "afuera"
        elif if personas[persona]=="afuera":
            personas[persona]= "adentro"
    print(personas)

def cerrar_puerta(oficina, persona):
    topico = oficina
    payload = 'cerrar,puerta'
    enviar_mensaje(topico, payload)
    

def prender_luz(oficina):
    topico = oficina
    payload = 'prender,luz'
    enviar_mensaje(topico, payload)
No tanto po
def apagar_luz(oficina):
    topico = oficina
    payload = 'apagar,luz'
    enviar_mensaje(topico, payload)



""" lado servidor """

def recibe_mensaje(topico, mensaje):
    """ mensaje tiene el recuro y la accion de alguna forma """
    accion = mensaje.split(",")[1]
    recurso = mensaje.split(",")[2]
    if accion == "abrir":
        abrir_puerta(recurso)
    elif accion == "cerrar":
        cerrar_puerta(recurso)
    elif accion == "prender":
        prender_luz(recurso)
    elif accion == "apagar":
        apagar_luz(recurso)


def abrir_puerta(recurso):
    print("abrir el recurso")

def cerrar_puerta(recurso):
    print("cerrar recurso")

def prender_luz(recurso):
    print("prender recurso")

def apagar_luz(recurso):
    print("apagar recurso")

recibe_mensaje()