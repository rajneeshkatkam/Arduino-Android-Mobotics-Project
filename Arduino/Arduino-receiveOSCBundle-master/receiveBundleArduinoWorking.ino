/*---------------------------------------------------------------------------------------------

  Open Sound Control (OSC) library for the ESP8266

  Example for receiving open sound control (OSC) bundles on the ESP8266
  Send integers '0' or '1' to the address "/led" to turn on/off the built-in LED of the esp8266.

  This example code is in the public domain.

--------------------------------------------------------------------------------------------- */
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <OSCMessage.h>
#include <OSCBundle.h>
#include <OSCData.h>

char ssid[] = "Mobotics";          // your network SSID (name)
char pass[] = "12345678";          // your network password


////Static IP Address
IPAddress staticIp(192,168,43,55);     ///Static IP address assigned to the Arduino
IPAddress gateway(192,168,43,1);       ///Ip Adress of the Mobile Hotspot
IPAddress subnet(255, 255, 255, 0);    ///Subnet mask



// A UDP instance to let us send and receive packets over UDP
WiFiUDP Udp;
IPAddress outIp(192,168,43,1);         ///IP address of the Mobile Hotspot 
const unsigned int outPort = 1111;          ////Port number of the Hotspot
const unsigned int localPort = 5555;        // local port to listen for UDP packets (here's where we send the packets)  Port number for the Arduino


OSCErrorCode error;
//unsigned int ledState = LOW;              // LOW means led is *on*

void setup() {
  pinMode(D2, OUTPUT); //PWM MOTOR PIN Motor Left
  pinMode(D7,OUTPUT); //PWM MOTOR PIN  Motor Right

  pinMode(D3,OUTPUT);  //Motor Left Direction Pin 1
  pinMode(D4,OUTPUT);  //Motor Left Direction Pin 2
  
  pinMode(D5,OUTPUT);   //Motor Right Direction Pin 1
  pinMode(D6,OUTPUT);   //Motor Right Direction Pin 2

//  digitalWrite(D3,LOW);
//  digitalWrite(D4,HIGH);
//  digitalWrite(D5,LOW);
//  digitalWrite(D6,HIGH);
  
 //digitalWrite(D2, ;    // turn *on* led

  Serial.begin(115200);

  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  ///For Static Wifi Addressing
  WiFi.mode(WIFI_STA);
  WiFi.config(staticIp,gateway,subnet);
  
  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");

  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  Serial.println("Starting UDP");
  Udp.begin(localPort);
  Serial.print("Local port: ");
  Serial.println(Udp.localPort());

}


void receivedMessage(OSCMessage &msg) {
 // Serial.println("led led ");
  Serial.print(msg.getFloat(0));
  Serial.print("\t");
  Serial.print(msg.getFloat(1));
  Serial.print("\t");
  Serial.print(msg.getInt(2));
  Serial.print("\t");
  Serial.print(msg.getInt(3));
  Serial.print("\t");
  Serial.print(msg.getInt(4));
  Serial.print("\t");
  Serial.print(msg.getInt(5));
  Serial.print("\n");
  

  analogWrite(D2,(int)msg.getFloat(0));       //Left Motor PWM
  
  analogWrite(D7,(int)msg.getFloat(1));       //Right Motor PWM
  
  digitalWrite(D3,(bool)msg.getInt(2));       //D3-0, D4-1 Forward for Left Motor (Left Motor Direction Pins)
  digitalWrite(D4,(bool)msg.getInt(3));
  digitalWrite(D5,(bool)msg.getInt(4));       //D5-0, D6-1 Forward for Right Motor  (Right Motor Direction Pins)
  digitalWrite(D6,(bool)msg.getInt(5));
  
  
}

void loop() {
  OSCMessage message;
  int size = Udp.parsePacket();

  if (size > 0) {
    
    while (size--) {
      message.fill(Udp.read());
    }
    if (!message.hasError()) {
      message.dispatch("/motorValues", receivedMessage,0);
      
    } else {
      error = message.getError();
      Serial.print("error: ");
      Serial.println(error);
    }
  }
}



