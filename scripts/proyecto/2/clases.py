import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import paho.mqtt.subscribe as subscribe

import time
import paho.mqtt.client as paho

class Lector:
    def __init__(self, identificador, server, payload):
        self.client = mqtt.Client(identificador)
        self.topic  = identificador
        self.server = server
        self.client.on_message = payload
        
    
    def publish(self):
        print("Conectando ",self.server)
        self.client.connect(self.server)
        self.client.loop_start()
        print("publicando en  ", self.topic)
        self.client.publish(self.topic, self.client.on_message )
        print(self.client.on_message )
        self.client.disconnect()
        self.client.loop_stop()
        


class Cerradura:
    def __init__(self, identificador, server, topic):
        self.client= mqtt.Client(identificador)
        self.identificador = identificador
        self.server = server
        self.client.on_message = topic

    def on_message(client, userdata, message):
        print("MENSAJE RECIBIDO =",str(self.message.payload.decode("utf-8")))
   
    def on_connect(self):
        print("Conectando ",self.server)
        self.client.subscribe(self.client.on_message)
        print("Subscribiendo a  " ,self.client.on_message)
        self.client.connect(self.server)
        self.client.loop_forever()


        #print("Conectando ",self.server)
        #self.client.connect(self.server)
        #self.client.loop_forever()()
        #self.client.subscribe(self.client.on_message)
        #print("Subscribiendo a  " ,self.client.on_message)
        
    




class Luz:
    def __init__(self, identificador):
        self.identificador  = identificador
        self.estado = estado


class Sistema:
    def __init__(self, oficina):
        self.oficina = oficina
