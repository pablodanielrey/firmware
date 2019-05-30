#Script para micropython
import utime
from umqttsimple import MQTTClient
from machine import Pin

persona = " "

server="169.254.254.254"
mqttClient = "umqtt_client"
topic_sub = "topico/prueba"
topic_pub = "puerta/estado"
led = Pin(15, Pin.OUT)

c = MQTTClient(mqttClient, server)

puerta_abierta = False

tiempo = utime.ticks_add(utime.ticks_ms(), -1000)
ini = 0

def sub_cb(topic, msg):
    print("SUBCB")
    msg_decode = msg.decode('UTF-8')
    global persona
    persona = msg_decode
    return persona
    
def abrir_cerrar(persona):
    print("ABRIR Y CERRAR")
    led(1)
    c.publish(topic_pub, persona)
    utime.sleep_ms(5000)
    led(0)
    persona = "PUERTA CERRADA!!"
    c.publish(topic_pub, persona)
    
def subscriber(server):
    print("SUBSCRIBIR")
    c.set_callback(sub_cb)
    c.connect()
    c.subscribe(topic_sub)
    while True:
        c.check_msg()
        #if persona != " ":
        abrir_cerrar(persona)
        #persona = " "
        print("FIN") 
    c.disconnect()