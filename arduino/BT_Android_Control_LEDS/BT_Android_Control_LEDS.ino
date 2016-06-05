String readString;
char c;

int redLED = 10;
int yellowLED = 11;
int greenLED = 12;

void setup() {
  pinMode(redLED,OUTPUT);
  pinMode(yellowLED,OUTPUT);
  pinMode(greenLED,OUTPUT);
  Serial.begin(115200);
  Serial.println("Ready for connection");
  Serial.println("Go on, type something");
}

void loop() {
  while(Serial.available()){
    delay(3);
    c = Serial.read();
    readString += c;
  }
  if (readString.length() > 0) {
    Serial.print("Char ----> ");
    Serial.write(c);
    Serial.println("\nString ----> " + readString);
    parseCommand(readString);
    readString = "";
  }
}

void parseCommand(String command) {
  char whichLED = command.charAt(0);
  String brigthnessString = command.substring(1);
  int pinLED;
  switch (whichLED) {
    case 'r':
      pinLED = redLED;
      break;
    case 'y':
      pinLED = yellowLED;
      break;
    case 'g':
      pinLED = greenLED;
      break;
  }
  int brigthness = brigthnessString.toInt();
  analogWrite(pinLED, brigthness);
}
