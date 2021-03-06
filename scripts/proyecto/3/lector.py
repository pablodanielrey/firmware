import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Lector:
    def __init__(self, identifier, server, topic, payload):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topic = topic
        self.payload = payload
        self.client.on_message = self.on_message
        self._publish_identifier()
        self.topi = ''        
        
    def _publish_identifier(self): #publica el identificador del dispositivo
        self.client.connect(self.server)
        self.client.publish(self.topic[0], self.payload)

    def on_message(self, client, userdata, message):#se ejecuta cada vez que detecta un nuevo mensaje
        self.topi = message.topic
        logging.info("TOPICO --> "f'{self.topi}')
        self.payl = message.payload.decode("utf8")
        logging.info("PAYLOAD --> "f'{self.payl}')
        self.cardDetected()
        print("********************************")
        
    def publish(self):
        self.client.connect(self.server)
        print("********************************")
        logging.info('TARJETA DETECTADA: ' f'{self.payl}' ' en ' f'{self.payload}')
        self.client.publish('tarjetaDetectada', self.topi)

    def cardDetected(self): #analiza si el topico del que viene el mensaje es usuariosDitesi
        if self.topi == self.topic[3]:
            self.publish()

    def subscriber(self): #se ejecuta una sola vez
        self.client.connect(self.server)
        logging.info("SUSCRIBIENDO A "f'{self.topic[3]}')
        logging.info("------------------")
        self.client.subscribe(self.topic[3])
        self.client.loop_forever()
    
if __name__ == '__main__':
    identifier = "lectorUnoDitesi"
    topic = ['devices', 'sistemaPrincipal', 'tarjetaDetectada', 'usuariosDitesi']
    #server = "127.0.0.1"
    server = "169.254.254.254"
    payload = identifier
    
    lectorDitesi = Lector(identifier, server, topic, payload)

    lectorDitesi.subscriber()
    lectorDitesi.publish()

