import paho.mqtt.client as mqtt
import sys
import time
import logging
logging.getLogger().setLevel(logging.INFO)


client = mqtt.Client("Tarjeta")
card = sys.argv[1]
topic = 'usuariosDitesi'
server = "127.0.0.1"
#server = "169.254.254.254"
payload = card

def publish():
    client.connect(server)
    logging.info(f'{topic, payload}')
    client.publish(topic, payload)

publish()