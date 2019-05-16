#!/usr/bin/env python3

import paho.mqtt.client as mqtt

def on_connect(client, userdata, flags, rc):
  client.subscribe("topico/prueba")

def on_message(client, userdata, msg):
  if msg.payload.decode() == "Puerta abierta!":
    print("Se conecto!!")
    client.publish("topico/estado", "Puerta abierta!")
    client.disconnect()
    
client = mqtt.Client()
client.connect("169.254.254.254",1883,60)

client.on_connect = on_connect
client.on_message = on_message

client.loop_forever()