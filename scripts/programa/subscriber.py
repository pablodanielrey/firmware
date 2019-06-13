#!/usr/bin/env python3

import paho.mqtt.client as mqtt

server = "169.254.254.254"
port = 1883
topic_sub = "puerta/estado"

def on_connect(client, userdata, flags, rc):
  print("Esperando... ")
  client.subscribe(topic_sub)

def on_message(client, userdata, msg):
    print(msg.payload)
    #client.disconnect()
    
client = mqtt.Client()
client.connect(server,port)

client.on_connect = on_connect
client.on_message = on_message

client.loop_forever()