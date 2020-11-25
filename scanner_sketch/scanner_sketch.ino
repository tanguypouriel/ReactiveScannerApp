#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

enum State { RIGHT, LEFT, STOP};
enum Actions { WANNA_GO_RIGHT, WANNA_GO_LEFT, WANNA_STOP, NO_ACTION };

typedef struct{
  int mSpeed;
  State state;
} dataScanner;

volatile Actions action;
dataScanner mDataScanner{3, STOP};

const byte interruptPinStop = 1;
const byte interruptPinRight= 2;
const byte interruptPinLeft= 3;
const byte interruptPinSpeed = 4; //interrupt ?

const byte pinMotorPWM = 5;
const byte pinMotor1 = 6;
const byte pinMotor2 = = 7;
 

char messageReceived = '3';

const int speedTab[] = {50, 91, 132, 173, 214, 255};

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
      motorGoRight();
      mDataScanner.state = RIGHT;
      break;
      
    case WANNA_GO_LEFT:
      SerialBT.write('B');
      motorGoLeft(); 
      mDataScanner.state = LEFT;
      break;
      
    case WANNA_STOP:
      SerialBT.write('C');
      motorStop();
      mDataScanner.state = STOP;
      break;
      
  }

  if ( boutdeCourseDroit ) {
    SerialBT.write('D');
    motorStop();
    mDataScanner.state = STOP;
  }

  if ( boutdeCourseGauche ) {
    SerialBT.write('E');
    motorStop();
    mDataScanner.state = STOP;
  }

  if ( SerialBT.available() ) {
    
    messageReceived = (char) SerialBT.read();

    switch(messageReceived){

    case 'A': 
      motorGoRight();
      mDataScanner.state = RIGHT;
      break;

    case 'B':
      motorGoLeft();
      mDataScanner.state = LEFT;
      break;

    case 'C':
      motorStop();
      mDataScanner.state = STOP;

    case '1':
      mDataScanner.mSpeed = 1;
      break;

    case '2':
      mDataScanner.mSpeed = 2;
      break;

    case '3':
      mDataScanner.mSpeed = 3;
      break;

    case '4':
      mDataScanner.mSpeed = 4;
      break;

    case '5':
      mDataScanner.mSpeed = 5;
      break;

    case '6':
      mDataScanner.mSpeed = 6;
      break;
      
    }
    
  }
  

}

private void motorGoRight(){
  digitalWrite(pinMotor1, HIGH);
  digitalWrite(pinMotor2, LOW);
  analogWrite(pinMotorPWM, speedTab[mDataScanner.mSpeed]);
  
}

private void motorGoLeft(){
  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, HIGH);
  analogWrite(pinMotorPWM, speedTab[mDataScanner.mSpeed]);
}

private void motorStop(){
  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, LOW);
  analogWrite(pinMotorPWM, 0);
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
