import sys
from clases import Lector

usuario = sys.argv[1]

identificador = "lector01Ditesi" #usado como topico
server = "127.0.0.1"
#server = "169.254.254.254"
payload = "Abrir"

lectorDitesi = Lector(identificador, server, payload)

lectorDitesi.publish()