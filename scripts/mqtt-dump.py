from datetime import date
import re
import paho.mqtt.subscribe as subscribe

def get_filename_datetime():
    return "registro-" + str(date.today()) + ".txt"

archivo = get_filename_datetime()

salida=open(archivo, "a")

topico = re.compile(r".*/(.*)/POWER")


def on_mqtt(client, userdata, message):
    print('-------------------------')
    print(message.topic)
    print(message.payload)
    print('-------------------------')

    if topico.match(message.topic):
        print("ok")

""" m1 = re.match(r'.*/(.*)', str(message.topic))
    m2 = re.match(r'.*\'(.*)\'', str(message.payload))
    s1 = str(m1.group(1))
    s2 = str(m2.group(1))
    print('-->' + s1)
    print('-->' + s2)

    l1 = ('---'+ str(date.today())+ '---')
    salida.write(l1 + s1 + s2 )"""


def suscribir():
    subscribe.callback(on_mqtt, "stat/#", hostname="169.254.254.254")

if __name__ == '__main__':
    suscribir()
