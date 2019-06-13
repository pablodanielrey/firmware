import connect
import cerradura

#aca pensar si falla el tema del a conexion wifi
def ejecutar():
    connect.conectar_wifi()
    cerradura.subscriber()