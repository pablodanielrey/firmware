import paho.mqtt.client as mqtt

client= mqtt.Client()
client.subscribe("lector01Ditesi")

def on_message(client, userdata, msg):
    print(msg.topic + " " + str(msg.payload))

client = mqtt.Client()
client.on_message = on_message
client.connect("169.254.254.254", 1883, 60)

client.loop_forever()