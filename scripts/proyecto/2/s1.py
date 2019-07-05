import paho.mqtt.client as mqtt 
import time

server="127.0.0.1"
topic = "lector01Ditesi"
client = mqtt.Client("S1") 

def on_message(client, userdata, message):
    print("TOPICO ",message.topic)
    print("PAYLOAD " ,message.payload)


def subscriber():

    client.on_message=on_message 
    print("CONECTANDO CON EL SERVIDOR....")
    client.connect(server) 
    client.loop_start() 
    print("SUBSCRIBIENDO A TOPICO", topic)
    client.subscribe(topic)
    time.sleep(10)
    client.loop_stop()

subscriber()