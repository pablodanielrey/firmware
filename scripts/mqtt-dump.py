import paho.mqtt.subscribe as subscribe

def on_mqtt(client, userdata, message):
    print('-------------------------')
    print(message.topic)
    print(message.payload)
    print('-------------------------')

def suscribir():
    subscribe.callback(on_mqtt, "#", hostname="169.254.254.254")


if __name__ == '__main__':

    suscribir()
