import paho.mqtt.publish as publish
import json
import logging
import sys
logging.getLogger().setLevel(logging.INFO)


publish.single("cmnd/janouno/POWER", "OFF", hostname="169.254.254.254")
