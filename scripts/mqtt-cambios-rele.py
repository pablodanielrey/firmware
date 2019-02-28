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
    # print('-------------------------')
    # print(message.topic)
    # print(message.payload.decode('UTF-8'))

    # print (prendido.match(message.payload))
    gnombre = topico.match(message.topic)
    if gnombre:
        logging.info('-------------------')
        nombre = gnombre.group(1)

        texto = message.payload.decode('UTF-8')
        if prendido.match(texto):
            dispositivo[nombre] = True
        elif apagado.match(texto):
            dispositivo[nombre] = False
        else:
            logging.warn('ERROR EN TEXTO')

        logging.info(dispositivo)
        logging.info('----------------')

    # on = prendido.match(message.payload.decode('UTF-8'))
    # off = apagado.match(message.payload.decode('UTF-8'))

    # if on:
    #     print("prendido")
    # else:
    #     if off:
    #         print("apagado")

def suscribir():
    subscribe.callback(on_mqtt, "stat/#", hostname="169.254.254.254")



if __name__ == '__main__':
    suscribir()
