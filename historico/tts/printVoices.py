
from espeak import espeak

if __name__ == '__main__':

    voices = espeak.list_voices()
    if len(voices) <= 0:
        sys.exit()

    for v in voices:
        print
        print 'id : %s' % v.identifier
        print 'nombre : %s' % v.name
        print 'genero : %s' % v.gender
        print 'edad : %s ' % v.age
        print 'lenguajes : %s' % v.variant
