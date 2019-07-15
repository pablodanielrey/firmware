import paho.mqtt.client as mqtt
import time
import logging
logging.getLogger().setLevel(logging.INFO)

class Cerradura:
    def __init__(self, identificador, server, topic):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.identificador = identificador
        self.topic = topic
        self.client.on_message = self.on_message 

    def on_message(self, client, userdata, message):
        logging.info("DISPOSITIVO "f'{self.identificador}')
        logging.info("PAYLOAD "f'{message.payload.decode("utf8")}')
        logging.info("********************************")

    def subscriber(self):
        self.client.connect(self.server)
        #self.client.loop_start()
        logging.info("SUSCRIBIENDO A TOPICO  "f'{self.topic}')
        logging.info("-------------------------------")
        self.client.subscribe(self.topic)
        #time.sleep(10)
        #self.client.loop_stop()
        self.client.loop_forever()

if __name__ == '__main__':
    identificador = "cerradura01Ditesi"
    server = "127.0.0.1"
    #server = "169.254.254.254"
    topic = "lector01Ditesi/puertas"
    cerraduraDitesi = Cerradura(identificador, server, topic)

    cerraduraDitesi.subscriber()