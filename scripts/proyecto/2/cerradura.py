import paho.mqtt.client as mqtt
from clases import Cerradura

identificador = "cerradura01Ditesi"
server = "127.0.0.1"
#server = "169.254.254.254"
topic = "lector01Ditesi/puertas"
cerraduraDitesi = Cerradura(identificador, server, topic)

cerraduraDitesi.subscriber()