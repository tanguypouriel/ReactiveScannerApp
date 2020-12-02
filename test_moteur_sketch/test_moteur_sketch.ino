const byte pinMotorPWM = 5;
const byte pinMotor1 = 17;
const byte pinMotor2 = 18;
const byte canalPWM0 = 0;


const int speedTab[] = {50, 91, 132, 173, 214, 255};

void setup() {

  pinMode(pinMotor1, OUTPUT);
  pinMode(pinMotor2, OUTPUT);
  pinMode(2, OUTPUT);

  

  
  ledcAttachPin(pinMotorPWM, canalPWM0); 
  ledcSetup(canalPWM0, 5000, 8);

}

void loop() {

  digitalWrite(pinMotor1, HIGH);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, 200);

  digitalWrite(2, HIGH);

  delay(1000);

    digitalWrite(2, HIGH);

  delay(1000);

 /* digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, HIGH);
  ledcWrite(canalPWM0, 200);

  digitalWrite(2, LOW);
  
  delay(5000);

  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, 0);

  digitalWrite(2, HIGH);

   delay(5000);*/


}
