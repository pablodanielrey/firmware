import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

topicoprograma = re.compile(r"(.*)/(.*)") #ditesi/asistencia
payloadprograma = re.compile(r"(.*)") #ditesi = {sys.argv[1]:sys.argv[2]}

def on_mqtt(client, userdata, message):
    try:
        oficina = topicoprograma.match(message.topic)
        asistencia = payloadprograma.match(message.payload.decode('UTF-8'))
        personasAsistencia = asistencia.group(1)
        personas = json.loads(personasAsistencia)
        logging.info(oficina.group(1))
        logging.info(personas)
    except Exception as ex:
        logging.exception(ex)

topic = 'ditesi/asistencia'
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
