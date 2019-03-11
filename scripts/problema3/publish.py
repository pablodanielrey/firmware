import paho.mqtt.publish as publish
import json
import logging
logging.getLogger().setLevel(logging.INFO)

dispositivo = {
    'algo':'dato',
    'algo2':'dato2'
}
payload = json.dumps(dispositivo)

logging.info(dispositivo)

servidor = '169.254.254.254'
topico = 'ejemplo/XXXX/topico'

logging.info(payload)

publish.single(topico, payload, hostname=servidor)
