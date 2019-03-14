import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

Office = {}

dispositivos = {
    'jano':['janouno'],
    'pablo':['pablouno'],
    'ema':['pablodos']
}


topico_entrar_salir = ...
payload_entrar_salir = ...
topico_estado_sonoff = ...
payload_estado_sonoff = ...

def manejar_entrar_salir(persona, accion):
    """ controlar si la persona entro o salio y en base a eso """
    """ envia_mqtt_mensaje_apagar_prender """
    pass


def manejar_estado_sonoff(sonoff, estado):
    pass


def on_mqtt(client, userdata, message):
    try:

        manejar_entrar_salir()

        manejar_estado_sonoff()

    except Exception as ex:
        logging.exception(ex)

# topic = 'puerta/ditesi'
topic = []
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
