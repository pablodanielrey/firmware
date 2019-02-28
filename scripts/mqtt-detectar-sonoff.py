"""
    guardar en un diccionario los sonoff que se prenden y la información
    asociada a ellos
    ip, hostname, etc
"""

from datetime import date
import re
import paho.mqtt.subscribe as subscribe

import logging
logging.getLogger().setLevel(logging.INFO)

dispositivo = {}

topico = re.compile(r".*/(.*)/POWER")
prendidos = [
    re.compile(r"ON"),
    re.compile(r"on")
]
apagados = [
    re.compile(r"OFF")
]




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
        for prendido in prendidos:
            if prendido.match(texto):
                dispositivo[nombre] = True
                break

        for apagado in apagados:
            if apagado.match(texto):
                dispositivo[nombre] = False
                break

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
