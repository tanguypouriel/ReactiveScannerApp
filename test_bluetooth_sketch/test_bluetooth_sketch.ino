#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif


BluetoothSerial SerialBT;


void setup() {

  SerialBT.begin("Scanner"); //Bluetooth device name
  Serial.begin(115200);
  Serial.println("Device started");

}

void loop() {

  if (SerialBT.available()) {
    Serial.write(SerialBT.read());
  }

  delay(1000);
}
