import paho.mqtt.publish as publish
import json
import logging
logging.getLogger().setLevel(logging.INFO)

dispositivo = {
    "Hola":"PEPE"
}
payload = json.dumps(dispositivo)

logging.info(dispositivo)

servidor = '169.254.254.254'
topico = 'stat/XXXX/topico'

logging.info(payload)

publish.single(topico, payload, hostname=servidor)
