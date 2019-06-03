#Script para micropython
from umqttsimple import MQTTClient
from machine import Pin
from machine import Timer

persona = " "

server="169.254.254.254"
mqttClient = "umqtt_client"
topic_sub = "topico/prueba"
topic_pub = "puerta/estado"
led = Pin(15, mode=Pin.OUT)
c = MQTTClient(mqttClient, server)

puerta_abierta = False
tiempo = Timer(-1)


def sub_cb(topic, msg):
    global puerta_abierta
    global persona
    persona = msg.decode('UTF-8')[19:]
    print(puerta_abierta)
    print(persona)
    return persona        

def abrir_cerrar(persona):
    global puerta_abierta
    print(puerta_abierta)
    if not puerta_abierta:
        puerta_abierta=True        
        led(1)
        c.publish(topic_pub, persona)
        print(puerta_abierta)
        def timer(t):
            led(0)
            persona = "PUERTA CERRADA!!"
            c.publish(topic_pub, persona)
            puerta_abierta=False
            print(puerta_abierta)
        tiempo.init(period=3000, mode=Timer.ONE_SHOT, callback=timer)
    elif puerta_abierta:
        print("puerta abierta!!!")
    


def subscriber(server):
    global persona
    c.set_callback(sub_cb)
    c.connect()
    c.subscribe(topic_sub)
    while True:
        c.check_msg()
        if persona != " ":
            abrir_cerrar(persona)
            persona = " "
    c.disconnect()