import paho.mqtt.publish as publish
import json
import logging
import sys
logging.getLogger().setLevel(logging.INFO)


# oficina = {"oficina":{"persona":"estado"}
ditesi = {sys.argv[1]:"abrir"}

payload = json.dumps(ditesi)
servidor = '169.254.254.254'
topico = 'puerta/ditesi'


publish.single(topico, payload, hostname=servidor)
