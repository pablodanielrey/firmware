import paho.mqtt.client as mqtt
import time

class Lector:
    def __init__(self, identificador, server, topicos, payloads):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.topic = topicos
        self.contenido = payloads

    def publish(self, dispositivo):
        self.client.connect(self.server)
        if dispositivo=='puerta':
            print(self.topic[0], self.payload)
            self.client.publish(self.topic[0], self.payload)
        if dispositivo=='luminaria':
            print(self.topic[1], self.payload)
            self.client.publish(self.topic[1], self.payload)

    def abrir(self): #hay  que mejorar esta funcion
        self.payload = self.contenido['puerta'][0]
        print(self.payload)
        self.publish('puerta')
        time.sleep(3)
        self.payload = self.contenido['puerta'][1]
        print(self.payload)
        self.publish('puerta')

    def prender(self): #hay  que mejorar esta funcion
        self.payload = self.contenido['luminaria'][0]
        print(self.payload)
        self.publish('luminaria')

class Cerradura:
    def __init__(self, identificador, server, topic):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.identificador = identificador
        self.topic = topic

    def on_message(self, client, userdata, message):
        print("TOPICO ", message.topic)
        print("PAYLOAD " , message.payload.decode("utf8"))
        print("DISPOSITIVO ", self.identificador)
        print("********************************")

    def subscriber(self):
        self.client.on_message = self.on_message 
        self.client.connect(self.server)
        self.client.loop_start()
        print("SUSCRIBIENDO A TOPICO", self.topic)
        print("-------------------------------")
        self.client.subscribe(self.topic)
        time.sleep(10)
        self.client.loop_stop()


class Luz:
    def __init__(self, identificador, server, topic):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.identificador = identificador
        self.topic = topic

    def on_message(self, client, userdata, message):
        print("TOPICO ", message.topic)
        print("PAYLOAD " , message.payload.decode("utf8"))
        print("DISPOSITIVO ", self.identificador)
        print("********************************")

    def subscriber(self):
        self.client.on_message = self.on_message 
        self.client.connect(self.server)
        self.client.loop_start()
        print("SUSCRIBIENDO A TOPICO", self.topic)
        print("-------------------------------")
        self.client.subscribe(self.topic)
        time.sleep(10)
        self.client.loop_stop()



class Sistema:
    def __init__(self, oficina):
        self.oficina = oficina
