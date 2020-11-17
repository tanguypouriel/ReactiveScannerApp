#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

enum State { RIGHT, LEFT, STOP};
enum Actions { WANNA_GO_RIGHT, WANNA_GO_LEFT, WANNA_STOP, NO_ACTION };

typedef struct{
  int speed;
  State state;
} dataScanner;

volatile Actions action;

const byte InterruptPinStop;
const byte InterruptPinRight;
const byte InterruptPinLeft;
const byte InterruptPinSpeed; //interrupt ? 

char messageReceived = '3';

void setup() {
  
  pinMode(InterruptPinStop, INPUT);
  pinMode(InterruptPinRight, INPUT);
  pinMode(InterruptPinLeft, INPUT);
  pinMode(InterruptPinSpeed, INPUT);

  action = Actions.NO_ACTION;

  attachInterrupt(digitalPinToInterrupt(interruptPinStop), stopScanner(), RISING);
  attachInterrupt(digitalPinToInterrupt(interruptPinRight), rightScanner(), RISING);
  attachInterrupt(digitalPinToInterrupt(interruptPinStop), leftScanner(), RISING);

  dataScanner mDataScanner;
  mDataScanner.mspeed = 3;
  mDataScanner.state = State.STOP;

  SerialBT.begin("Scanner"); //Bluetooth device name
}

void loop() {

  switch(action){
    
    Actions.WANNA_GO_RIGHT:
      SerialBT.write('A');
      // mettre moteur à droite
      mDataScanner.state = State.RIGHT;
      break;
      
    Actions.WANNA_GO_LEFT:
      SerialBT.write('B');
      //mettre le moteur à gauche 
      mDataScanner.state = State.LEFT;
      break;
      
    Actions.WANNA_GO_STOP:
      SerialBT.write('C');
      //arreter le moteur
      mDataScanner.state = State.STOP;
      break;
      
    default:
      //ne rien faire
  }

  if ( boutdeCourseDroit ) {
    SerialBT.write('D');
    //arreter le moteur
    mDataScanner.state = State.STOP;
  }

  if ( boutdeCourseGauche ) {
    SerialBT.write('E');
    //arreter le moteur
    mDataScanner.state = State.STOP;
  }

  if ( SerialBT.available() ) {
    
    messageReceived = (char) SerialBT.read();

    switch(messageReceived){

    'A': 
      //aller à droite
      mDataScanner.state = State.RIGHT;
      break;

    'B':
      //aller à gauche
      mDataScanner.state = State.LEFT;
      break;

    'C':
      //stoper
      mDataScanner.state = State.STOP;

    '1':
      //passer la vitesse à 1
      mDataScanner.mspeed = 1;
      break;

    '2':
      //passer la vitesse à 2
      mDataScanner.mspeed = 2;
      break;

    '3':
      //passer la vitesse à 3
      mDataScanner.mspeed = 3;
      break;

    '4':
      //passer la vitesse à 4
      mDataScanner.mspeed = 4;
      break;

    '5':
      //passer la vitesse à 5
      mDataScanner.mspeed = 5;
      break;


    '6':
      //passer la vitesse à 6
      mDataScanner.mspeed = 6;
      break;

    default: 
      //erreur
      
    }
    
  }
  

}

void stopScanner(){
  action = Actions.WANNA_STOP;
}

void rightScanner(){
  action = Actions.WANNA_GO_RIGHT;
}

void leftScanner(){
  action = Actions.WANNA_GO_LEFT;
}
