
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// Update these with values suitable for your network.

const char* ssid = "...";
const char* password = "...";
const char* mqtt_server = "mqtt.econo.unlp.edu.ar";
const char *topic_eventos = "eventos";
const char *topic_comandos = "comandos";
const char *topic_registro = "registro";

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = 0;

void setup() {
  Serial.begin(9600);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void setup_wifi() {
  delay(10);
  
  Serial.println();
  Serial.print("Conectando a ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("conectado a WiFi");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("[");
  Serial.print(topic);
  Serial.print("]");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  if (strcmp(topic, "comandos") == 0) {
    client.publish("eventos","recibi un comando");
  }
}

char *ipToChar(IPAddress add) {
  char ip[20];
  int a = 0;
  while (a < 15) { 
    for (int i = 0; i < 3; i++) {
      ip[a+i] = add[i];
      a++;
    }
    ip[a] = '.';
    a++;
  }
  return ip;
}


void reconnect() {
  while (!client.connected()) {
    Serial.print("Conectando a MQTT");
    char *ip = ipToChar(WiFi.localIP());
    if (client.connect(ip)) {
      Serial.println("le aviso al arduino que ya estamos conectados");
      client.publish(topic_registro, ip);
      client.subscribe(topic_comandos);
    } else {
      Serial.print(client.state());
      delay(5000);
    }
  }
}

char buffer[100];

void loop() {

  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  while (Serial.available()) {
    String e = Serial.readString();
    e.toCharArray(buffer,100);
    client.publish(topic_eventos, buffer);
  }
  
}
