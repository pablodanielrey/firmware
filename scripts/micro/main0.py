#Script para micropython
import time
from umqttsimple import MQTTClient
from machine import Pin

server="169.254.254.254"
mqttClient = "umqtt_client"
topic_sub = "topico/prueba"
topic_pub = "puerta/estado"
led = Pin(15, Pin.OUT)

#persona = ""

c = MQTTClient(mqttClient, server)


def sub_cb(topic, msg):
    print((topic, msg))
    #los que llegan con la puerta cerrada - registra mensaje para abrir
    print(type(msg))
    msg_decode = msg.decode('UTF-8')
    global persona
    persona = msg_decode
    return persona
    
def abrir_cerrar(persona):
    #variable global para la ultima ejecución de abrir_cerrar
    led(1)
    c.publish(topic_pub, persona)
    time.sleep_ms(5000)
    led(0)
    persona = "PUERTA CERRADA!!"
    c.publish(topic_pub, persona)
    
def subscriber(server):
    c.set_callback(sub_cb)
    c.connect()
    c.subscribe(topic_sub)
    while True:
        mensaje = c.wait_msg()
        abrir_cerrar(persona)
        # aca se llama a abrir
        print(mensaje)
        print('despues de whait!')
    c.disconnect()