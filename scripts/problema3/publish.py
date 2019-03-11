import paho.mqtt.publish as publish
import json
import logging
logging.getLogger().setLevel(logging.INFO)

dispositivo = {"POWER":"ON"}

logging.info(dispositivo)

servidor = '169.254.254.254'
payload = json.dumps(dispositivo)
topico = 'stat/XXXXXX/RESULT'

logging.info(payload)

publish.single(topico, payload, hostname=servidor)
