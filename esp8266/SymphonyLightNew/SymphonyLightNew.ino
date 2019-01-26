/**
 * This is a smart light decor.  Randomly shows light sequences.
 * But if it receives E1.31 data, it will synchronize to that if it receives a universe it recognizes.
 */
#include "Arduino.h"
#include <ESPAsyncE131.h>
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include "SymphonyLightNew.h"
#ifndef SYMPHONYPRODUCT_H_
#include "Product.h"
#endif

#define DEBUG_PIXELS
#define MY_VERSION 1.30

String myName = "symphonyLight";
Product product;

ESPAsyncE131 e131;
uint16_t countE131 = 0;  //count to be used to to determine if we are receiving any E131 packets
long timerMilli = 0;

void handleInit(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Init of SymphonyLight E1.31 is called.");
	Serial.println("handleInit");
}

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
		switch (cmd) {
			case 1: {
				break;
			}
			case 2: {
				break;
			}
			case 3: {
				break;
			}
			case 4: {
				break;
			}
			case 5: {
				break;
			}
			case 10: { //on-off command from the control page
				Serial.println("command is 10");
				if (json.containsKey("name")) {
					Serial.printf("deviceName=%s, value=%i\n",json["name"].as<char *>(), json["val"].as<int>());
				}
			}
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
//	s.on("/fire", HTTP_GET, handleFire);
	s.serveStatic("/light.html", SPIFFS, "/light.html");
	s.serveStatic("/patterns.html", SPIFFS, "/patterns.html");
	s.serveStatic("/fire.html", SPIFFS, "/fire.html");
	s.serveStatic("/picker.html", SPIFFS, "/picker.html");
	s.serveStatic("/config.html", SPIFFS, "/config.html");
	s.serveStatic("/light.js", SPIFFS, "/light.js");
}
LightEffect* light[2];

//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START SymphonyLight Setup***************");
	s.setWsCallback(wsHandlerJason);
	s.setup(myName);
	initWeb();
	product = Product(s.myName, "Bedroom", "Lights");
	Gui gui1 = Gui("Grp1", RADIO_CTL, "Red", 0, 1, 1);
	product.addProperty("0001", false, gui1);
	Gui gui2 = Gui("Grp1", RADIO_CTL, "Blue", 0, 1, 0);
	product.addProperty("0002", false, gui2);
	Gui gui3 = Gui("Grp1", RADIO_CTL, "Green", 0, 1, 0);
	product.addProperty("0003", false, gui3);
	s.setProduct(product);
	FastLED.setBrightness(  BRIGHTNESS );
	if (WiFi.status() == WL_CONNECTED ) {
		//we only enable e131 if we are connected to wifi as client
		if (e131.begin(E131_MULTICAST, UNIVERSE_START, UNIVERSE_COUNT))   // Listen via Multicast
			Serial.println(F("Listening for data..."));
		else
			Serial.println(F("*** e131.begin failed ***"));
	}

	String config = file.readFrSPIFFS(lightConfigFile.c_str());
	StaticJsonBuffer<300> jsonBuffer;
	JsonArray& json = jsonBuffer.parseArray(config);

//	FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[0], NUM_PIXELS).setCorrection( TypicalLEDStrip );
//	FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds[1], NUM_PIXELS).setCorrection( TypicalLEDStrip );

	leds = createLedMatrix(1, 50);
	FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(leds.theLeds[0], 50).setCorrection( TypicalLEDStrip );
	FlowEffect flow = FlowEffect(leds);
	WipeEffect wipe = WipeEffect(leds);
	wipe.setRefreshRate(500);
	light[0] = &flow;
	light[1] = &wipe;
	Serial.printf("************PIXELS=%d STRINGS=%d***************\n", NUM_PIXELS, LED_STRINGS);
	Serial.printf("\n************END SymphonyLight Setup Version%.2f***************\n", MY_VERSION);
}
// The loop function is called in an endless loop
void loop()
{
	///DO NOT PUT A delay() out side of if (s.loop()){} AS IT CAUSES ERROR DURING OTA
	if (s.loop()) {
		if (!e131.isEmpty() && isNormal) {
			isNormal = false;
		}
		if (millis() - timerMilli >= 1000) {
			//we look at the values of the countE131 after every second
			if (countE131 == 0)
				isNormal = true;
			countE131 = 0;
			timerMilli = millis();
		}
		if (isNormal) {
			for (int i=0; i<2; i++) {
//				bool result = light[i]->run();
				if (light[i] == NULL)
					Serial.println("******************** light[i] is null!!!");
				if (light[i] == nullptr)
					Serial.println("******************** light[i] is nullptr!!!");
				light[i]->print();
			}
		} else {
			if (!e131.isEmpty()) {
				countE131++;
				e131_packet_t packet;
				e131.pull(&packet);     // Pull packet from ring buffer
#ifdef DEBUG_PIXELS
				CRGB pixColor = getPixColorFromE131Stream(packet, 30, 0, 1);	//we get the first pixel's color
				Serial.printf("Listening to Universe%u: thisUniverse %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u,%u,%u --> CRGB(%u, %u, %u)\n",
				myUniverse,
				htons(packet.universe),                 // The Universe for this packet
				htons(packet.property_value_count) - 1, // Start code is ignored, we're interested in dimmer data
				e131.stats.num_packets,                 // Packet counter
				e131.stats.packet_errors,               // Packet error counter
				packet.property_values[1],  packet.property_values[2], packet.property_values[3],  // Dimmer data for Channel 1
				pixColor.r, pixColor.g, pixColor.b);
#endif
				if (myUniverse == htons(packet.universe)) {  //we will only handle packets if universe is myUniverse
					for (int i = 0; i < NUM_PIXELS; i++) {
						for (int k=0; k< LED_STRINGS;k++) {
							theLeds[k][i] = getPixColorFromE131Stream(packet, 30, k, i);
//							int l = k * NUM_PIXELS + i;
//							int j = l * 3 + (CHANNEL_START - 1);
//							theLeds[k][i].setRGB(packet.property_values[j+1], packet.property_values[j+3], packet.property_values[j+2]);
						}
					}
					FastLED.show();
				}
			}
		}
	}
}
