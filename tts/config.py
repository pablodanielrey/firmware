from os.path import expanduser
import ConfigParser

class Config:

    configs = {}

    def __init__(self):
        home = expanduser("~")
        cfg = home + '/' + 'tts-config.cfg'
        print "Reading config from : " + cfg

        config = ConfigParser.ConfigParser()
        config.read(cfg)
        for section in config.sections():
            options = config.options(section)
            for option in options:
                try:
                    self.configs[section + '_' + option] = config.get(section,option)
                except:
                    self.configs[section + '_' + option] = None

        print self.configs
