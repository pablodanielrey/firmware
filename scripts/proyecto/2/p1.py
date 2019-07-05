import paho.mqtt.client as mqtt
server="127.0.0.1" 


client = mqtt.Client("P1") 
client.connect(server) 
client.publish("lector01Ditesi","CERRADOOOOOO")