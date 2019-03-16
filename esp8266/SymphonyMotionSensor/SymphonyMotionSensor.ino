#include "Arduino.h"
#include "Symphony.h"

Product product;
Symphony s = Symphony();
#define INPUT_PIN         13
#define LED_PIN1			  2
#define USER_PWD_LEN      40
bool oldInputState;
bool isLatchSwitch = false;
volatile bool pirStateChanged = 0;


void handleInterrupt() {
	pirStateChanged = 1;
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
		case 1:
			break;
		case 3:
			break;
		case 10:
			isLatchSwitch = json["val"];
			break;
		}
	}
	Serial.println("\ncallback executed end");
}
void setup()
{
	Serial.begin(115200);
	delay(10);
	pinMode(INPUT_PIN, INPUT);
	Serial.println("************Setup MOTION SENSOR***************");
	s.setWsCallback(wsHandler);
	s.setup("PIR");

	oldInputState = digitalRead(INPUT_PIN);
	product = Product(s.myName, "Bedroom", "PIR");
	Gui gui1 = Gui("Mode", BUTTON_CTL, "Latch", 0, 1, 0);
	product.addProperty("0025", false, LED_PIN1, gui1);
	Gui gui2 = Gui("Mode", BUTTON_SNSR, "Sensor", 0, 1, 0);
	product.addProperty("0026", true, INPUT_PIN, gui2);
	Gui gui3 = Gui("State", BUTTON_CTL, "State", 0, 1, 0);
	product.addProperty("0060", false, gui3);
	s.setProduct(product);
	pinMode(INPUT_PIN, INPUT);
    oldInputState = digitalRead(INPUT_PIN);
    attachInterrupt(digitalPinToInterrupt(INPUT_PIN), handleInterrupt, CHANGE);
	product.print();
	int inputState = digitalRead(INPUT_PIN);
	Serial.printf("setup  inputstate is %d\n",inputState);
	Serial.println("************END Setup MOTION SENSOR ***************");
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
	//				MqttUtil::sendCommand("0026", oldInputState);
				}
				Serial.println("*** MotionSensor isLatch\n");
			} else {
				char state[2];
				sprintf(state, "%d", inputState);
				product.setValue("0026", inputState);
	//			MqttUtil::sendCommand("0026", inputState);
				Serial.println("*** MotionSensor isMomentary\n");
			}
		}
	}
}
