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

puerta_cerrada = True
tiempo = Timer(-1)

def sub_cb(topic, msg):
    global persona
    persona = msg.decode('UTF-8')[19:]
    print("***********")
    print(persona)

def abrir_cerrar(persona):
    global puerta_cerrada
    print("puerta_cerrada") 
    print(puerta_cerrada)
    if puerta_cerrada:
        puerta_cerrada=False        
        led(1)
        c.publish(topic_pub, persona)
        print("puerta_cerrada") 
        print(puerta_cerrada)

        def timer(t):
            global puerta_cerrada
            led(0)
            mensaje = "PUERTA CERRADA!!"
            c.publish(topic_pub, mensaje)
            puerta_cerrada=True
            print("puerta_cerrada") 
            print(puerta_cerrada)
            print("----------------------------")

        tiempo.init(period=3000, mode=Timer.ONE_SHOT, callback=timer)

    elif not puerta_cerrada:
        print("\\\\\\\\\\\\PUERTA ABIERTA\\\\\\\\\\\\")
    
def subscriber():
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

subscriber()