import paho.mqtt.publish as publish

publish.single("cmnd/sonoffs/power", "OFF", hostname="169.254.254.254")
# publish.single("cmnd/pablouno/power", "OFF", hostname="169.254.254.254")
# publish.single("cmnd/pablodos/power", "OFF", hostname="169.254.254.254")
