#include <PubSubClient.h>

#include <SoftwareSerial.h>

// software serial = RX, TX
SoftwareSerial myWifi(PIND6,PIND7);

int datos;

void setup() {
  Serial.begin(9600);
  while (!Serial);

  Serial.println("Inicializando SoftwareSerial D6 y D7");
  myWifi.begin(9600);
}

void loop() {
  
  if (myWifi.available()) {
    //Serial.println("wifi --> arduino");
    Serial.write(myWifi.read());
  }
  if (Serial.available()) {
    //USE Serial.println("arduino --> wifi");
    myWifi.write(Serial.read());
  }
}
