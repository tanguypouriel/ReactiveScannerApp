const byte pinMotorPWM = 5;
const byte pinMotor1 = 32;
const byte pinMotor2 = 33;
const byte canalPWM0 = 0;


const int speedTab[] = {50, 91, 132, 173, 214, 255};

void setup() {

  ledcAttachPin(pinMotorPWM, canalPWM0); 
  ledcSetup(canalPWM0, 5000, 8);

}

void loop() {

  digitalWrite(pinMotor1, HIGH);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, 200);

  delay(5000);

  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, HIGH);
  ledcWrite(canalPWM0, 200);

  delay(5000);

  digitalWrite(pinMotor1, LOW);
  digitalWrite(pinMotor2, LOW);
  ledcWrite(canalPWM0, 0);

   delay(5000);


}
