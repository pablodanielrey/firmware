import time
from umqttsimple import MQTTClient
import ubinascii
import machine
from machine import Pin
import micropython
import network
import esp
esp.osdebug(None)
import gc
gc.collect()

ssid = 'sistemas'
password = 'ditesisitedi'
mqtt_server = '169.254.254.254'
client_id = ubinascii.hexlify(machine.unique_id())
topic_sub = b'puerta/ditesi'
topic_pub = b''

pin = machine.Pin(2, machine.Pin.OUT) 

station = network.WLAN(network.STA_IF)

station.active(True)
station.connect(ssid, password)

while station.isconnected() == False:
  pass

print('Connection successful')
print(station.ifconfig())


def sub_cb(topic, msg):
  print((topic, msg))

def connect_and_subscribe():
  global client_id, mqtt_server, topic_sub
  client = MQTTClient(client_id, mqtt_server)
  client.set_callback(sub_cb)
  client.connect()
  client.subscribe(topic_sub)
  print('Connected to %s MQTT broker, subscribed to %s topic' % (mqtt_server, topic_sub))
  return client

def restart_and_reconnect():
  print('Failed to connect to MQTT broker. Reconnecting...')
  time.sleep(10)
  machine.reset()

try:
  client = connect_and_subscribe()
except OSError as e:
  restart_and_reconnect()

while True:
  try:
    new_message = client.check_msg()
    if new_message != 'None':
      client.publish(topic_pub, b'received')
      pin.value(0) 
    time.sleep(1)
  except OSError as e:
    restart_and_reconnect()