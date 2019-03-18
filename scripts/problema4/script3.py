import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

import json
import re
import logging
logging.getLogger().setLevel(logging.INFO)

sonoffs = {}


dispositivos = {
    'jano':['janouno'],
    'pablo':['pablouno'],
    'ema':['pablodos']
}


topico_entrar_salir = re.compile(r"(.*)/asistencia")
topico_estado_sonoff = re.compile(r".*/(.*)/RESULT")
payload_estado_sonoff = re.compile(r"\{\s*\"POWER\"\s*:\s*\"(.*)\"\s*}")

def manejar_entrar_salir(persona, accion):
    logging.info('----Entrar / Salir----')
    """ controlar si la persona entro o salio y en base a eso """
    """ envia_mqtt_mensaje_apagar_prender """
    if accion:
        logging.info(f'{persona} entro a la oficina')
        if persona in dispositivos.keys():
            logging.info(f'{persona} tiene asignado un dispositivo')
            for sonoff in dispositivos[persona]:
                if sonoff not in sonoffs.keys():
                    publish.single(f"cmnd/{sonoff}/power", "ON", hostname="169.254.254.254")
                elif sonoffs[sonoff]:
                    publish.single(f"cmnd/{sonoff}/power", "ON", hostname="169.254.254.254")
        else:
            logging.info(f'{persona} NO tiene asignado un dispositivo')
    else:
        logging.info(f'{persona} salio de la oficina')
        if persona in dispositivos.keys():
            logging.info(f'{persona} tiene asignado un dispositivo')
            for sonoff in dispositivos[persona]:
                if sonoff in sonoffs.keys():
                    publish.single(f"cmnd/{sonoff}/power", "OFF", hostname="169.254.254.254")
                elif not sonoffs[sonoff]:
                    logging.info(f'{persona} tiene asignado un dispositivo')
                    publish.single(f"cmnd/{sonoff}/power", "OFF", hostname="169.254.254.254")

        else:
            logging.info(f'{persona} NO tiene asignado un dispositivo')

def guardar_estado_sonoff(sonoff, estado):
    sonoffs[sonoff] = estado

    logging.info(sonoffs)

    # publish.single(f"cmnd/{sonoff}/power", f"{estado}", hostname="169.254.254.254")


def on_mqtt(client, userdata, message):
    try:
        match_topico_entrar_salir = topico_entrar_salir.match(message.topic)
        match_topico_estado_sonoff = topico_estado_sonoff.match(message.topic)
        match_payload_estado_sonoff = payload_estado_sonoff.match(message.payload.decode('UTF-8'))

        if match_topico_entrar_salir:
            personal = message.payload
            personas = json.loads(personal)
            persona = list(personas.keys())[0]
            accion = personas[persona]
            manejar_entrar_salir(persona, accion)

        if match_topico_estado_sonoff:
            sonoff = match_topico_estado_sonoff.group(1)
            estado = match_payload_estado_sonoff.group(1)
            guardar_estado_sonoff(sonoff, estado)


    except Exception as ex:
        logging.exception(ex)

topic = ['ditesi/asistencia', 'stat/#']
servidor = '169.254.254.254'
subscribe.callback(on_mqtt, topic, hostname=servidor)
