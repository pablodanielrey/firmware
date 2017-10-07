# -*- coding: utf-8 -*-
import signal, sys
import inject
import thread
import time
import datetime
import Queue

from config import Config
from speaker import SpeakerThread

''' el core de websockets '''
from Ws.SimpleWebSocketServer import SimpleWebSocketServer
from websocketServer import WebsocketServer


def config_injector(binder):
    pass

def serveWebsockets(server, *args):
    server.serveforever()

def getTheTime():
    return datetime.datetime.now().strftime("%H %M")

if __name__ == '__main__':

    phraseQueue = Queue.Queue()
#    speaker = SpeakerThread(phraseQueue)
#    speaker.start()

    inject.configure(config_injector)
    config = inject.instance(Config)

    ''' codigo de inicialización del servidor '''

    websocketServer = SimpleWebSocketServer(config.configs['server_ip'],int(config.configs['server_port']),WebsocketServer,phraseQueue)

    print('iniciando thread de reproduccion')
    speaker = SpeakerThread(phraseQueue)
    speaker.start()

    def close_sig_handler(signal,frame):
        print "se detecto fin"
        speaker.finish()
        websocketServer.close()
        sys.exit()

    print('configurando ctrl-c handler')
    signal.signal(signal.SIGINT,close_sig_handler)

    print('iniciando websocket')
    thread.start_new_thread(serveWebsockets,(websocketServer,1))

    while True:
        time.sleep(5000)



"""
    while True:
        try:
            phrase = phraseQueue.get(True,0.05)
            enginetts = pyttsx.init()
            enginetts.setProperty('rate',140)
            enginetts.setProperty('voice','spanish-latin-am')

            enginetts.say(phrase)
            enginetts.runAndWait()

            enginetts.stop()

            print 'Termine'


        except Queue.Empty:
            continue


#    while (True):
#        time.sleep(5000)
"""
