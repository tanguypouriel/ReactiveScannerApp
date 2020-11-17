#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

enum State { RIGHT, LEFT, STOP};
enum Actions { WANNA_GO_RIGHT, WANNA_GO_LEFT, WANNA_STOP, NO_ACTION };

typedef struct{
  int mspeed;
  State state;
} dataScanner;

volatile Actions action;
dataScanner mDataScanner{3, STOP};

const byte interruptPinStop = 1;
const byte interruptPinRight= 2;
const byte interruptPinLeft= 3;
const byte interruptPinSpeed = 4; //interrupt ? 

char messageReceived = '3';

void setup() {
  
  pinMode(interruptPinStop, INPUT);
  pinMode(interruptPinRight, INPUT);
  pinMode(interruptPinLeft, INPUT);
  pinMode(interruptPinSpeed, INPUT);

  action = NO_ACTION;

  attachInterrupt(interruptPinStop, stopScanner, RISING);
  attachInterrupt(interruptPinRight, rightScanner, RISING);
  attachInterrupt(interruptPinStop, leftScanner, RISING);

  SerialBT.begin("Scanner"); //Bluetooth device name
}

void loop() {

  switch(action){
    
    case WANNA_GO_RIGHT:
      SerialBT.write('A');
      // mettre moteur à droite
      mDataScanner.state = RIGHT;
      break;
      
    case WANNA_GO_LEFT:
      SerialBT.write('B');
      //mettre le moteur à gauche 
      mDataScanner.state = LEFT;
      break;
      
    case WANNA_STOP:
      SerialBT.write('C');
      //arreter le moteur
      mDataScanner.state = STOP;
      break;
      
  }

  if ( boutdeCourseDroit ) {
    SerialBT.write('D');
    //arreter le moteur
    mDataScanner.state = STOP;
  }

  if ( boutdeCourseGauche ) {
    SerialBT.write('E');
    //arreter le moteur
    mDataScanner.state = STOP;
  }

  if ( SerialBT.available() ) {
    
    messageReceived = (char) SerialBT.read();

    switch(messageReceived){

    case 'A': 
      //aller à droite
      mDataScanner.state = RIGHT;
      break;

    case 'B':
      //aller à gauche
      mDataScanner.state = LEFT;
      break;

    case 'C':
      //stoper
      mDataScanner.state = STOP;

    case '1':
      //passer la vitesse à 1
      mDataScanner.mspeed = 1;
      break;

    case '2':
      //passer la vitesse à 2
      mDataScanner.mspeed = 2;
      break;

    case '3':
      //passer la vitesse à 3
      mDataScanner.mspeed = 3;
      break;

    case '4':
      //passer la vitesse à 4
      mDataScanner.mspeed = 4;
      break;

    case '5':
      //passer la vitesse à 5
      mDataScanner.mspeed = 5;
      break;

    case '6':
      //passer la vitesse à 6
      mDataScanner.mspeed = 6;
      break;
      
    }
    
  }
  

}

void stopScanner(){
  action = WANNA_STOP;
}

void rightScanner(){
  action = WANNA_GO_RIGHT;
}

void leftScanner(){
  action = WANNA_GO_LEFT;
}
