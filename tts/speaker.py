import threading, Queue
from espeak import espeak

class SpeakerThread(threading.Thread):

    def __init__(self, wordsQueue):
        super(SpeakerThread,self).__init__()
        self.wordsQueue = wordsQueue
        self.finish = False

    def finish(v):
        print 'terminando thread'
        self.finish = v


    def run(self):
        espeak.set_voice('spanish-latin-am')
        espeak.set_parameter(espeak.Parameter.Rate,120)

        while not self.finish:
            try:
                phrase = self.wordsQueue.get(True,0.05)
                espeak.synth(phrase)

            except Queue.Empty:
                continue


        """
        self.enginetts = pyttsx.init()
        self.enginetts.setProperty('rate',140)
        self.enginetts.setProperty('voice','spanish-latin-am')

        self.enginetts.say('motor inicializado')
        self.enginetts.runAndWait()

        while True:
            try:
                phrase = self.wordsQueue.get(True,0.05)
                self.enginetts.say(phrase)
                self.enginetts.runAndWait()

            except Queue.Empty:
                continue
        """
