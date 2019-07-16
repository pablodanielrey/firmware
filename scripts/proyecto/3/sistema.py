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
        #self.card = True

    def on_message(self, client, userdata, message):#se ejecuta cada vez que detecta un nuevo mensaje
        self.topi = message.topic
        logging.info("TOPICO --> "f'{self.topi}')
        self.payl = message.payload.decode("utf8")
        logging.info("Payload --> "f'{self.payl}')
        self._addtopics()
        print("********************************")

    def subscriber(self): #se ejecuta una sola vez
        self.client.connect(self.server)
        for topic in self.topics:
            logging.info("SUSCRIBIENDO A topico  "f'{topic}')
            logging.info("---")
            self.client.subscribe(topic)
        self.client.loop_forever()

    
    def _addtopics(self):
        if self.topi == self.topics[0]:
            if self.payl not in dispositivos:
                dispositivos.append(self.payl)
                logging.info("DISPOSITIVOS" f'{dispositivos}')
            else:
                logging.info("El dispositivo " f'{self.payl}' " ya existe")   
        
        

if __name__ == '__main__':
    identifier = 'sistemaPrincipal'
    dispositivos = []
    server = '127.0.0.1'
    #server = "169.254.254.254"
    topics = ['devices', identifier,'tarjetaDetectada']
    payloads = {'puerta': ['abrir','cerrar'], 'luminaria': ['prender','apagar'] }

    sistemaPrincipal = Sistema(identifier, server, topics, payloads)

    sistemaPrincipal.subscriber()