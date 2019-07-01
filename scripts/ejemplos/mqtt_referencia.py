
# publicar

import paho.mqtt.publish as publish

servidor = ''
pyload = ''
topico = ''
publish.single(topico, payload, hostname=servidor)




# para recibir

import paho.mqtt.subscribe as subscribe

def on_mqtt(client, userdata, message):
    message.topic
    message.payload
    pass

topico = ''
servidor = ''
subscribe.callback(on_mqtt, topico, hostname=servidor)


# manejo de estructura de datosself.

#diccionario se define:

dicc = {}

# agregar una clave al diccionario. valor puede ser cualquier cosaself.
# clave tiene que ser un string

dicc['clave'] = valor

# leer el valor de una calve desde un diccionario. queda dentro de la variable valor el contenido de la clave = calve

valor = dicc['calve']

# ejemplo de como realizar mas corto un acceso a diccionarios anidados:
#ej: original

dicc = {
            'clave1': {
                        'clave2': valor
                    }
        }

d = dicc['clave1']
valor = d['clave2']

#es lo mismo hacerlo asi:

valor = dicc['clave1']['clave2']


### estructura de dato:

if condicion:
    codigo a ejecutar si la condición es: True o no nula o no cero

if condicion != None:
    codigo



if condicion:
    codigo

codigo que se ejecutar siempre

# ejemplo de if completo

if condicion:
    codigo que se ejecuta si condicion cumple
elif condicion2:
    codigo que se ejecuta si condicion2 cumple
else:
    codigo que se ejecuta si condicion no cumple

codigo que se ejecuta siemple.

# en general es mas simple de verlo de esta forma:

if condicion:
    codigo que se ejecuta si condicion cumple

if condicion2:
    codigo que se ejecuta si condicion2 cumple


############ for
#para cada clave del diccionario, obtener los valores

for clave in dicc:
    valor = dicc[clave]
    proceso el valor (ej usando if)
