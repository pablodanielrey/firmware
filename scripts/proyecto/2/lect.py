import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Lector:
    def __init__(self, identificador, server, topic, payload):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.topic = topic
        self.payload = payload

    def publish(self, dispositivo):
        self.client.connect(self.server)
        logging.info(f'{self.topic, self.payload}')
        self.client.publish(self.topic, self.payload)
        

if __name__ == '__main__':
    identificador = "lectorUnoDitesi"
    topic = 'sistemaPrincipal'
    #server = "127.0.0.1"
    server = "169.254.254.254"
    payload = identificador

    lectorDitesi = Lector(identificador, server, topicos, payloads)
