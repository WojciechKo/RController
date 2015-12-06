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

int servoFix = 50;

const int DIRECTION_FORWARD = 1;
const int DIRECTION_BACKWARD = 2;

int direction = DIRECTION_FORWARD;
int speed = 0;

int enginePwm = 0;
int servoPwm = 90;
const int MIN_ENGINE_PWM = 0;

// cmd:
// ^ [1|2] [0-100] | [1|2] [0-100] [0-100]$
// ^direction speed | side   angle  servo-fix$
char START_CMD_CHAR = '^';
char END_CMD_CHAR = '$';
char SEP_CMD_CHAR = '|';

void setup() {
  pinMode(engineForwardPin, OUTPUT);
  pinMode(engineBackwardPin, OUTPUT);

  Serial.begin(9600);
  bluetooth.begin(9600);
  servo.attach(servoPin);

  digitalWrite(engineForwardPin, HIGH);
  digitalWrite(engineBackwardPin, LOW);
  analogWrite(servoPin, getServoPwm(side, angle, servoFix));
  analogWrite(enginePowerPin, getEnginePwm(direction, speed));
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
  int servoFix = bluetooth.parseInt();
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
  Serial.print(servoFix);
  Serial.println(END_CMD_CHAR);

  // Handle servo data
  int newServoPwm = getServoPwm(newSide, newAngle, servoFix);
  if (servoPwm != newServoPwm) {
    servoPwm = newServoPwm;
    servo.write(servoPwm);
    Serial.println("Servo moved");
  }
  
  // Handle engine data
  if (direction != newDirection) {
    // slow down
    for (int i = speed ; i >= MIN_ENGINE_PWM ; i--) {
      delay(10);
      enginePwm = getEnginePwm(direction, i);
      analogWrite(enginePowerPin, enginePwm);
    }

    // set direction
    digitalWrite(engineForwardPin, newDirection == DIRECTION_FORWARD ? HIGH : LOW);
    digitalWrite(engineBackwardPin, newDirection == DIRECTION_BACKWARD ? HIGH : LOW);
    direction = newDirection;
    
    // speed up
    for (int i = MIN_ENGINE_PWM ; i <= newSpeed ; i++) {
      delay(10);
      enginePwm = getEnginePwm(newDirection, i);
      analogWrite(enginePowerPin, enginePwm);
    }
    speed = newSpeed;
    
    Serial.println("Direction changed");
  }

  // Changed speed only
  if (speed != newSpeed) {
    speed = newSpeed;
    enginePwm = getEnginePwm(direction, speed);
    analogWrite(enginePowerPin, enginePwm);
    Serial.print("Speed changed: ");
    Serial.println(enginePwm);
  }
}

// Returns [0 - 180]
int getServoPwm(int side, int angle, int servoFix) {
  angle *= 0.9;
  int result = 90;
  switch (side) {
    case SIDE_RIGHT:
      result -= angle;
      break;
    case SIDE_LEFT:
      result += angle;
      break;
  }
  result += servoFix;
  return result;
}

// Returns [0 - 255]
int getEnginePwm(int direction, int speed) {
  return speed * 2.55;
}

