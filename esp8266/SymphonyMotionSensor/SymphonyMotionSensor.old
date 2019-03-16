#include "Arduino.h"
#include "SymphonyCore.h"

Symphony s = Symphony();
SymphProduct product = SymphProduct();
#define INPUT_PIN         13
#define LED_PIN1			  2
#define USER_PWD_LEN      40
bool oldInputState;
bool isLatchSwitch = false;
volatile bool doInterrupt = 0;

/*
 * Callback for Websocket events
 */
int WsCallback(uint8_t * payload, size_t length) {
	WsData wsdata = WsData(payload, length);
	Serial.printf("WsCallback payload=%s ssid=%s value=%s\n", payload, wsdata.getSSID().c_str(), wsdata.getValue().c_str());
	int cmd = atoi(wsdata.getValue().c_str());
	product.setValue(wsdata.getSSID(), cmd);
	if (MqttUtil::product.getProperty(wsdata.getSSID()).index == 1 ) {
		Serial.printf("WsCallback Latch is clicked\n");
		if (cmd == 1)
			isLatchSwitch = true;
		else
			isLatchSwitch = false;
	}
	return 0;
}
/*
 * Callback for MQTT events
 */
attribStruct MyMqttCallback(attribStruct property, int scmd) {
	MqttUtil::product.setValue(property.ssid, scmd);
	attribStruct returnPs;
	returnPs.pin = 15;
	returnPs.gui.value = scmd;
	returnPs.gui.pinType = SLIDER_OUT;
	return returnPs;
}

void handleInterrupt() {
	doInterrupt = 1;
}

void setup()
{
	Serial.begin(115200);
	Serial.println("************Setup MOTION SENSOR***************");

	product.productType = "0008";
	product.room = "U7YY";  //salas
	product.name = "PIR";
	int pIndex;
	product.addProperty(1, "0025", true, LED_PIN1, SymphProduct::createGui("Mode", BUTTON_OUT, "Latch", 0, 1, 0));
//	pIndex = product.addProperty(1, 12, BUTTON_OUT, true, "0025", "Switch", 0, 1, 0);
	product.addProperty(2, "0026", true, INPUT_PIN, SymphProduct::createGui("Mode", BUTTON_IN, "Sensor", 0, 1, 0));
	product.addProperty(3, "0060", false, SymphProduct::createGui("State", BUTTON_OUT, "State", 0, 1, 0));
//	pIndex = product.addProperty(13, INPUT_PIN, BUTTON_IN, true, "0026", "Sensor", 0, 1, 0);
	//	pinMode(INPUT_PIN, INPUT);
    oldInputState = digitalRead(INPUT_PIN);
    attachInterrupt(digitalPinToInterrupt(INPUT_PIN), handleInterrupt, CHANGE);
	product.print();
	s.setProduct(product);  //always set the product first before running the setup
	s.setWsCallback(&WsCallback);
	s.setMqttCallback(&MyMqttCallback);
	s.setup();
	int inputState = digitalRead(INPUT_PIN);
	Serial.printf("setup  inputstate is %d\n",inputState);
	digitalWrite(LED_PIN1, 1);
	Serial.println("************END Setup MOTION SENSOR ***************");
}

void loop()
{
	s.loop();
	if (doInterrupt) {
		doInterrupt = false;
		int inputState = digitalRead(INPUT_PIN);
		if (isLatchSwitch) {
			if (inputState) {
				oldInputState = !oldInputState;
				char state[2];
				sprintf(state, "%d", oldInputState);
				Symphony::setProperty("0026", state);
				MqttUtil::sendCommand("0026", oldInputState);
			}
		} else {
			char state[2];
			sprintf(state, "%d", inputState);
			Symphony::setProperty("0026", state);
			MqttUtil::sendCommand("0026", inputState);
			Serial.println("*** MotionSensor isMomentary\n");
		}
	}
	delay(500);
}
