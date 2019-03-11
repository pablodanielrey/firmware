import paho.mqtt.subscribe as subscribe
import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

dispositivos = {}

topico = re.compile(r".*/(.*)/RESULT")
estado = re.compile(r"\{\s*\"POWER\"\s*:\s*\"(.*)\"\s*}")

def on_mqtt(client, userdata, message):
    logging.info('------------------------------------------------------------')
    logging.info(message.topic)
    logging.info(message.payload.decode('UTF-8'))
    # logging.info(type(message.payload.decode('UTF-8')))
    a = message.payload.decode('UTF-8')
    x = json.loads(a)
    logging.info('******************')

    try:

        nombre = topico.match(message.topic)

    except Exception as ex1:
        logging.info(ex1)

    if nombre:
        try:
            logging.info('if nombre:')

            power = estado.match(message.payload.decode('UTF-8'))
            if power:
                try:
                    n = nombre.group(1)
                    p = power.group(1)
                except Exception as e3:
                    logging.exception(e3)

            if n not in dispositivos:
                logging.info('n NO existe en dispositivios:')
                dispositivos[n] = {}
                logging.info(dispositivos)


                if power:
                    logging.info('if power:')

                    logging.info(p)

                    dispositivos [n] = {'POWER': p}
                else:
                    logging.info(p)

            else:
                logging.info('n SI existe en dispositivios:')
                if power:
                    logging.info('if power:')
                    # p = power.group(1)

                    dispositivos [n] = {'POWER': p}


            logging.info(dispositivos)

        except Exception as ex:
            logging.info(ex)

topic = 'stat/#'
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
