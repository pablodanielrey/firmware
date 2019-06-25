#!/usr/bin/env python3

import paho.mqtt.client as mqtt

server = "169.254.254.254"
port = 1883
topic_sub = "cerradura/ditesi"
topic_pub = "ditesi/usuario"
arc = 'usuarios-ditesi.txt'
payload = "ditesi/usuario"

def on_connect(client, userdata, flags, rc):
    client.subscribe(topic_sub)
    print("Esperando... ")
    check_string()

def enviar_mensajes(client, userdata, msg):
    client.publish(topic_pub, payload)
    print(topic_pub, msg.payload.decode('UTF-8'))
    
def check_string():
      usuario = msg.payload.decode('UTF-8')
      archivo=open(arc, 'r')
      for line in archivo:
        if usuario in line:
          print("EL USUARIO EXISTE")
          enviar_mensajes(client, userdata, msg)
          break
    
    
client = mqtt.Client()
client.connect(server,port)

client.on_connect = on_connect
client.enviar_mensajes = enviar_mensajes

client.loop_forever()

