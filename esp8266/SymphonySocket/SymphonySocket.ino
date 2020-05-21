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
#define LED_PIN     2
#define UPDATES_PER_SECOND 100
#define UNIVERSE_START 1
#define UNIVERSE_COUNT 7

Filemanager	file = 	Filemanager();
String socketConfigFile = "/socket.cfg";

uint16_t myUniverse = 2;
uint16_t countE131 = 0;  //count to be used to to determine if we are receiving any E131 packets
long myTimerMilli = 0;
ESPAsyncE131 e131;
bool prevSocketState;
bool socketState = 1;
bool isNormal = true;
uint8_t isE131Enabled = 0;

struct timerStruct {
	bool enabled = false;
	uint8_t hour;
	uint8_t secs;
	uint8_t mins;
	long millis;
	long startMillis;
};

timerStruct timerData;

Product product;
Symphony s = Symphony();

WiFiClient espClient;

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

/**
 * sends the content of the isE131Enabled and isMqttEnabled variables to the client
 * These variables were previously read from socket.cfg file during setup.
 */
void handleGetConfig(AsyncWebServerRequest *request) {
	DynamicJsonBuffer jsonBuffer;
	JsonObject& jsonObj = jsonBuffer.createObject();
	jsonObj["e131"] = isE131Enabled;
	String socketConfig;
	jsonObj.printTo(socketConfig);
	request->send(200, "text/html", socketConfig.c_str());
	Serial.println("handleGetConfig");
}

void sendTimerData(AsyncWebSocketClient *client, JsonObject& json) {
	json["core"] = CORE_CONTROL;
	json["cmd"] = 1;
	json["e"] = timerData.enabled?1:0;
	json["hrs"] = timerData.hour;
	json["mins"] = timerData.mins;
	json["secs"] = timerData.secs;
	json["value"] = timerData.enabled?timerData.startMillis + timerData.millis - millis():0;
	String responseData;
	json.printTo(responseData);
	client->text(responseData);
}

/*
 * Callback function for the websocket events
 */
int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) {
//int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, uint8_t * payload, size_t len) {
	Serial.println("SymphonySocket websocket callback executed start");
	json.prettyPrintTo(Serial);Serial.println();
	if (json.containsKey("cmd")) {
			uint8_t cmd = json["cmd"];
			switch (cmd) {
			case 1://fetch timer command
				sendTimerData(client, json);
				break;
			case 3://set timer command
			{
				timerData.enabled = true;
				timerData.startMillis = millis();
				if (json.containsKey("data")) {
					JsonArray& arr = json["data"];
					for(JsonVariant v : arr) {
						Serial.printf(" ssid=%i lbl=%s, val=%i\n", v["ssid"].as<int>(), v["lbl"].as<String>().c_str(), v["val"].as<int>());
						if (v["ssid"].as<int>() == 13)
							timerData.hour = v["val"].as<int>();
						if (v["ssid"].as<int>() == 14)
							timerData.mins = v["val"].as<int>();
						if (v["ssid"].as<int>() == 15)
							timerData.secs = v["val"].as<int>();
					}

				} else {
					timerData.hour = json["hrs"].as<int>();
					timerData.mins = 10 * json["mins1"].as<int>() + json["mins2"].as<int>();
					timerData.secs = 10* json["secs1"].as<int>() + 	json["secs2"].as<int>();
				}
				timerData.millis = timerData.hour * 3600000 +  // 3600000 milliseconds in an hour
					(timerData.mins) * 60000 + // 60000 milliseconds in a minute
					(timerData.secs) * 1000; // 1000 milliseconds in a second
				sendTimerData(client, json);
#ifdef DEBUG_SOCKET
				Serial.printf("\nhour: %i, millis:",timerData.hour);
				Serial.print(timerData.millis);
				Serial.print(", current:");
				Serial.println(millis());
#endif
				break;
			}
			case 7://set config command
			{
				json.remove("core");
				json.remove("cmd");
				isE131Enabled = json["e131"].as<int>();
				String confData;
				json.printTo(confData);
				file.saveToSPIFFS(socketConfigFile.c_str(), confData.c_str());
				s.doReboot();
				break;
			}
			case 10://on-off command
				int ssid = json["ssid"].as<int>();
				if (ssid==1) {
					socketState = json["val"].as<int>();
					product.setValue(json["ssid"].as<String>(), socketState, true);	//even if the property is virtual, we are setting its value here
					Serial.printf("wsHandler Pin%i set to %i\n",SOCKET_PIN, socketState);
				} else {
					attribStruct a = s.product.getProperty(json["ssid"].as<String>());
					Serial.printf("wsHandler timer %s=%i\n", a.gui.label.c_str(), json["val"].as<int>());
					if (ssid == 20)//for testing only
						product.setValue(a.ssid, json["val"].as<int>(), true);
				}
				break;
			}
	}
	Serial.println("SymphonySocket websocket callback executed end");
}
/*
 * Callback function for the mqtt events
 */
int mqttHandler(int index, String value) {
	Serial.printf("SymphonySocket mqtt callback executed start index=%i value=%s\n", index, value.c_str());
//	{
//	  "prop-index": 0,
//	  "MRN": "35027136",
//	  "MSN": "poop",
//	  "prop-value": "0",
//	  "CID": "wemos1_502914e722c"
//	}

	product.setValueByIndex(index, value.toInt(), false);
	if (index==0) {
		socketState = value.toInt();
	}
	Serial.println("SymphonySocket mqtt callback executed end");
}
void setup()
{
	Serial.begin(115200);
	delay(10);
	pinMode(SOCKET_PIN, OUTPUT);
	pinMode(LED_PIN, OUTPUT);
	digitalWrite(SOCKET_PIN, socketState);
	digitalWrite(LED_PIN, !socketState);
	Serial.println("\n\n************START Symphony Socket Setup***************");
	String config = file.readFrSPIFFS(socketConfigFile.c_str());
	Serial.printf("%s is %s\n",socketConfigFile.c_str(), config.c_str());
	DynamicJsonBuffer jsonBuffer;
	JsonObject& jsonObj = jsonBuffer.parseObject(config);
	isE131Enabled = jsonObj["e131"].as<int>();
	s.setWsCallback(wsHandler);
	s.setMqttCallback(mqttHandler);
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, MY_VERSION);
	bool mode = s.setup(ver);
	s.on("/init", HTTP_GET, handleInit);
	s.on("/toggle", HTTP_GET, handleToggle);
	s.serveStatic("/socket.html", SPIFFS, "/socket.html");

	s.on("/getConfig", HTTP_GET, handleGetConfig);

	product = Product(s.mac, "Kitchen", s.name);
	//gui1 and gui2 for the switch control
	Gui gui1 = Gui("Socket Control", BUTTON_CTL, "On/Off", 0, 1, socketState);
	product.addCallableProperty("01", SOCKET_PIN, gui1);//add a property that has an attached pin
	Gui gui2 = Gui("Socket Control", BUTTON_SNSR, "Indicator", 0, 1, socketState);
	product.addVirtualProperty("02", gui2);//add a logical property that has no attached pin
	Gui guiTest = Gui("Socket Control", BUTTON_CTL, "Test", 0, 1, socketState);
	product.addVirtualProperty("20", guiTest);//add a logical property that has no attached pin
	//gui3-8 for the timer control
//	Gui gui3 = Gui("Timer", BUTTON_SNSR, "Enabled", 0, 1, 0);
//	product.addVirtualProperty("11", gui3);//add a logical property that has no attached pin
//	Gui gui4 = Gui("Timer", SLIDER_SNSR, "Timer", 0, 24, 0);
//	product.addVirtualProperty("12", gui4);//add a logical property that has no attached pin
//	Gui gui5 = Gui("Timer", SLIDER_SUBMIT, "Hours", 0, 23, 0);
//	product.addVirtualProperty("13", gui5);//add a logical property that has no attached pin
//	Gui gui6 = Gui("Timer", SLIDER_SUBMIT, "Mins", 0, 59, 0);
//	product.addVirtualProperty("14", gui6);//add a logical property that has no attached pin
//	Gui gui7 = Gui("Timer", SLIDER_SUBMIT, "Secs", 0, 59, 0);
//	product.addVirtualProperty("15", gui7);//add a logical property that has no attached pin
//	Gui gui8 = Gui("Timer", SUBMIT, "Submit", 0, 1, 0);
//	product.addVirtualProperty("16", gui8);//add a logical property that has no attached pin
	s.setProduct(product);

	if (mode == 7) {//we do this only if we are connected as station
		if (e131.begin(E131_MULTICAST, UNIVERSE_START, UNIVERSE_COUNT))   // Listen via Multicast
			Serial.println(F("Listening for data..."));
		else
			Serial.println(F("*** e131.begin failed ***"));
	}

//	e131.begin(E131_MULTICAST, myUniverse, 1);
	Serial.printf("\n************END Symphony Socket Setup Version %u.%u***************\n", SYMPHONY_VERSION, MY_VERSION);
}

// The loop function is called in an endless loop
void loop() {

	if (s.loop()) {
		if (!e131.isEmpty() && isNormal) {
			isNormal = false;
		}
		if (millis() - myTimerMilli >= 1000) {
#ifdef DEBUG_SOCKET
			Serial.printf("countE131=%u countEmpty=%u\n",countE131);
#endif
			//we look at the values of the countE131 after every second
			if (countE131 == 0)
				isNormal = true;
			countE131 = 0;
			myTimerMilli = millis();
		}
		if (isNormal) {
			//the normal socket state
			if (socketState != prevSocketState) {
				digitalWrite(SOCKET_PIN, socketState);
				digitalWrite(LED_PIN, !socketState);
				prevSocketState = socketState;
				Serial.printf("loop Pin%i set to %i\n",SOCKET_PIN, socketState);
			}
			//let us look at the timer
			if (timerData.enabled) {
				if (millis() >= (timerData.startMillis + timerData.millis)) {
					//timer has elapsed, let us toggle the socketState
					socketState = !socketState;
					product.setValue("01", socketState, true);
					timerData.enabled = false;		//disable timer
					DynamicJsonBuffer jsonBuffer;
					JsonObject& json = jsonBuffer.createObject();
					json["core"] = CORE_CONTROL;
					json["cmd"] = 1;
					json["e"] = 0;
					json["hrs"] = timerData.hour;
					json["mins"] = timerData.mins;
					json["secs"] = timerData.secs;
					json["value"] = timerData.enabled?timerData.startMillis + timerData.millis - millis():0;
					s.textAll(json);
				}
			}
		} else if (isE131Enabled) {
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
