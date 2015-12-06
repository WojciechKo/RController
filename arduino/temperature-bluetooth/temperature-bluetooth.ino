#include <SoftwareSerial.h>

#include <dht.h>

dht DHT;

#define higrometrPin 5

#define bluetoothTx 2
#define bluetoothRx 3

SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);

void setup() {
  Serial.begin (9600);
  bluetooth.begin(9600);
}

void loop() {
  Serial.flush();
  unsigned long time = millis();
  
  int humidity;
  int temperature;
  long distance;

  // Read higrometr
  switch (DHT.read11(higrometrPin)) {
    case DHTLIB_OK:  
    break;
    case DHTLIB_ERROR_CHECKSUM: 
    Serial.print("Checksum error,\t"); 
    break;
    case DHTLIB_ERROR_TIMEOUT: 
    Serial.print("Time out error,\t"); 
    break;
    default: 
    Serial.print("Unknown error,\t"); 
    break;
  }
  humidity = DHT.humidity;
  temperature = DHT.temperature;

  // Print data
  Serial.print("{\"humidity\": \"");
  Serial.print(humidity, 1);
  Serial.print("%\", \"temp\": \"");
  Serial.print(temperature, 1);
  Serial.print("C\"}\n");
  
  bluetooth.print("{\"humidity\": \"");
  bluetooth.print(humidity, 1);
  bluetooth.print("%\", \"temp\": \"");
  bluetooth.print(temperature, 1);
  bluetooth.print("C\"}\n");
  delay(2000);
}


