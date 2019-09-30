/**
 * This is a dimmable light.
 */
#include "Arduino.h"
#include "Symphony.h"
#include "version.h"

#define BRIGHTNESS  200

String myName = "dimLight";
Product product;
Symphony s = Symphony();

/**
 * START webrequest handlers
 */
void handleInit(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Init of DimmableLight.");
	Serial.println("handleInit");
}

/**
 * END webrequest handlers
 */

/*
 * Callback function for the websocket transactions
 * the following are the recognized commands
 * 	1 : the light sequence
 * 		data is an array of the sequences
 * 	2 : universe update
 * 	3 : fire selection
 * 	4 : color selection
 */
int wsHandlerJason(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) {
#ifdef DEBUG_PIXELS
		Serial.println("SymphonyLight json callback executed");
#endif
	if (json.containsKey("cmd")) {
		uint8_t cmd = json["cmd"];
		if (cmd == 1) { //light CYCLE update

		}
		if (cmd == 2) {//light config show/update

		}
	} else {
		Serial.println("Not a valid command");
	}
}
/*
 * inititalize the web pages to be shown
 */
void initWeb() {
	s.on("/init", HTTP_GET, handleInit);
}
//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START DimmableLight Setup***************");
	s.setWsCallback(wsHandlerJason);
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, MY_VERSION);
	s.setup(myName, ver);
	initWeb();
	product = Product(s.nameWithMac, "Bedroom", "Lights");
	Gui gui1 = Gui("Cool White", RADIO_CTL, "On/Off", 0, 1, 1);
	product.addProperty("0001", true, 5, gui1);
	Gui gui2 = Gui("Cool White", SLIDER_CTL, "Brightness", 0, 255, BRIGHTNESS);
	product.addProperty("0002", false, gui2);

	Gui gui3 = Gui("Warm White", RADIO_CTL, "On/Off", 0, 1, 0);
	product.addProperty("0003", true, 4, gui3);
	Gui gui4 = Gui("Warm White", SLIDER_CTL, "Brightness", 0, 255, BRIGHTNESS);
	product.addProperty("0004", false, gui4);
	s.setProduct(product);

	if (WiFi.status() == WL_CONNECTED ) {

	}
	Serial.printf("\n************END DimmableLight Setup Version %s***************\n", ver);
}

// The loop function is called in an endless loop
void loop()
{
	///DO NOT PUT A delay() out side of if (s.loop()){} AS IT CAUSES ERROR DURING OTA
	if (s.loop()) {

	}
}
