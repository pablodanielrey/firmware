import paho.mqtt.publish as publish
import json

dispositivo={"Estado":"Online", "Encendido":"OFF","IP":"16.16.16.16"}



servidor = '169.254.254.254'
payload = json.dumps(dispositivo)
topico = 'prueba/topico'

publish.single(topico, payload, hostname=servidor)
