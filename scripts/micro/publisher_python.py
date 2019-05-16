#!/usr/bin/env python3

import paho.mqtt.client as mqtt

client = mqtt.Client()
client.connect("169.254.254.254",1883,60)
client.publish("topico/prueba", "Puerta abierta!")
client.disconnect()