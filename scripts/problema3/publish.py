import paho.mqtt.publish as publish

servidor = '169.254.254.254'
payload = 'hola'
topico = 'prueba/topico'

publish.single(topico, payload, hostname=servidor)
