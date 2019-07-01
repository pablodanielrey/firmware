import paho.mqtt.publish as publish
import json
import logging
import sys
logging.getLogger().setLevel(logging.INFO)

dispositivo = {
    "POWER":sys.argv[1]
}
payload = json.dumps(dispositivo)

servidor = '169.254.254.254'
topico = 'stat/mi-programa'


publish.single(topico, payload, hostname=servidor)
