#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define pinA 2
#define pinB 4
#define pinC 16
#define pinD 17
#define pinE 5
#define pinF 18
#define pinG 19

#define VAL_SEUIL 3000   // should be between 0-4095

const bool segTab[6][7] = {
  {1,0,0,1,1,1,1},
  {0,0,1,0,0,1,0},
  {0,0,0,0,1,1,0},
  {1,0,0,1,1,0,0},
  {0,1,0,0,1,0,0},
  {0,1,0,0,0,0,0}
};

BluetoothSerial SerialBT;

enum State { RIGHT, LEFT, STOP, MAX_RIGHT, MAX_LEFT};
enum Actions { WANNA_GO_RIGHT, WANNA_GO_LEFT, WANNA_STOP, WANNA_SPEED_UP, WANNA_SPEED_DOWN, NO_ACTION };

typedef struct{
  int mSpeed;
  State state;
} dataScanner;

volatile Actions action;
dataScanner mDataScanner{3, STOP};

Actions lastAction;

const byte interruptPinStop = 35; 
const byte interruptPinRight= 12;
const byte interruptPinLeft= 14;
const byte interruptPinSpeedUp = 25;
const byte interruptPinSpeedDown = 26;

const byte pinMotorPWM = 27; //enable du pont en H
const byte pinMotor1 = 32;
const byte pinMotor2 = 33;

const byte pinsDigit[] = { pinA, pinB, pinC, pinD, pinE, pinF, pinG};

const byte pinBattery = 34; // OUTPUT pas initialisé

const byte canalPWM0 = 0;
 

char messageReceived = '3';

const int speedTab[] = {140, 160, 180, 205, 230, 255};

void setup() {
  
  pinMode(interruptPinStop, INPUT);
  pinMode(interruptPinRight, INPUT);
  pinMode(interruptPinLeft, INPUT);
  pinMode(interruptPinSpeedUp, INPUT);
  pinMode(interruptPinSpeedDown, INPUT);

  for (int i=0; i < 7 ; i++) {
    pinMode(pinsDigit[i], OUTPUT);
  }

  pinMode(pinMotor1, OUTPUT);
  pinMode(pinMotor2, OUTPUT);
  pinMode(pinMotorPWM, OUTPUT);

  //pinMode(pinBattery, INPUT);

  action = NO_ACTION;

  attachInterrupt(interruptPinStop, stopScanner, RISING);
  attachInterrupt(interruptPinRight, rightScanner, RISING);
  attachInterrupt(interruptPinLeft, leftScanner, RISING);
  attachInterrupt(interruptPinSpeedUp, speedUp, RISING);
  attachInterrupt(interruptPinSpeedDown, speedDown, RISING);

  ledcAttachPin(pinMotorPWM, canalPWM0); 
  ledcSetup(canalPWM0, 5000, 8);

  Serial.begin(115200);
  SerialBT.begin("Scanner"); //Bluetooth device name
}

void loop() {

  switch(action){
    
    case WANNA_GO_RIGHT:

      if ( lastAction != WANNA_GO_RIGHT ) {
        sendChar('A');
        motorGoRight();
        mDataScanner.state = RIGHT;
        lastAction = WANNA_GO_RIGHT;
      }

      break;
      
    case WANNA_GO_LEFT:

      if ( lastAction != WANNA_GO_LEFT ) {
        sendChar('B');
        motorGoLeft(); 
        mDataScanner.state = LEFT;
        lastAction = WANNA_GO_LEFT;
      }

      break;
      
    case WANNA_STOP:

      if (lastAction != WANNA_STOP) {
        sendChar('C');
        motorStop();
        mDataScanner.state = STOP;
        lastAction = WANNA_STOP;
      }

      break;

    case WANNA_SPEED_UP:
       
        if(mDataScanner.mSpeed < 6){
          mDataScanner.mSpeed++;
          sendChar(intToChar(mDataScanner.mSpeed));
          displayDigit(mDataScanner.mSpeed);
          
        }

    break;

    case WANNA_SPEED_DOWN:
        
        if(mDataScanner.mSpeed > 1){
          mDataScanner.mSpeed--;
          sendChar(intToChar(mDataScanner.mSpeed));
          displayDigit(mDataScanner.mSpeed);
        }
      
    break;

    action = NO_ACTION;
  }

  /*if ( analogRead(pinCurrent) > VAL_SEUIL ) {
    if(mDataScanner.state == RIGHT){
      sendChar('D');
      mDataScanner.state = MAX_RIGHT;
    }else if(mDataScanner.state == LEFT){
      sendChar('E');
      mDataScanner.state = MAX_LEFT;
    }
    motorStop();
  } */
  
  if ( SerialBT.available() ) {
    
    messageReceived = (char) SerialBT.read();

    Serial.write(messageReceived);

    switch(messageReceived){

    case 'A': 
      motorGoRight();
      mDataScanner.state = RIGHT;
      lastAction = WANNA_GO_RIGHT;
      break;

    case 'B':
      motorGoLeft();
      mDataScanner.state = LEFT;
      lastAction = WANNA_GO_LEFT;
      break;

    case 'C':
      motorStop();
      mDataScanner.state = STOP;
      lastAction = WANNA_STOP;

    case '1':
      mDataScanner.mSpeed = 1;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case '2':
      mDataScanner.mSpeed = 2;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case '3':
      mDataScanner.mSpeed = 3;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case '4':
      mDataScanner.mSpeed = 4;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case '5':
      mDataScanner.mSpeed = 5;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case '6':
      mDataScanner.mSpeed = 6;
      refreshSpeed();
      displayDigit(mDataScanner.mSpeed);
      break;

    case 'F': // demande de synchronisation
      sendChar(intToChar(mDataScanner.mSpeed));

      switch (mDataScanner.state) {
        case RIGHT:
          sendChar('A');
          break;

        case LEFT:
          sendChar('B');
          break;
          
        case STOP:
          sendChar('C');
          break;

        case MAX_RIGHT:
          sendChar('D');
          break;

        case MAX_LEFT:
          sendChar('E');
          break;
      }      
    }
    
  }

  Serial.print("state :");
  Serial.println(mDataScanner.state);
  Serial.print("speed :");
  Serial.println(mDataScanner.mSpeed);
  
  delay(20);
 
}

void motorGoRight(){
  
   if(mDataScanner.state != RIGHT){
        motorStop();
        delay(500);
   }
      
  digitalWrite(pinMotor1, HIGH);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, speedTab[mDataScanner.mSpeed-1]);
}

void motorGoLeft(){

  if(mDataScanner.state != LEFT){
        motorStop();
        delay(500);
  }
  
  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, HIGH);
  ledcWrite(canalPWM0, speedTab[mDataScanner.mSpeed-1]);
 
}

void motorStop(){
  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, 0);

}

void refreshSpeed() {

  if (mDataScanner.state == RIGHT){
    motorGoRight();
  } else if ( mDataScanner.state == LEFT) {
    motorGoLeft();
  }
}

void displayDigit(int mSpeed) {
  
  mSpeed--;
  
  for (int i = 0; i<7; i++){
    digitalWrite(pinsDigit[i], segTab[mSpeed][i]);
  }
}

void sendChar(char val){
  SerialBT.write(val);
  SerialBT.write('\r');
}

char intToChar(int val) {

  switch (val) {
    case 1:
      return '1';
      break;
    case 2:
      return '2';
      break;
    case 3:
      return '3';
      break;
    case 4:
      return '4';
      break;
    case 5:
      return '5';
      break;
    case 6:
      return '6';
      break;
  }
  
}

void stopScanner(){
  action = WANNA_STOP;
}

void rightScanner(){
  action = WANNA_GO_RIGHT;
  //delay(10);
}

void leftScanner(){
  action = WANNA_GO_LEFT;
  //delay(10);
}

void speedUp(){
  action = WANNA_SPEED_UP;
  //delay(10);
}

void speedDown(){
  action = WANNA_SPEED_DOWN;
 // delay(10);
}
