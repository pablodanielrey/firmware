import paho.mqtt.publish as publish

publish.single("/cmnd/sonoff/POWER", "on", hostname="169.254.254.254")