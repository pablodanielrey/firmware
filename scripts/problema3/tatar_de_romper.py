import paho.mqtt.publish as publish
import logging
logging.getLogger().setLevel(logging.INFO)

servidor = '169.254.254.254'

mensajes = {
    'stat/zxxx/RESULT': [
        'On',
        'off',
        '{"POWER":"ON"}'
    ],
    'stat/XXXXXXRESULT': [
        'algo',
        '{"POWER":"ON"}'
    ]
}


for topico in mensajes.keys():
    lista = mensajes[topico]
    for payload in lista:
        logging.info('{} <-- {}'.format(topico, payload))
        publish.single(topico, payload, hostname=servidor)
