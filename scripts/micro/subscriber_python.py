#!/usr/bin/env python3

import paho.mqtt.client as mqtt

def on_connect(client, userdata, flags, rc):
  print("Esperando... ")
  client.subscribe("topico/estado")

def on_message(client, userdata, msg):
  if msg.payload.decode() == "Puerta abierta!":
    print("Puerta abierta!")
    client.disconnect()
    
client = mqtt.Client()
client.connect("169.254.254.254",1883,60)

client.on_connect = on_connect
client.on_message = on_message

client.loop_forever()