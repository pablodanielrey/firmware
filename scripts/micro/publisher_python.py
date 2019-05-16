#!/usr/bin/env python3

import paho.mqtt.client as mqtt
import sys

usuario = sys.argv[1]

def publish():
    client = mqtt.Client()
    client.connect("169.254.254.254",1883,60)
    client.publish("topico/prueba", "Puerta abierta!")
    client.disconnect()

def check_string():
    archivo=open('ditesi.txt', 'r')
    for line in archivo:
        if usuario in line:
            publish()
            break

check_string()
