import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import paho.mqtt.subscribe as subscribe

import time
import paho.mqtt.client as paho

class Lector:
    def __init__(self,identificador, server, payload):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.topic  = identificador
        self.client.on_message = payload
        
    def publish(self):
        self.client.connect(self.server)
        self.client.publish(self.topic, self.client.on_message)


class Cerradura:
    def __init__(self, identificador, server, topic):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.identificador = identificador
        self.topic = topic
        

    def on_message(client, userdata, message):
        print("TOPICO ",self.message.topic)
        print("PAYLOAD " ,self.message.payload)

    def subscriber(self):
        self.client.on_message = self.on_message 
        print("CONECTANDO CON EL SERVIDOR....")
        self.client.connect(self.server)
        self.client.loop_start()
        print("SUBSCRIBIENDO A TOPICO", self.topic)
        self.client.subscribe(self.topic)
        time.sleep(10)
        self.client.loop_stop()



class Luz:
    def __init__(self, identificador):
        self.identificador  = identificador
        self.estado = estado


class Sistema:
    def __init__(self, oficina):
        self.oficina = oficina
