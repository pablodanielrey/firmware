import paho.mqtt.subscribe as subscribe

import logging
logging.getLogger().setLevel(logging.INFO)

def on_mqtt(client, userdata, message):
    logging.info(message.topic)
    logging.info(message.payload.decode('UTF-8'))
    pass

topico = 'prueba/topico'
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topico, hostname=servidor)
