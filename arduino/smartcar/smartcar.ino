#include <vector>
#include <string>
#include <Smartcar.h>
#include <MQTT.h>
#include <WiFi.h>
#ifdef __SMCE__
#include <OV767X.h>
#endif

WiFiClient net;

MQTTClient mqtt;

char ssid[] = " ";
char pass[] = " ";

const auto oneSecond = 1000UL;
const int TRIGGER_PIN           = 6; // D6
const int ECHO_PIN              = 7; // D7
const unsigned int MAX_DISTANCE = 300; //set the distance to 300
int fSpeed = 70;  // 70% of the full speed forward
int bSpeed   = -70; // 70% of the full speed backward
const int lDegrees = -75; // degrees to turn left
const int rDegrees = 75;  // degrees to turn right
bool flag = false;
const int GYROSCOPE_OFFSET = 37;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
unsigned int pulsesPerMeter = 600;
 
DirectionlessOdometer leftOdometer{ arduinoRuntime,
                                    smartcarlib::pins::v2::leftOdometerPin,
                                    []() { leftOdometer.update(); },
                                    pulsesPerMeter };
DirectionlessOdometer rightOdometer{ arduinoRuntime,
                                     smartcarlib::pins::v2::rightOdometerPin,
                                     []() { rightOdometer.update(); },
                                     pulsesPerMeter };
                                     
GY50 gyroscope(arduinoRuntime, GYROSCOPE_OFFSET);
SmartCar car(arduinoRuntime, control, gyroscope, leftOdometer, rightOdometer);
SR04 front(arduinoRuntime, TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
std::vector<char> frameBuffer;

void autoStop(String message){
  //car.disableCruiseControl();
  const auto distance = front.getDistance();
  if(distance>0 and distance<110 and !flag){
    car.setSpeed(0);
   } 
  if(message == "l"){
    flag == false;
    car.setSpeed(fSpeed);
    car.setAngle(lDegrees);  
  }else if(message == "r"){
    flag = false;
    car.setSpeed(fSpeed);
    car.setAngle(rDegrees);
  }else if(message == "f"){
    flag = false;
    car.setSpeed(fSpeed);
    car.setAngle(0);
  } else if(message == "b"){
    flag = true;
    car.setSpeed(bSpeed);
    car.setAngle(0);
  }else if(message == "s"){
    flag = false;
    car.setSpeed(0);
    car.setAngle(0);
  }
}

void voiceCommands(String message, boolean hasDistance){
  if(message == "l"){
    go(1, fSpeed);
    rotate(-65,fSpeed);
  if(!hasDistance){
      car.setSpeed(fSpeed);
      car.setAngle(0); 
    }else{
      return;
    }
  }else if(message == "r"){
    go(1, fSpeed);
    rotate(65,fSpeed);
    if(!hasDistance){
      car.setSpeed(fSpeed);
      car.setAngle(0); 
    }else{
      return;
    }
  }else if(message == "f"){
    if(!hasDistance){
      car.setSpeed(fSpeed);
      car.setAngle(0);
    }else{
      return;
    }
  }else if(message == "b"){
    car.setSpeed(bSpeed);
    car.setAngle(0);
  }else if(message == "s"){
    car.setSpeed(0);
    car.setAngle(0);
  }
}

const auto counter = 0;
const auto mqttBrokerUrl = "127.0.0.1";

void setup()
{
    Serial.begin(9600);
    
  #ifdef __SMCE__
  Camera.begin(QVGA, RGB888, 15);
  frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
  #endif
  
  WiFi.begin(ssid, pass); 
  mqtt.begin(mqttBrokerUrl, 1883, net);
   

Serial.println("Connecting to MQTT broker");
  while (!mqtt.connect("arduino", "public", "public")) {
    Serial.print(".");
    delay(1000);
  }
  mqtt.subscribe("myfirst/test", 1);
  mqtt.subscribe("smartcar/fspeed", 1);
  mqtt.subscribe("smartcar/autodrive", 1);
  mqtt.subscribe("smartcar/voice", 1);
  mqtt.onMessage([](String topic, String message){
    
    if(topic=="myfirst/test"){
      autoStop(message);
    }else if(topic=="smartcar/fspeed"){
       fSpeed = message.toInt();
       if(car.getSpeed()>0){
       car.setSpeed(fSpeed);
       }
    }else if(topic == "smartcar/voice"){
      int centimeters = message.toInt();
      //if we have a distance
      if (centimeters > 0){
        voiceCommands(message,true);
        go(centimeters,fSpeed);
      }else{
        voiceCommands(message,false);
      }
      
    }
  });
}


/**
   Makes the car travel at the specified distance with a certain speed
   @param centimeters   How far to travel in centimeters, positive for
                        forward and negative values for backward
   @param speed         The speed to travel
*/
void go(long centimeters, int speed)
{
    if (centimeters == 0)
    {
        return;
    }
    // Ensure the speed is towards the correct direction
    speed = smartcarlib::utils::getAbsolute(speed) * ((centimeters < 0) ? -1 : 1);
    car.setAngle(0);
    car.setSpeed(speed);
 
    long initialDistance          = car.getDistance();
    bool hasReachedTargetDistance = false;
    while (!hasReachedTargetDistance)
    {
        car.update();
        auto currentDistance   = car.getDistance();
        auto travelledDistance = initialDistance > currentDistance ? initialDistance - currentDistance : currentDistance - initialDistance;
        hasReachedTargetDistance = travelledDistance >= smartcarlib::utils::getAbsolute(centimeters);
    }
    car.setSpeed(0);
}

/**
   Rotate the car at the specified degrees with the certain speed
   @param degrees   The degrees to turn. Positive values for clockwise
                    negative for counter-clockwise.
   @param speed     The speed to turn
*/
void rotate(int degrees, int speed)
{
    speed = smartcarlib::utils::getAbsolute(speed);
    degrees %= 360; // Put degrees in a (-360,360) scale
    if (degrees == 0)
    {
        return;
    }
 
    car.setSpeed(speed);
    if (degrees > 0)
    {
        car.setAngle(90);
    }
    else
    {
        car.setAngle(-90);
    }
 
    const auto initialHeading    = car.getHeading();
    bool hasReachedTargetDegrees = false;
    while (!hasReachedTargetDegrees)
    {
        car.update();
        auto currentHeading = car.getHeading();
        if (degrees < 0 && currentHeading < initialHeading)
        {
            // If we are turning left and the current heading is larger than the
            // initial one (e.g. started at 10 degrees and now we are at 350), we need to substract
            // 360 so to eventually get a signed displacement from the initial heading (-20)
            currentHeading -= 360;
        }
        else if (degrees > 0 && currentHeading > initialHeading)
        {
            // If we are turning right and the heading is smaller than the
            // initial one (e.g. started at 350 degrees and now we are at 20), so to get a signed
            // displacement (+30)
            currentHeading += 360;
        }
        // Degrees turned so far is initial heading minus current (initial heading
        // is at least 0 and at most 360. To handle the "edge" cases we substracted or added 360 to
        // currentHeading)
        int degreesTurnedSoFar  = initialHeading - currentHeading;
        hasReachedTargetDegrees = smartcarlib::utils::getAbsolute(degreesTurnedSoFar)
                                  >= smartcarlib::utils::getAbsolute(degrees);
    }
 
    car.setSpeed(0);
}


void loop()
{
    if (mqtt.connected()) {
    mqtt.loop();
    autoStop("");
    const auto currentTime = millis();
#ifdef __SMCE__
    static auto previousFrame = 0UL;
    if (currentTime - previousFrame >= 33) {
      previousFrame = currentTime;
      Camera.readFrame(frameBuffer.data());
      mqtt.publish("/smartcar/camera", frameBuffer.data(), frameBuffer.size(),
                   false, 0);
      const auto avgOdometerSpeed = String(car.getSpeed());             
      const auto defaultSpeed = String(fSpeed);
      const auto travelledDistance = String(car.getDistance());
      mqtt.publish("/smartcar/speedometer",  avgOdometerSpeed);
      mqtt.publish("/smartcar/defaultSpeed", defaultSpeed);
      mqtt.publish( "/smartcar/travelledDistance", travelledDistance);             
    }
#endif
    static auto previousTransmission = 0UL;
    if (currentTime - previousTransmission >= oneSecond) {
      previousTransmission = currentTime;
      const auto distance = String(front.getDistance());
      mqtt.publish("/smartcar/ultrasound/front", distance);
    }
   }
    delay(1);
}
