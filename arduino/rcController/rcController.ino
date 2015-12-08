#include <SoftwareSerial.h>
#include <Servo.h>

#define bluetoothTx 2
#define bluetoothRx 3
#define engineForwardPin 4
#define engineBackwardPin 5
#define enginePowerPin 6
#define servoPin 9

Servo servo;
SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);

const int SIDE_RIGHT = 1;
const int SIDE_LEFT = 2;

int side = SIDE_RIGHT;
int angle = 0;
int servoFix = 14;

const int DIRECTION_FORWARD = 1;
const int DIRECTION_BACKWARD = 2;

int direction = DIRECTION_FORWARD;
int speed = 0;

const int MIN_ENGINE_SPEED = 80;

// cmd:
// ^ [1|2] [0-100] | [1|2] [0-100] [-50 - 50]$
// ^direction speed | side   angle  servo-fix$
char START_CMD_CHAR = '^';
char END_CMD_CHAR = '$';
char SEP_CMD_CHAR = '|';

void setup() {
  pinMode(engineForwardPin, OUTPUT);
  pinMode(engineBackwardPin, OUTPUT);
  pinMode(enginePowerPin, OUTPUT);
  pinMode(servoPin, OUTPUT);

  Serial.begin(9600);
  bluetooth.begin(9600);
  servo.attach(servoPin);

  setServo(side, angle, servoFix);
  setDirectionPins(direction);
  setEnginePower(direction, speed);
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
  int newServoFix = bluetooth.parseInt();
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
  Serial.print(" ");
  Serial.print(newServoFix);
  Serial.println(END_CMD_CHAR);

  // Side or angle has changed
  if (side != newSide || angle != newAngle || servoFix != newServoFix) {
    setServo(newSide, newAngle, newServoFix);

    side = newSide;
    angle = newAngle;
    servoFix = newServoFix;
    Serial.println("Servo moved");
  }

   // Direction has changed
  if (direction != newDirection) {
    // Slow down
    for (int speedIterator = speed ; speedIterator >= MIN_ENGINE_SPEED ; setEnginePower(direction, --speedIterator)) {
      delay(10);
    }

    // Change direction
    setDirectionPins(newDirection);
    
    // Speed up
    for (int speedIterator = MIN_ENGINE_SPEED ; speedIterator <= newSpeed ; setEnginePower(newDirection, speedIterator++)) {
      delay(10);
    }

    speed = newSpeed;
    direction = newDirection;
    Serial.println("Direction changed");

  // Only speed has changed
  } else if (speed != newSpeed) {
    setEnginePower(direction, newSpeed);

    speed = newSpeed;
    Serial.print("Speed changed");
  }
}

void setServo(int side, int angle, int servoFix) {
  int servoPwm = getServoPwm(side, angle, servoFix);
  servo.write(servoPwm);
}

void setDirectionPins(int direction) {
    digitalWrite(engineForwardPin, direction == DIRECTION_FORWARD ? HIGH : LOW);
    digitalWrite(engineBackwardPin, direction == DIRECTION_BACKWARD ? HIGH : LOW);
}

void setEnginePower(int direction, int speed) {
    analogWrite(enginePowerPin, getEnginePwm(speed >= MIN_ENGINE_SPEED ? speed : 0));
}

// Returns [0 - 180]
int getServoPwm(int side, int angle, int servoFix) {
  angle *= 0.25;
  int result = 90;
  switch (side) {
    case SIDE_RIGHT:
      result += angle;
      break;
    case SIDE_LEFT:
      result -= angle;
      break;
  }
  result += servoFix;
  return result;
}

// Returns [0 - 255]
int getEnginePwm(int speed) {
  return speed * 2.55;
}

