import time
import paho.mqtt.client as paho
broker="169.254.254.254"

def on_message(client, userdata, message):
    time.sleep(1)
    print("MENSAJE RECIBIDO =",str(message.payload.decode("utf-8")))

client= paho.Client("Cliente01") #create client object client1.on_publish = on_publish #assign function to callback client1.connect(broker,port) #establish connection client1.publish("house/bulb1","on")
######Bind function to callback
client.on_message=on_message
#####
print("Conectando ",broker)
client.connect(broker)#connect
client.loop_start() #start loop to process received messages
print("Subscribiendo ")
client.subscribe("puerta")#subscribe
time.sleep(2)
print("publicando ")
client.publish("puerta","abrir")#publish
time.sleep(1)
client.disconnect() #disconnect
client.loop_stop() #stop loop
