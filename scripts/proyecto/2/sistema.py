import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)

class Sistema:
    def __init__(self, identifier, server, topic, payload):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topic = topic
        self.contenido = payload

    def subscriber(self):
        self.client.connect(self.server)
        logging.info("SUSCRIBIENDO A TOPICO  "f'{self.topic}')
        logging.info("-------------------------------")
        self.client.subscribe(self.topic)
        self.client.loop_forever()

    def publish(self):
        self.client.connect(self.server)
        logging.info("PUBLICANDO A TOPICO  "f'{self.topic, self.payload}')
        self.client.publish(self.topic, self.payload)

if __name__ == '__main__':
    identifier = 'sistemaUnoPrincipal'
    server = "169.254.254.254"
    topics = ['lectorUnoDitesi', 'cerraduraUnoDItesi', 'lamparaUnoDItesi']
    payloads = {'puerta': ['abrir','cerrar'], 'luminaria': ['prender','apagar'] }

    sistemaUnoPrincipal = Sistema(identifier, server, topics, payloads)

        