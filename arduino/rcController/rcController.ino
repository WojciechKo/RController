#include <SoftwareSerial.h>
#include <Servo.h>

#define bluetoothTx 3
#define bluetoothRx 2
#define enginePin 5
#define servoPin 6

Servo servo;
SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);

int side = 1;
const int SIDE_RIGHT = 1;
const int SIDE_LEFT = 2;

int angle = 0;

int direction = 1;
const int DIRECTION_FORWARD = 1;
const int DIRECTION_BACKWARD = 2;

int speed = 0;

int enginePwm = 0;
int servoPwm = 90;

// cmd:
// ^ [1|2] [0-100] | [1|2] [0-100] $
//    side  angle  direction  speed
char START_CMD_CHAR = '^';
char END_CMD_CHAR = '$';
char SEP_CMD_CHAR = '|';

void setup() {
  Serial.begin(9600);
  bluetooth.begin(9600);
  servo.attach(servoPin);
}

void loop() {
  Serial.flush();

  if (bluetooth.available() < 1) return;

  // Read cmd
  if (bluetooth.read() != START_CMD_CHAR) return;

  int newDirection = bluetooth.parseInt();
  int newSpeed = bluetooth.parseInt();

  if (bluetooth.read() != SEP_CMD_CHAR) return;

  int newSide = bluetooth.parseInt();
  int newAngle = bluetooth.parseInt();

  if (bluetooth.read() != END_CMD_CHAR) return;

  // Validation
  if (newSide != SIDE_LEFT && newSide != SIDE_RIGHT) return;
  if (newAngle < 0 || newAngle > 100) return;
  if (newDirection != DIRECTION_FORWARD && newDirection != DIRECTION_BACKWARD) return;
  if (newSpeed < 0 || newSpeed > 100) return;

  // Debug
  Serial.print(START_CMD_CHAR);
  Serial.print(newDirection);
  Serial.print(" ");
  Serial.print(newSpeed);
  Serial.print(SEP_CMD_CHAR);
  Serial.print(newSide);
  Serial.print(" ");
  Serial.print(newAngle);
  Serial.println(END_CMD_CHAR);

  // Handle servo data
  int newServoPwm = getServoPwm(newSide, newAngle);
  if (servoPwm != newServoPwm) {
    servoPwm = newServoPwm;
    servo.write(servoPwm);
    Serial.println("Servo moved");
  }
  
  // Handle engine data
  if (direction != newDirection) {
    for (int i = speed ; i >= 0 ; i--) {
      delay(10);
      enginePwm = getEnginePwm(direction, i);
      analogWrite(enginePin, enginePwm);
    }

    for (int i = 0 ; i <= newSpeed ; i++) {
      delay(10);
      enginePwm = getEnginePwm(newDirection, i);
      analogWrite(enginePin, enginePwm);
    }

    direction = newDirection;
    speed = newSpeed;
    Serial.println("Direction changed");
  }

  // Changed only speed
  if (speed != newSpeed) {
    speed = newSpeed;
    enginePwm = getEnginePwm(direction, speed);
    analogWrite(enginePin, enginePwm);
    Serial.println("Speed changed");
  }
}

int getServoPwm(int side, int angle) {
  angle *= 0.9;
  if (side == SIDE_RIGHT) {
    return 90 - angle;
  } else { // SIDE_LEFT
    return 90 + angle;
  }
}

int getEnginePwm(int direction, int speed) {
  return speed * 2.55;
}

