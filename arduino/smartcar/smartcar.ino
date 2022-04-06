#include <Smartcar.h>

const int TRIGGER_PIN           = 6; // D6
const int ECHO_PIN              = 7; // D7
const unsigned int MAX_DISTANCE = 300; //set the distance to 300
const int fSpeed   = 70;  // 70% of the full speed forward
const int bSpeed   = -70; // 70% of the full speed backward
const int lDegrees = -75; // degrees to turn left
const int rDegrees = 75;  // degrees to turn right
bool flag = false;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
SimpleCar car(control);
SR04 front(arduinoRuntime, TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

void autoStop(){
  char input = Serial.read();
  const auto distance = front.getDistance();
  if(distance>0 and distance<150 and !flag){
    car.setSpeed(0);
   } 
  
  if(input == 'l'){
    flag == false;
    car.setSpeed(fSpeed);
    car.setAngle(lDegrees);  
  }else if(input == 'r'){
    flag = false;
    car.setSpeed(fSpeed);
    car.setAngle(rDegrees);
  }else if(input == 'f'){
    flag = false;
    car.setSpeed(fSpeed);
    car.setAngle(0);
  } else if(input == 'b'){
    flag = true;
    car.setSpeed(bSpeed);
    car.setAngle(0);
  }else if(input == 's'){
    flag = false;
    car.setSpeed(0);
    car.setAngle(0);
  }
 
}

void setup()
{
    Serial.begin(9600);
}

void loop()
{
  autoStop();
    Serial.println(front.getDistance());
    delay(100);
}
