import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Luz:
    def __init__(self, identifier, server, topic, payload):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topic = topic
        self.payload = payload
        self._publish_identifier()

    def _publish_identifier(self):
        self.client.connect(self.server)
        logging.info("TOPICO: " f'{self.topic[0]}')
        logging.info("PAYLOAD/ID: " f'{self.payload}')
        self.client.publish(self.topic[0], self.payload)


    def publish(self):
        self.client.connect(self.server)
        logging.info(f'{self.topic, self.payload}')
        self.client.publish(self.topic, self.payload)
        

if __name__ == '__main__':
    identifier = "luzUnoDitesi"
    topic = topic = ['devices', 'sistemaPrincipal']
    server = "127.0.0.1"
    #server = "169.254.254.254"
    payload = identifier

    luzDitesi = Luz(identifier, server, topic, payload)

    #lectorDitesi.publish()
