import paho.mqtt.publish as publish

publish.single("cmnd/janouno/power", "TOGGLE", hostname="169.254.254.254")
publish.single("cmnd/pablouno/power", "TOGGLE", hostname="169.254.254.254")
publish.single("cmnd/pablodos/power", "TOGGLE", hostname="169.254.254.254")
