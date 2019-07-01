import paho.mqtt.publish as publish
import logging

servidor = '169.254.254.254'
payload = '{"POWER":"ON"}'
topico = 'stat/XXXXXX/RESULT'

logging.info(payload)

publish.single(topico, payload, hostname=servidor)
