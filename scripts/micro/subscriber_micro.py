#Script para micropython
import time
from umqttsimple import MQTTClient
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
            c.publish("topico/estado", "Puerta abierta!")
        else:
            c.check_msg()
            time.sleep(1)
    c.disconnect()
