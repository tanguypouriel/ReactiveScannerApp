#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define pinA 15
#define pinB 2
#define pinC 4
#define pinD 16
#define pinE 17
#define pinF 5
#define pinG 18

const bool segTab[6][7] = {
  {1,0,0,1,1,1,1},
  {0,0,1,0,0,1,0},
  {0,0,0,0,1,1,0},
  {1,0,0,1,1,0,0},
  {0,1,0,0,1,0,0},
  {0,1,0,0,0,0,0}
};

BluetoothSerial SerialBT;

enum State { RIGHT, LEFT, STOP};
enum Actions { WANNA_GO_RIGHT, WANNA_GO_LEFT, WANNA_STOP, WANNA_SPEED_UP, WANNA_SPEED_DOWN, NO_ACTION };

typedef struct{
  int mSpeed;
  State state;
} dataScanner;

volatile Actions action;
dataScanner mDataScanner{3, STOP};

const byte interruptPinStop = 35; 
const byte interruptPinRight= 12;
const byte interruptPinLeft= 14;
//const byte interruptPinSpeedUp = 27;
//const byte interruptPinSpeedDown = 26;

const byte pinMotorPWM = 27;
const byte pinMotor1 = 32;
const byte pinMotor2 = 33;

const byte pinsDigit[] = { pinA, pinB, pinC, pinD, pinE, pinF, pinG};

const byte pinCurrent = 25; // OUTPUT pas initialisé
const byte pinBattery = 34; // OUTPUT pas initialisé

const byte canalPWM0 = 0;
 

char messageReceived = '3';

const int speedTab[] = {140, 160, 180, 205, 230, 255};

void setup() {
  
  pinMode(interruptPinStop, INPUT);
  pinMode(interruptPinRight, INPUT);
  pinMode(interruptPinLeft, INPUT);
//  pinMode(interruptPinSpeedUp, INPUT);
 // pinMode(interruptPinSpeedDown, INPUT);

  for (int i=0; i < 7 ; i++) {
    pinMode(pinsDigit[i], OUTPUT);
  }

  pinMode(pinMotor1, OUTPUT);
  pinMode(pinMotor2, OUTPUT);
  pinMode(pinMotorPWM, OUTPUT);

  /*pinMode(pinCurrent, INPUT);
  pinMode(pinBattery, INPUT);*/

  action = NO_ACTION;

  attachInterrupt(interruptPinStop, stopScanner, RISING);
 /* attachInterrupt(interruptPinRight, rightScanner, RISING);
  attachInterrupt(interruptPinLeft, leftScanner, RISING);
 // attachInterrupt(interruptPinSpeedUp, speedUp, RISING);
  attachInterrupt(interruptPinSpeedDown, speedDown, RISING);*/

  ledcAttachPin(pinMotorPWM, canalPWM0); 
  ledcSetup(canalPWM0, 5000, 8);

  Serial.begin(115200);
  SerialBT.begin("Scanner"); //Bluetooth device name
}

void loop() {

  /*switch(action){
    
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

    case WANNA_SPEED_UP:
    if(mDataScanner.mSpeed < 6){
      mDataScanner.mSpeed++;
      SerialBT.write(mDataScanner.mSpeed);
      displayDigit(mDataScanner.mSpeed);
    }
    break;

    case WANNA_SPEED_DOWN:
    if(mDataScanner.mSpeed > 1){
      mDataScanner.mSpeed--;
      SerialBT.write(mDataScanner.mSpeed);
      displayDigit(mDataScanner.mSpeed);
    }
    break;

    action = NO_ACTION;
  }*/

  /*if ( boutdeCourseDroit ) {
    if(mDataScanner.state == RIGHT){
      SerialBT.write('D');
    }else if(mDataScanner.state == LEFT){
      SerialBT.write('E');
    }
    motorStop();
    mDataScanner.state = STOP;
  }*/

  if ( action == WANNA_STOP ){
      SerialBT.write('C');
      motorStop();
      mDataScanner.state = STOP;
      action = NO_ACTION;
  }
  
  if ( SerialBT.available() ) {
    
    messageReceived = (char) SerialBT.read();

    Serial.write(messageReceived);

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
      
    }
    
  }

  Serial.print("state :");
  Serial.println(mDataScanner.state);
  Serial.print("speed :");
  Serial.println(mDataScanner.mSpeed);

  SerialBT.write('h');
  
  delay(500);
 
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

void stopScanner(){
  action = WANNA_STOP;
}

void rightScanner(){
  action = WANNA_GO_RIGHT;
}

void leftScanner(){
  action = WANNA_GO_LEFT;
}

void speedUp(){
  action = WANNA_SPEED_UP;
}

void speedDown(){
  action = WANNA_SPEED_DOWN;
}
