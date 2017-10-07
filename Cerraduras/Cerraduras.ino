

#include <SPI.h>
#include <MFRC522.h>
#include <EEPROM.h>



///////// instancia del reader ///////

#define RST_PIN 9
#define SS_PIN 10

MFRC522 mfrc522(SS_PIN, RST_PIN);

///////// variables del sistema ///////

#define RED_LED 0
#define GREEN_LED 1

int leds[2] = {PIND4,PIND3};
byte card[4] = {0,0,0,0};

byte memoria[4*10];


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
  int r = 1;
  for (int i = 0; i < 4; i++) {
    if (memoria[m + i] != card[i]) {
      return 0;
    }
  }
  return r;
}

int escribirTarjetaMaestra() {
  return escribirTarjeta(1);
}

int escribirTarjeta(int indice) {
  int m = indice * 4;
  for (int i = 0; i < 4; i++) {
    memoria[m + i] = card[i];
  }
  memoria[0] = memoria[0] + 1;
  return indice;
}

int agregarTarjeta() {
  int i = memoria[0] + 1;
  if ((i * 4) > (sizeof(memoria) - 4)) {
    Serial.println("No entran mas tarjetas en la memoria");
    return 0;
  }
  return escribirTarjeta(i);
}

int buscarTarjeta() {
  // retorna el indice de la tarjeta si la encuentra. y 0 en caso de no encontrarla
  for (int i = 1; i < memoria[0]; i++) {
    int indice = i * 4;
    for (int a = 0; a < 4; a++) {
      if (memoria[indice + a] != card[a]) {
        break;
      }
      return indice;
    }
  }
  return 0;
}

////////// inicializacion del sistema ////////////////////


void setup() {
  // inicializo el serie al pc.
  Serial.begin(9600);
  while (!Serial);


  Serial.println("Inicializando Leds");
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  for (int i = 0; i < 2; i++) {
    pinMode(leds[i], OUTPUT);
    digitalWrite(leds[i], HIGH);
  }
  digitalWrite(LED_BUILTIN, LOW);

  Serial.println("Inicializando Lector");
  SPI.begin();
  mfrc522.PCD_Init();
  mfrc522.PCD_DumpVersionToSerial();

  // inicializo el modelo del sistema
  enableProgramMode();
  while (!masterCardLoaded()) {
    Serial.println("Tarjeta maestra vacía");
    Serial.println("Ingrese Tarjeta Maestra");
    while (!readCard()) {
      delay(100);
    }
    escribirTarjetaMaestra();
  }
  disableProgramMode();
  lock();

}



void loop() {
  
  Serial.println("Esperando Tarjeta");

  lock();
  if (programMode) {
    enableProgramMode();
  }
  
  while (!readCard()) {
    delay(100);
  }
  printCard();

  if (chequearTarjetaMaestra()) {
  
    Serial.println("Tarjeta maestra ingresada");
    if (!programMode) {
      enableProgramMode();
    } else {
      disableProgramMode();
    }

  } else {

    if (!buscarTarjeta()) {

      if (programMode) {
        Serial.println("Programando Tarjeta");
        int i = agregarTarjeta();
        Serial.println(i);
      } else {
        Serial.println("Acceso DENEGADO");
        denyAccess();
      }
      
    } else {

      if (!programMode) {
        Serial.println("Desbloqueando Puerta");
        unlock();
      }
    }
    
  }
  delay(1000);

}
