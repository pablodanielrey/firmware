import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

dispositivos = {}

topico = re.compile(r".*/(.*)/RESULT")
estado = re.compile(r"\{\s*\"POWER\"\s*:\s*\"(.*)\"\s*}")

topicop = re.compile(r".*(mi-programa)")
estadop = re.compile(r"\{\s*\"(.*)\"\s*:\s*\"(.*)\"\s*}")



def on_mqtt(client, userdata, message):
    try:
        #logging.info('------------------------------------------------------------')
        #logging.info(message.topic)
        #logging.info(message.payload.decode('UTF-8'))
        # logging.info(type(message.payload.decode('UTF-8')))
        #logging.info('******************')
        nombre = topico.match(message.topic)
        if nombre:
            logging.info("SONOFF")
            power = estado.match(message.payload.decode('UTF-8'))
            n = nombre.group(1)
            if n not in dispositivos:
                dispositivos[n] = {}
            if power:
                p = power.group(1)
                if p == "ON":
                    dispositivos[n] = {'POWER': True}
                if p == "OFF":
                    dispositivos[n] = {'POWER': False}
            logging.info(dispositivos)

        nombrep = topicop.match(message.topic)
        if nombrep:
            logging.info("PROGRAMA")
            power = estadop.match(message.payload.decode('UTF-8'))
            np = nombrep.group(1)
            for n in dispositivos.keys():
                p = power.group(2)
                if p == 'OFF':
                    if dispositivos[n]['POWER'] == True:
                        # dispositivos[n]['POWER'] = False
                        publish.single(f"cmnd/{n}/POWER", "OFF", hostname="169.254.254.254")
                elif p == 'ON':
                    if dispositivos[n]['POWER'] == False:
                        # dispositivos[n]['POWER'] = True
                        publish.single(f"cmnd/{n}/POWER", "ON", hostname="169.254.254.254")



            logging.info(dispositivos)
            # if np not in dispositivos:
            #     dispositivos[np] = {}
            # if power:
            #     p = power.group(1)
            #     if p == "ON":
            #         dispositivos[np] = {'POWER': True}
            #     if p == "OFF":
            #         dispositivos[np] = {'POWER': False}
            #     logging.info(dispositivos)




    except Exception as ex:
        logging.exception(ex)

topic = 'stat/#'
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
