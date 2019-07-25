/*
 * This is the Symphony Socket
 * It is an ordinary wifi controllable socket.
 * But if it receives E1.31 data, it will synchronize to that if it receives a universe it recognizes.
 */

#include "Arduino.h"
#include <ESPAsyncE131.h>
#include "Symphony.h"
#include "version.h"

//#define DEBUG_SOCKET
#define SOCKET_PIN     12
#define UPDATES_PER_SECOND 100
#define UNIVERSE_START 1
#define UNIVERSE_COUNT 7

String myName = "symphonySocket";

uint16_t myUniverse = 2;
uint16_t countE131 = 0;  //count to be used to to determine if we are receiving any E131 packets
long timerMilli = 0;
ESPAsyncE131 e131;
bool prevSocketState;
bool socketState = 1;
bool isNormal = true;

struct timerStruct {
	bool enabled = false;
	uint8_t hour;
	uint8_t secs1;
	uint8_t secs2;
	uint8_t mins1;
	uint8_t mins2;
	long millis;
	long startMillis;
};

timerStruct timerData;

Product product;
Symphony s = Symphony();

void handleInit(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Init of SymphonySocket is called.");
	Serial.println("handleInit");
}

void handleToggle(AsyncWebServerRequest *request) {
	//we set the socket state, this will take effect if the device is in isNormal state in the loop
	socketState = !socketState;
//	digitalWrite(SOCKET_PIN, socketState);
	char buff[50];
	sprintf(buff, "Toggle of SymphonySocket is called. state is %u", socketState);
	request->send(200, "text/html", buff);
	Serial.printf("handleToggle state=%u\n",socketState);
}

void sendTimerData(AsyncWebSocketClient *client, JsonObject& json) {
	json["core"] = CORE_CONTROL;
	json["cmd"] = 1;
	json["e"] = timerData.enabled?1:0;
	json["hrs"] = timerData.hour;
	json["mins1"] = timerData.mins1;
	json["mins2"] = timerData.mins2;
	json["secs1"] = timerData.secs1;
	json["secs2"] = timerData.secs2;
	json["value"] = timerData.enabled?timerData.startMillis + timerData.millis - millis():0;
	String responseData;
	json.printTo(responseData);
	client->text(responseData);
}
/*
 * Callback function for the websocket transactions
 */
int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) {
//int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, uint8_t * payload, size_t len) {
	Serial.println("callback executed start");
	json.prettyPrintTo(Serial);
	if (json.containsKey("cmd")) {
			uint8_t cmd = json["cmd"];
			switch (cmd) {
			case 1://fetch timer command
				sendTimerData(client, json);
				break;
			case 3://set timer command
				timerData.enabled = true;
				timerData.startMillis = millis();
				timerData.hour = json["hrs"].as<int>();
				timerData.mins1 = json["mins1"].as<int>();
				timerData.mins2 = json["mins2"].as<int>();
				timerData.secs1 = json["secs1"].as<int>();
				timerData.secs2 = json["secs2"].as<int>();
				timerData.millis = timerData.hour * 3600000 +  // 3600000 milliseconds in an hour
						(timerData.mins1*10 + timerData.mins2) * 60000 + // 60000 milliseconds in a minute
						(timerData.secs1*10 + timerData.secs2) * 1000; // 1000 milliseconds in a second
				sendTimerData(client, json);
#ifdef DEBUG_SOCKET
				Serial.printf("\nhour: %i, millis:",timerData.hour);
				Serial.print(timerData.millis);
				Serial.print(", current:");
				Serial.println(millis());
#endif
				break;
			case 10://on-off command
				socketState = json["val"].as<int>();
				product.setValue("0001", socketState);
				break;
			}
	}
	Serial.println("\ncallback executed end");
}

void setup()
{
	Serial.begin(115200);
	delay(10);
	pinMode(SOCKET_PIN, OUTPUT);
	digitalWrite(SOCKET_PIN, socketState);
	Serial.println("\n\n************START Symphony Socket Setup***************");
	s.setWsCallback(wsHandler);
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, SOCKET_VERSION);
	s.setup(myName, ver);
	s.on("/init", HTTP_GET, handleInit);
	s.on("/toggle", HTTP_GET, handleToggle);
	s.serveStatic("/socket.html", SPIFFS, "/socket.html");
	product = Product(s.nameWithMac, "Bedroom", "Socket");
	Gui gui1 = Gui("Socket Control", BUTTON_CTL, "On/Off", 0, 1, socketState);
	product.addProperty("0001", false, SOCKET_PIN, gui1);//add aproperty that has an attached pin
	s.setProduct(product);

	if (e131.begin(E131_MULTICAST, UNIVERSE_START, UNIVERSE_COUNT))   // Listen via Multicast
		Serial.println(F("Listening for data..."));
	else
		Serial.println(F("*** e131.begin failed ***"));

//	e131.begin(E131_MULTICAST, myUniverse, 1);
	Serial.printf("\n************END Symphony Socket Setup Version %u.%u***************", SYMPHONY_VERSION, SOCKET_VERSION);
}

// The loop function is called in an endless loop
void loop() {

	if (s.loop()) {
		if (!e131.isEmpty() && isNormal) {
			isNormal = false;
		}
		if (millis() - timerMilli >= 1000) {
#ifdef DEBUG_SOCKET
			Serial.printf("countE131=%u countEmpty=%u\n",countE131);
#endif
			//we look at the values of the countE131 after every second
			if (countE131 == 0)
				isNormal = true;
			countE131 = 0;
			timerMilli = millis();
		}
		if (isNormal) {
			//the normal socket state
			if (socketState != prevSocketState) {
				digitalWrite(SOCKET_PIN, socketState);
				prevSocketState = socketState;
			}
			//let us look at the timer
			if (timerData.enabled) {
				if (millis() >= (timerData.startMillis + timerData.millis)) {
					socketState = !socketState;
					product.setValue("0001", socketState);
					timerData.enabled = false;		//disable timer
					DynamicJsonBuffer jsonBuffer;
					JsonObject& json = jsonBuffer.createObject();
					json["core"] = CORE_CONTROL;
					json["cmd"] = 1;
					json["e"] = 0;
					json["hrs"] = timerData.hour;
					json["mins1"] = timerData.mins1;
					json["mins2"] = timerData.mins2;
					json["secs1"] = timerData.secs1;
					json["secs2"] = timerData.secs2;
					json["value"] = timerData.enabled?timerData.startMillis + timerData.millis - millis():0;
					s.textAll(json);
				}
			}
		} else {
			if (!e131.isEmpty()) {
				countE131++;
				e131_packet_t packet;
				e131.pull(&packet);     // Pull packet from ring buffer
	#ifdef DEBUG_SOCKET
				Serial.printf("Universe %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u\n",
					htons(packet.universe),                 // The Universe for this packet
					htons(packet.property_value_count) - 1, // Start code is ignored, we're interested in dimmer data
					e131.stats.num_packets,                 // Packet counter
					e131.stats.packet_errors,               // Packet error counter
					packet.property_values[1]);             // Dimmer data for Channel 1
	#endif
				if (myUniverse == htons(packet.universe)) {  //we will only handle packets if universe is myUniverse
					if (packet.property_values[3])
						digitalWrite(SOCKET_PIN, 1);
					else
						digitalWrite(SOCKET_PIN, 0);
				}
			}
		}
	}
}
