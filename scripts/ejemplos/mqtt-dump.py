from datetime import date
import re
import paho.mqtt.subscribe as subscribe

import logging
logging.getLogger().setLevel(logging.INFO)

dispositivo = {}

topico = re.compile(r".*/(.*)/POWER")
prendido = re.compile(r"ON")
apagado = re.compile(r"OFF")


def on_mqtt(client, userdata, message):
    logging.info('-------------------------')
    logging.info(message.topic)
    logging.info(message.payload.decode('UTF-8'))
    logging.info('-------------------')

def suscribir():
    subscribe.callback(on_mqtt, "#", hostname="169.254.254.254")

if __name__ == '__main__':
    suscribir()
