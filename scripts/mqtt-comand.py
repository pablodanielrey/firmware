import paho.mqtt.publish as publish

publish.single("cmnd/sonoff/power", "ON", hostname="169.254.254.254")
