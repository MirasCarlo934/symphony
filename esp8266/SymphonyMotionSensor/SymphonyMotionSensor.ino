#include "Arduino.h"
#include "Symphony.h"
#include "version.h"

#define INPUT_PIN		13
#define LED_PIN1		2
#define USER_PWD_LEN	40
bool oldInputState;
bool isLatchSwitch = false;
bool isController = true;
volatile bool pirStateChanged = 0;
String myName = "PIR";	//temporary name, will be set later via the admin config client

Product product;
Symphony s = Symphony();

void handleInterrupt() {
	pirStateChanged = 1;
}

/*
 * Callback function for the mqtt events
 */
int mqttHandler(JsonObject& json) {
	Serial.println("PIR mqtt callback executed start");
	json.prettyPrintTo(Serial);Serial.println();
	Serial.println("PIR mqtt callback executed end");
}
/*
 * Callback function for the websocket transactions
 * Will only be called if the property directPin = false
 */
int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) {
//int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, uint8_t * payload, size_t len) {
	Serial.println("callback executed start");
	json.prettyPrintTo(Serial);
	if (json.containsKey("cmd")) {
		uint8_t cmd = json["cmd"];
		switch (cmd) {
			case 1:
				break;
			case 3:
				break;
			case 10: {
				String ssid = json["ssid"].as<char*>();
				product.setValue(ssid, json["val"].as<int>());
				json["core"] = 20;
				s.textAll(json);		//broadcast to other clients
				String strReg;
				json.printTo(strReg);
				s.transmit(strReg.c_str());
				if (ssid.toInt() == 25) {
					isLatchSwitch = json["val"];
				} else if (ssid.toInt() == 27) {
					isController = json["val"];
				} break;
			}
		}
	}
	Serial.println("\ncallback executed end");
}

/**
 *
 */
void sendSensorData(int value) {
	DynamicJsonBuffer jsonBuffer;
	JsonObject& reply = jsonBuffer.createObject();
	reply["core"] = WSCLIENT_DO_DISPLAY;
	reply["cmd"] = WSCLIENT_DO_CMD;
	reply["ssid"] = "0026";
	reply["mac"] = s.mac;
	reply["cid"] = 0;	//we are putting a dummy id, the cid of client will not match this.  Hence client will change the element's satus.
	reply["val"] = value;
	String replyStr;
	reply.printTo(replyStr);
	Serial.print("*** Sending ");reply.printTo(Serial);Serial.println();
	s.textAll(reply);	//broadcast to other clients

	DynamicJsonBuffer buffer;
	JsonObject& poopJson = buffer.createObject();
	poopJson["MRN"] = Symphony::getMRN();
	poopJson["MSN"] = "poop";
	poopJson["CID"] = "0000";
	poopJson["prop-index"] = "0026";
	poopJson["prop-value"] = value;
	String strReg;
	poopJson.printTo(strReg);
	s.transmit(strReg.c_str());	//transmit to mqtt
}
/**
 * The setup
 */
void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("************Setup MOTION SENSOR***************");
	s.setWsCallback(wsHandler);

	s.setMqttCallback(mqttHandler);
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, MY_VERSION);
	s.setup(myName, ver);

	product = Product(s.nameWithMac, "Dining", myName);
	pinMode(LED_PIN1, OUTPUT);
	digitalWrite(LED_PIN1, 1);
	Gui gui1 = Gui("Mode", BUTTON_CTL, "Latch", 0, 1, 0);
	product.addProperty("0025", LED_PIN1, gui1);
	Gui gui2 = Gui("Mode", BUTTON_SNSR, "Sensor", 0, 1, 0);
	product.addProperty("0026", INPUT_PIN, gui2);
	Gui gui3 = Gui("Mode", BUTTON_SNSR, "Controller", 0, 1, 1);
	product.addVirtualProperty("0027", gui3);	//add a logical property that has no attached pin
	Gui gui4 = Gui("State", BUTTON_CTL, "State", 0, 1, 0);
	product.addVirtualProperty("0060", gui4);	//add a logical property that has no attached pin
	Gui gui5 = Gui("State", SLIDER_CTL, "Test", 0, 100, 50);
	product.addVirtualProperty("0070", gui5);	//add a logical property that has no attached pin
	Gui gui6 = Gui("State", SLIDER_SNSR, "SnSR", 0, 100, 70);
	product.addVirtualProperty("0080", gui6);	//add a logical property that has no attached pin
	s.setProduct(product);
	pinMode(INPUT_PIN, INPUT);
	oldInputState = digitalRead(INPUT_PIN);
	attachInterrupt(digitalPinToInterrupt(INPUT_PIN), handleInterrupt, CHANGE);
	product.print();
	int inputState = digitalRead(INPUT_PIN);
	Serial.printf("\n************END  Setup MOTION SENSOR %u.%u***************\n", SYMPHONY_VERSION, MY_VERSION);
}

void loop()
{
	if (s.loop()) {
		if (pirStateChanged) {
			pirStateChanged = false;
			int inputState = digitalRead(INPUT_PIN);
			Serial.printf("statechanged %d\n",inputState);
			if (isLatchSwitch) {
				if (inputState) {
					oldInputState = !oldInputState;
					product.setValue("0026", oldInputState);
					digitalWrite(LED_PIN1, !oldInputState);
//					if (isController) {
//						sendSensorData(oldInputState?1:0);
//					}
	//				MqttUtil::sendCommand("0026", oldInputState);
				}
				Serial.println("*** MotionSensor isLatch\n");
			} else {
				char state[2];
				sprintf(state, "%d", inputState);
				product.setValue("0026", inputState);
				digitalWrite(LED_PIN1, !inputState);
//				if (isController) {
//					sendSensorData(inputState);
//				}
	//			MqttUtil::sendCommand("0026", inputState);
				Serial.println("*** MotionSensor isMomentary\n");
			}
		}
	}
}
