"""
    1 - enviar un comando usando un script y recibirlo en otro script
                codigo necesario de comunicación

    2 - enviar datos dentro del comando resuelto en el punto 1 e interpretar dentro del script que lo recibe
                formato de los datos enviados y convertirlos a bytes para poder enviarlos

    3 - recibir el estado del relee de los sonoff y almacenarlo en alguna estructura de datos (ej diccionario)
            ejecutar codigo dependiendo del topic que se obtiene. (IF)
            solucion de como almacenar los datos (diccionario)

    4 - recibir el comando de apagado,
        analizar en la estructura de datos   cuales reles estan prendidos,
        y enviar el mesanje mqtt para cada rele prendido para apagarlo

        se usa for e if
"""
