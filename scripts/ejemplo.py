def enviar_mensaje_mqtt(topico, mensaje):
    pass

def abrir_puerta(oficina):
    topico = oficina+"/puerta"
    mensaje = "abrir" 
    enviar_mensaje_mqtt(topico, mensaje)

abrir_puerta(oficina)

def cerrar_puerta(oficina):
    topico = oficina+"/puerta"
    mensaje = "cerrar"
    enviar_mensaje_mqtt(topico ,mensaje)

cerrar_puerta(oficina)


def encender_luz(oficina):
    topico = oficina+"/luz"
    mensaje = "encender" 
    enviar_mensaje_mqtt(topico, mensaje)

encender_luz(oficina)

def apagar_luz(oficina):
    topico = oficina+"/luz"
    mensaje = "apagar"
    enviar_mensaje_mqtt(topico,mensaje)

apagar_luz(oficina)














