import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Cerradura:
    def __init__(self, identifier, server, topic, payload):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topic = topic
        self.payload = payload

    def publish(self):
        self.client.connect(self.server)
        logging.info(f'{self.topic, self.payload}')
        self.client.publish(self.topic, self.payload)
        

if __name__ == '__main__':
    identifier = "cerraduraUnoDitesi"
    topic = 'sistemaPrincipal'
    #server = "127.0.0.1"
    server = "169.254.254.254"
    payload = identifier

    lectorDitesi = Cerradura(identifier, server, topic, payload)

    lectorDitesi.publish()
