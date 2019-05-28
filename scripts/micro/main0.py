#Script para micropython
import time
from umqttsimple import MQTTClient
from machine import Pin

server="169.254.254.254"
mqttClient = "umqtt_client"
topic_sub = "topico/prueba"
topic_pub = "puerta/estado"
led = Pin(15, Pin.OUT)

c = MQTTClient(mqttClient, server)

def sub_cb(topic, msg):
    print((topic, msg))
    led(1)
    c.publish(topic_pub, msg)
    time.sleep_ms(5000)
    led(0)
    msg = "PUERTA CERRADA!!"
    c.publish(topic_pub, msg)

def subscriber(server):
    c.set_callback(sub_cb)
    c.connect()
    c.subscribe(topic_sub)
    while True:
        c.wait_msg()
        print('despues de whait!')
    c.disconnect()