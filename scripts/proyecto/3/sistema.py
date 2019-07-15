import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)

class Sistema:
    def __init__(self, identifier, server, topics, payload):
        self.client = mqtt.Client(identifier)
        self.server = server
        self.topics = topics
        self.identifier = identifier
        self.client.on_message = self.on_message
        self.card = True

    def on_message(self, client, userdata, message):
        logging.info("TOPICO --> "f'{self.topics}')
        self.payl = message.payload.decode("utf8")
        logging.info("Payload --> "f'{self.payl}')
        self.addtopics()
        logging.info("********************************")

    def subscriber(self):
        self.client.connect(self.server)
        for topic in self.topics:
            logging.info("SUSCRIBIENDO A topico  "f'{topic}')
            logging.info("---")
            self.client.subscribe(topic)
        self.client.loop_forever()

    
    def addtopics(self, devices):
        pass 
        
        #if self.payl not in self.topics:
        #    devices.append(self.payl)
        #    logging.info(f'{self.payl}'" Agregado a la lista de DISP/TOPICOS "f'{devices}')
        #else:
        #    logging.info("EL DISPOSITIVO/TOPICO YA EXISTE")
        

if __name__ == '__main__':
    identifier = 'sistemaPrincipal'
    devices = []
    #server = '127.0.0.1'
    server = "169.254.254.254"
    topics = ['devices', identifier,'tarjetaDetectada']
    payloads = {'puerta': ['abrir','cerrar'], 'luminaria': ['prender','apagar'] }

    sistemaPrincipal = Sistema(identifier, server, topics, payloads)

    sistemaPrincipal.subscriber()