import paho.mqtt.subscribe as subscribe
import json
import logging
logging.getLogger().setLevel(logging.INFO)

def on_mqtt(client, userdata, message):
    # logging.info(message.topic)
    # logging.info(type(message.payload))
    logging.info(message.payload.decode('UTF-8'))
    # logging.info(type(message.payload.decode('UTF-8')))
    a = message.payload.decode('UTF-8')
    x = json.loads(a)
    logging.info(type(x))

topico = 'prueba/topico'
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topico, hostname=servidor)
