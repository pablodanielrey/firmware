import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Lector:
    def __init__(self, identifier, server, topic, payload, card):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topic = topic
        self.payload = payload
        self.c = card
        self._publish_identifier()

    def _publish_identifier(self):
        self.client.connect(self.server)
        logging.info("TOPICO: " f'{self.topic[0]}')
        logging.info("PAYLOAD/ID: " f'{self.payload}')
        self.client.publish(self.topic[0], self.payload)

    def publish(self):
        #self.client.connect(self.server)
        #logging.info(f'{self.topic[1], self.payload}')
        #self.client.publish(self.topic[1], self.payload)
        if self.cardDetected():
            self.client.connect(self.server)
            logging.info('TARJETA DETECTADA: ' f'{self.c[1]}' ' en ' f'{self.payload}')
            self.client.publish(self.topic[2], self.c[1])

    def cardDetected(self):
        if len(sys.argv) > 1:
            return True
    
if __name__ == '__main__':
    card = sys.argv
    identifier = "lectorUnoDitesi"
    topic = ['distpositivos', 'sistemaPrincipal', 'tarjetaDetectada']
    #server = "127.0.0.1"
    server = "169.254.254.254"
    payload = identifier
    lectorDitesi = Lector(identifier, server, topic, payload, card)

    lectorDitesi.publish()
