#!/usr/bin/env python3

import paho.mqtt.client as mqtt
import sys

usuario = sys.argv[1]

def enviar_mensaje():
    client = mqtt.Client()
    client.connect("169.254.254.254",1883)
    client.publish("cerradura/ditesi", usuario)
    client.disconnect()
    print("Enviado!!!")

enviar_mensaje()