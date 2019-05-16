# Para micropython
from umqttsimple import MQTTClient
def main(server="169.254.254.254"):
   c = MQTTClient("umqtt_client", server)
   c.connect()
   c.publish("topico/prueba", "Puerta abierta!")
   c.disconnect()
