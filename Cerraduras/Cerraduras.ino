
#include <SoftwareSerial.h>
#include <WiFiEspClient.h>
#include <WiFiEsp.h>
#include <PubSubClient.h>

#include <SPI.h>
#include <MFRC522.h>
#include <EEPROM.h>

///////// instancia del reader ///////

#define RST_PIN 9
#define SS_PIN 10

MFRC522 mfrc522(SS_PIN, RST_PIN);


///////// WIFI ESP8266 //////////

#define WIFI_AP "decirapidopachu"
#define WIFI_PASSWORD "petepetepopopopo"
#define MQTT_SERVER "mqtt.econo.unlp.edu.ar"

WiFiEspClient espClient;
PubSubClient client(espClient);
SoftwareSerial soft(PIND6, PIND7); // RX, TX
char buffer[100];
boolean wifiEnabled = false;
unsigned long lastSend;


///////// variables del sistema ///////

#define RED_LED 0
#define GREEN_LED 1

int leds[2] = {PIND4,PIND3};


/////////////// tarjetas ///////////////////

#define MAXTARJETAS 5
byte card[4] = {0,0,0,0};
byte memoria[4*MAXTARJETAS];
boolean programMode = false;

int readCard() {
  if (!mfrc522.PICC_IsNewCardPresent()) {
    return 0;
  }
  if (!mfrc522.PICC_ReadCardSerial()) {
    return 0;
  }
  for (int i = 0; i < 4; i++) {
    card[i] = mfrc522.uid.uidByte[i];
  }
  mfrc522.PICC_HaltA();
  return 1;
}


void unlock() {
  digitalWrite(leds[RED_LED],LOW);
  digitalWrite(leds[GREEN_LED],HIGH);
}

void lock() {
  digitalWrite(leds[RED_LED],HIGH);
  digitalWrite(leds[GREEN_LED],LOW);
}

void denyAccess() {
  for (int i = 0; i < 5; i++) {
    digitalWrite(leds[RED_LED],HIGH); 
    delay(100);
    digitalWrite(leds[RED_LED],LOW); 
    delay(100);
  }
}

void enableProgramMode() {
  for (int i = 0; i < 5; i++) {
    digitalWrite(leds[RED_LED],HIGH); 
    digitalWrite(leds[GREEN_LED],HIGH);
    delay(200);
    digitalWrite(leds[RED_LED],LOW); 
    digitalWrite(leds[GREEN_LED],LOW);    
    delay(200);
  }
  digitalWrite(leds[RED_LED],HIGH); 
  digitalWrite(leds[GREEN_LED],HIGH);
  programMode = true;
}

void disableProgramMode() {
  digitalWrite(leds[RED_LED],LOW); 
  digitalWrite(leds[GREEN_LED],LOW);
  programMode = false;
}

void printCard() {
  Serial.print("Uid: ");
  for (int i = 0; i < 4; i++) {
    Serial.print(card[i]);
  }
  Serial.println();
}

int masterCardLoaded() {
  if (memoria[0] > 0) {
    return 1;
  }
  return 0;
}

int chequearTarjetaMaestra() {
  int m = 1 * 4;
  for (int i = 0; i < 4; i++) {
    if (memoria[m + i] != card[i]) {
      return 0;
    }
  }
  return 1;
}

int escribirTarjetaMaestra() {
  return escribirTarjeta(1);
}

int escribirTarjeta(int indice) {
  if (indice > MAXTARJETAS - 2) {
    return 0;    
  }
  int m = indice * 4;
  for (int i = 0; i < 4; i++) {
    memoria[m + i] = card[i];
  }
  memoria[0] = memoria[0] + 1;
  return indice;
}

int eliminarTarjeta(int indice) {
  if (indice > MAXTARJETAS - 2) {
    return 0;    
  }
  int m = indice * 4;
  for (int i = 0; i < 4; i++) {
    memoria[m + i] = 0;
  }
  memoria[0] = memoria[0] - 1;
  return indice;
}


int buscarLugarVacio() {
  for (int i = 2; i < MAXTARJETAS - 2; i++) {
    int base = i * 4;
    boolean encontrado = true;
    for (int a = 0; a < 4; a++) {
      if (memoria[base + a] != 0) {
        encontrado = false;
        break;
      }
    }
    if (encontrado) {
      return i;
    }
  }
  return 0;
}

int agregarTarjeta() {
  int i = buscarLugarVacio();
  if (!i) {
    Serial.println("No entran mas tarjetas en la memoria");
    return 0;
  }
  return escribirTarjeta(i);
}

int buscarTarjeta() {
  for (int i = 1; i < MAXTARJETAS - 2; i++) {
    boolean encontrada = true;
    int indice = i * 4;
    for (int a = 0; a < 4; a++) {
      if (memoria[indice + a] != card[a]) {
        encontrada = false;
        break;
      }
    }
    if (encontrada) {
      return indice;
    }
  }
  return 0;
}

void limpiarTarjeta() {
  for (int i = 0; i < 4; i++) {
    card[i] = 0;
  }
}

int esperarTarjeta() {
  return readCard();
}


/////////// inicializacion wifi ///////////////////////

void initWifi() {
  Serial.println("Configurando servidor mqtt");
  client.setServer(MQTT_SERVER, 1883);
  
  Serial.println("Inicializando WiFi");
  WiFi.init(&soft);
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("No se encuentra módulo WiFi");
    return;
  }
  wifiEnabled = true;
  
}

void reconnect() {
  if (!wifiEnabled) {
    return;
  }
  if (WiFi.status() != WL_CONNECTED) {
    while (WiFi.status() != WL_CONNECTED) {
      Serial.print("Intentando conectar a : ");
      Serial.println(WIFI_AP);
      WiFi.begin(WIFI_AP, WIFI_PASSWORD);
      delay(500);
    }
    Serial.println("Conectado");
  }
  if (!client.connected()) {
    while (!client.connected()) {
      Serial.println("Conectando mqtt a servidor");
      if (client.connect("id-cliente", NULL, NULL)) {
        Serial.println("Conectado!!");
      } else {
        Serial.print("Error : ");
        Serial.println(client.state());
        delay(5000);
      }
    }
  }
  lastSend = 0;
}

void enviarPorWifi(String datos) {
  datos.toCharArray(buffer, 100);
  client.publish("arduino", buffer);
  Serial.print("mqtt: c --> s : ");
  Serial.println(buffer);  
}

void enviarMensaje(String m) {
  String d = "{\"c\":\"arduino\",\"d\":\"" + m + "\"}";
  if (wifiEnabled) {
    enviarPorWifi(d);
    client.loop();
  } else {
    Serial.println(d);
  }
}

////////// inicializacion del sistema ////////////////////

void setup() {
  // inicializo el serie al pc.
  Serial.begin(115200);
  while (!Serial);

  Serial.println("Inicializando Leds");
  for (int i = 0; i < 2; i++) {
    pinMode(leds[i], OUTPUT);
    digitalWrite(leds[i], HIGH);
  }

  Serial.println("Inicializando comunicación con Wifi ESP8266");
  soft.begin(9600);
  while (!wifiEnabled) {
    initWifi();
  }
  
  Serial.println("Inicializando Lector");
  SPI.begin();
  mfrc522.PCD_Init();
  mfrc522.PCD_DumpVersionToSerial();

  // inicializo el modelo del sistema
  enableProgramMode();
  while (!masterCardLoaded()) {
    Serial.println("-----------------------------------");
    Serial.println("Ingrese Tarjeta Maestra");
    while (!esperarTarjeta()) {
      delay(1000);
    }
    Serial.println("Registrando Tarjeta");
    escribirTarjetaMaestra();
    Serial.println("-----------------------------------");
  }
  disableProgramMode();
  lock();

}

void loop() {

  if (programMode) {

    if (esperarTarjeta()) {
      if (chequearTarjetaMaestra()) {
        enviarMensaje("Tarjeta Maestra - Se desactiva el modo programación");
        disableProgramMode();
        
      } else {
        enviarMensaje("Tarjeta Detectada");
        int i = buscarTarjeta();
        if (i > 1) {
          eliminarTarjeta(i);
          enviarMensaje("Se elimina la tarjeta : " + i);
          
        } else {
          i = agregarTarjeta();
          enviarMensaje("Se agrego la tarjeta : " + i);
        }
      }
    }
    
  } else {

    //enviarMensaje("Esperando Tarjeta");
    if (esperarTarjeta()) {
      printCard();
      
      if (chequearTarjetaMaestra()) {
        enviarMensaje("Tarjeta Maestra - Se activa el modo programación");
        enableProgramMode();
        
      } else {
        enviarMensaje("Tarjeta Detectada");
        int i = buscarTarjeta();
        if (i > 1) {
          enviarMensaje("Tarjeta registrada - Acceso Concedido - " + i);
          enviarMensaje("Desbloqueando puerta");
          unlock();
          
          delay(1000);
          
          enviarMensaje("Bloqueando puerta");
          lock();
          
        } else {
          enviarMensaje("Tarjeta no registrada - Acceso Denegado");
          denyAccess();
        }
      }
    }
   
  }

  // limpio el buffer
  limpiarTarjeta();

  if (wifiEnabled) {
    reconnect();
    client.loop();
  }
}
