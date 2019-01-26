#include "Arduino.h"
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include "Symphony.h"
#include "ESPAsyncUDP.h"


Symphony s = Symphony();
AsyncUDP udp;

void handleUdp(AsyncWebServerRequest *request) {
	size_t size = udp.print("Hello Server from init!");
	char buff[50];
	sprintf(buff,"send udp, size:%i\n", size);
	request->send(200, "text/html", buff);
	Serial.println("handleInit");
}

//The setup function is called once at startup of the sketch
void setup()
{

// Add your initialization code here
	Serial.begin(115200);
	delay(10);
	s.setup("UDPClient");
	s.on("/test", HTTP_GET, handleUdp);
//	startUdpClient();
	startUDPServer();
}

// The loop function is called in an endless loop
void loop()
{
//Add your repeated code here
	if (s.loop()) {

	}
}

void startUdpClient() {
	;
	if(udp.connect(IPAddress(239,1,2,3), 1234)) {
		Serial.println("UDP connected");
		udp.onPacket([](AsyncUDPPacket packet) {
			Serial.print("Client UDP Packet Type: ");
			Serial.print(packet.isBroadcast()?"Broadcast":packet.isMulticast()?"Multicast":"Unicast");
			Serial.print(", From: ");
			Serial.print(packet.remoteIP());
			Serial.print(":");
			Serial.print(packet.remotePort());
			Serial.print(", To: ");
			Serial.print(packet.localIP());
			Serial.print(":");
			Serial.print(packet.localPort());
			Serial.print(", Length: ");
			Serial.print(packet.length());
			Serial.print(", Data: ");
			Serial.write(packet.data(), packet.length());
			Serial.println();
			//reply to the client
			packet.printf("Client got %u bytes of data", packet.length());
		});
		//Send unicast
		udp.print("Hello Server!");
		udp.broadcastTo("Anyone here?", 1234);
	} else {
		Serial.println("cannot connect to UDP Server");
	}
}

void startUDPServer() {
	if(udp.listenMulticast(IPAddress(239,1,2,3), 1234)) {
		Serial.print("UDP Listening on IP: ");
		Serial.println(WiFi.localIP());
		udp.onPacket([](AsyncUDPPacket packet) {
			Serial.print("TestUDP Server UDP Packet Type: ");
			Serial.print(packet.isBroadcast()?"Broadcast":packet.isMulticast()?"Multicast":"Unicast");
			Serial.print(", From: ");
			Serial.print(packet.remoteIP());
			Serial.print(":");
			Serial.print(packet.remotePort());
			Serial.print(", To: ");
			Serial.print(packet.localIP());
			Serial.print(":");
			Serial.print(packet.localPort());
			Serial.print(", Length: ");
			Serial.print(packet.length());
			Serial.print(", Data: ");
			Serial.write(packet.data(), packet.length());
			Serial.println();
			//reply to the client
			packet.printf("TestUDP Server got %u bytes of data", packet.length());
		});
		//Send multicast
		udp.print("Hello from TestUDP server!");
	}
}
