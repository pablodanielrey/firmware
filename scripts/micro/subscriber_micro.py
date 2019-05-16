#Script para micropython
import time
from umqttsimple import MQTTClient
from machine import Pin
led = Pin(15, Pin.OUT)
def sub_cb(topic, msg):
    print((topic, msg))
def subscriber(server="169.254.254.254"):
    c = MQTTClient("umqtt_client", server)
    c.set_callback(sub_cb)
    c.connect()
    c.subscribe("topico/prueba")
    while True:
        if True:
            c.wait_msg()
            led(1)
            c.publish("puerta/estado", "Puerta abierta!")
            time.sleep_ms(3000)
            led(0)
            c.publish("puerta/estado", "Puerta Cerrada!")
        else:
            c.check_msg()
            time.sleep(1)
    c.disconnect()
