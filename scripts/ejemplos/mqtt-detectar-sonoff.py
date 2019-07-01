from datetime import date
import re
import paho.mqtt.subscribe as subscribe
import json
import logging
logging.getLogger().setLevel(logging.INFO)


dispositivo = {}

topico1 = re.compile(r".*/(.*)/INFO2")
topico2 = re.compile(r".*/(.*)/LWT")


def parsear(info):
    p = json.loads(info)
    return p

def on_mqtt(client, userdata, message):
    logging.info('----------------')
    logging.info(message.topic)
    logging.info(message.payload.decode('UTF-8'))
    logging.info('----------------')

    nombre = topico1.match(message.topic)
    if nombre:
        recibidos = parsear(message.payload.decode('UTF-8'))
        hostname = recibidos['Hostname']
        ip = recibidos['IPAddress']

        nomb = nombre.group(1)

        if nomb not in dispositivo:
            dispositivo[nomb] = {}

        datos = dispositivo[nomb]
        datos['Hostname'] = hostname
        datos['IPAddress'] = ip




    lwt = topico2.match(message.topic)
    if lwt:
        l = lwt.group(1)
        if l not in dispositivo:
            dispositivo[l] = {}
        datos = dispositivo[l]
        datos['Estado'] =  message.payload.decode('UTF-8')


    logging.info(dispositivo)
    logging.info(datos)
    logging.info('**************')




    # if True:
    #     """ topico 1 """
    #     """ guardamos los datos  """
    #     datos['nombre'] = hostname
    #     datos['ip'] = ip
    #
    # if True:
    #     """ topico 2 """
    #     datos['ap'] = hostname
    #     datos['wifi'] = ip
    #
    # if True:
    #     """ topico 3 """
    #     datos['ip'] = hostname
    #     datos['dato'] = ip
    #
    # st = json.dumps(datos)
    # with open('/tmp/datos.json','w') as f:
    #     f.write(st)



def suscribir():
    subscribe.callback(on_mqtt, "tele/#", hostname="169.254.254.254")



if __name__ == '__main__':
    suscribir()
