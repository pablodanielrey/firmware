import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


class Lector:
    def __init__(self, identificador, server, topicos, payloads):
        self.client = mqtt.Client(identificador)
        self.server = server
        self.topic = topicos
        self.contenido = payloads

    def publish(self, dispositivo):
        self.client.connect(self.server)
        if dispositivo=='puerta':
            logging.info(f'{self.topic[0], self.payload}')
            self.client.publish(self.topic[0], self.payload)
        if dispositivo=='luminaria':
            logging.info(f'{self.topic[1], self.payload}')
            self.client.publish(self.topic[1], self.payload)

    def abrirPuerta(self): #hay  que mejorar esta funcion
        self.payload = self.contenido['puerta'][0]
        self.publish('puerta')
        time.sleep(3)
        self.payload = self.contenido['puerta'][1]
        self.publish('puerta')

    def prenderLuz(self): #hay  que mejorar esta funcion
        self.payload = self.contenido['luminaria'][0]
        self.publish('luminaria')



if __name__ == '__main__':
    identificador = "lector01Ditesi" #usado para el topico
    topicos = [identificador+'/puertas', identificador+'/luminarias']
    #server = "127.0.0.1"
    server = "169.254.254.254"
    payloads = {'puerta': ['abrir','cerrar'], 'luminaria': ['prender','apagar'] }

    lectorDitesi = Lector(identificador, server, topicos,payloads)

    lectorDitesi.abrirPuerta()
    lectorDitesi.prenderLuz()
