import paho.mqtt.client as mqtt
from clases import Luz

identificador = "luz01Ditesi"
server = "127.0.0.1"
#server = "169.254.254.254"
topic = "lector01Ditesi/luminarias"
luzDitesi = Luz(identificador, server, topic)

luzDitesi.subscriber()