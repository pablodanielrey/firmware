import sys
from clases import Lector

# esta linea estaba de mas
#usuario = sys.argv[1]

identificador = "lector01Ditesi" #usado como topico
topicos = [identificador+'/puertas', identificador+'/luminarias']
server = "127.0.0.1"
#server = "169.254.254.254"
payloads = {'puerta': ['abrir','cerrar'], 'luminaria': ['prender','apagar'] }

lectorDitesi = Lector(identificador, server, topicos,payloads)

lectorDitesi.abrir()
lectorDitesi.prender()