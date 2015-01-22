import threading, Queue, inject
from espeak import espeak
from config import Config

class SpeakerThread(threading.Thread):

    config = inject.attr(Config)

    def __init__(self, wordsQueue):
        super(SpeakerThread,self).__init__()
        self.wordsQueue = wordsQueue
        self._stop = threading.Event()

    def finish(self):
        print 'terminando thread'
        self._stop.set()


    def run(self):
        name = self.config.configs['espeak_name']
        rate = int(self.config.configs['espeak_rate'])

        espeak.set_voice(name)
        espeak.set_parameter(espeak.Parameter.Rate,rate)

        while not self._stop.isSet():
            try:
                phrase = self.wordsQueue.get(True,0.05)
                espeak.synth(phrase.encode('utf8'))

            except Queue.Empty:
                continue
