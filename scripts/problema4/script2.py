import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

Office = {}

topicoprograma = re.compile(r"puerta\/(.*)") #puerta/ditesi
payloadprograma = re.compile(r"\{\s*\"(.*)\"\s*:\s*\"(.*)\"\s*}") #ditesi = {sys.argv[1]:sys.argv[2]}

def manejar_cerradura(oficina, personaAccion):
    if oficina not in Office:
        logging.info(f'{oficina} no existe, la creo')
        Office[oficina] = {}
    if personaAccion:
        persona = personaAccion.group(1)
        accion = personaAccion.group(2)
        personasenoficina = Office[oficina]
        if persona not in personasenoficina:
            logging.info(f'{persona} no existe, lo agrego')
            personasenoficina[persona] = False
        if accion == 'abrir':
            if personasenoficina[persona] == True: #me fijo si esta adentro
                personasenoficina[persona] = False
                po = json.dumps(personasenoficina)
                publish.single(f"ditesi/asistencia", po, hostname="169.254.254.254")
            elif personasenoficina[persona] == False: #me fijo si esta afuera
                personasenoficina[persona] = True
                po = json.dumps(personasenoficina)
                publish.single(f"ditesi/asistencia", po, hostname="169.254.254.254")
    logging.info(Office)


def on_mqtt(client, userdata, message):
    try:

        cerradura = topicoprograma.match(message.topic)
        if cerradura:
            oficina = cerradura.group(1)
            personaAccion = payloadprograma.match(message.payload.decode('UTF-8'))
            manejar_cerradura(oficina, personaAccion)

    except Exception as ex:
        logging.exception(ex)

# topic = 'puerta/ditesi'
topic = "puerta/#"
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
