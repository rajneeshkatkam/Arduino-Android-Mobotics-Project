/*---------------------------------------------------------------------------------------------

  Open Sound Control (OSC) library for the ESP8266

  Example for sending messages from the ESP8266 to a remote computer
  The example is sending "hello, osc." to the address "/test".

  This example code is in the public domain.

--------------------------------------------------------------------------------------------- */
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <OSCMessage.h>

char ssid[] = "Resurrected";          // your network SSID (name)
char pass[] = "rajneesh97";                    // your network password

////Static IP Address
IPAddress staticIp(192,168,43,55);
IPAddress gateway(192,168,43,1);
IPAddress subnet(255, 255, 255, 0);



WiFiUDP Udp;                                // A UDP instance to let us send and receive packets over UDP
const IPAddress outIp(192,168,43,1);        // Mobile Hotspot IP
const unsigned int outPort = 1111;          // Port Number of Hotspot
const unsigned int localPort =5555 ;        // port to listen for OSC packets (actually not used for sending)

void setup() {
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

void loop() {
    OSCMessage msg("/test");
    msg.add("hello, osc.");
    Udp.beginPacket(outIp, outPort);
    Serial.println("Sending");
    msg.send(Udp);
    Udp.endPacket();
    msg.empty();
    delay(1);
}

