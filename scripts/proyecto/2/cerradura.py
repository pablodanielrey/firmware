import paho.mqtt.client as mqtt
from clases import Cerradura

identificador = "cerradura01Ditesi"
server = "169.254.254.254"
topic = "lector01Ditesi"
cerraduraDitesi = Cerradura(identificador, server, topic)

cerraduraDitesi.on_connect()