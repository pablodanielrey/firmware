#!/usr/bin/env python3

import paho.mqtt.client as mqtt
import sys

usuario = sys.argv[1]

server = "169.254.254.254"
#port = 1883
topic_pub = "topico/prueba"
pyload = "Puerta abierta por " + usuario
arc = 'ditesi.txt'

def publish():
    client = mqtt.Client()
    #client.connect(server,port)
    client.connect(server)
    client.publish(topic_pub, pyload)
    client.disconnect()
    print(topic_pub, pyload)

def check_string():
    archivo=open(arc, 'r')
    for line in archivo:
        if usuario in line:
            publish()
            break

check_string()