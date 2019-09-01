/**
 * This is a smart light decor.  Randomly shows light sequences.
 * But if it receives E1.31 data, it will synchronize to that if it receives a universe it recognizes.
 */
#include "Arduino.h"
#include <ESPAsyncE131.h>
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include "SymphonyLight.h"
#ifndef SYMPHONYPRODUCT_H_
#include "Product.h"
#endif

//#define DEBUG_PIXELS

String myName = "symphonyLight";
Product product;

ESPAsyncE131 e131(10);//we allocate 10 buffers
uint16_t countE131 = 0;  //count to be used to to determine if we are receiving any E131 packets
long timerMilli = 0;

/**
 * START webrequest handlers
 */
void handleInit(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Init of SymphonyLight E1.31 is called.");
	Serial.println("handleInit");
}
void handleFire(AsyncWebServerRequest *request) {
	int i = 0;
	if(request->hasParam("value")) {
		AsyncWebParameter* p = request->getParam("value");
		i = atoi(p->value().c_str());
	}

	request->send(200, "text/html", "Fire sequence called.");
	Serial.println("handleFire");
	if (i < 0) {
		seq = CYCLE;
	} else {
		seq = FIRE;
		setupFire(i);
	}
}
void handleToggle(AsyncWebServerRequest *request) {
	char buff[50];
	if (effect == 3)
		effect = 0;
	else
		effect++;
	for (int i = 0; i < maxPattern; i++)
		palettes[i].pattern = effect;
	sprintf(buff, "Toggle of Effects is called. Effect is %u", effect);
	request->send(200, "text/html", buff);
	Serial.printf("handleToggle effect=%u\n",effect);
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
			seq = CYCLE;
			if (json.containsKey("data")) {
				JsonArray& a = json["data"];
#ifdef DEBUG_PIXELS
				a.printTo(Serial);
#endif
//				Serial.println(a.size());
				for (int i = 0; i < maxPattern; i++) {
					String ch = a[i].as<char *>();
					palettes[i].visible = ch[0];
					palettes[i].pattern = ch[1]-'0'; //convert char to integer
//					Serial.print(palettes[i].pattern);
				}
//				Serial.println("done");
				client->text("Change Light Cycle successful.");
			}
		}
		if (cmd == 2) {//light config show/update
			Serial.println("config update");
			json.prettyPrintTo(Serial);
			int cfg = json["cfg"].as<int>();//cfg==0, show config; cfg==1, save config and reboot
			if (cfg == 1) {
				//save config
				if (json.containsKey("data")) {
#ifdef DEBUG_PIXELS
					JsonArray& a = json["data"];
					Serial.printf("\nnew universe:%i, strings:%i, pixels:%i\n", json["u"].as<int>(), json["s"].as<int>(), json["p"].as<int>());
					for (int i = 0; i < a.size(); i++) {
						uint8_t pin = a[i]["pin"].as<int>();
						Serial.printf("\nnew pin:%i\n", pin);
					}
#endif
					json.remove("cfg");
					json.remove("cmd");
					myUniverse = json["u"].as<int>();
					colorOrder = json["rgb"].as<int>();
					if (colorOrder == 1) { cOrder[0] = R; cOrder[1] = G; cOrder[2] = B;}		//RBG
					if (colorOrder == 2) { cOrder[0] = R; cOrder[1] = B; cOrder[2] = G;}		//RBG
					if (colorOrder == 3) { cOrder[0] = G; cOrder[1] = B; cOrder[2] = R;}		//GBR
					if (colorOrder == 4) { cOrder[0] = G; cOrder[1] = R; cOrder[2] = B;}		//GRB
					if (colorOrder == 5) { cOrder[0] = B; cOrder[1] = R; cOrder[2] = G;}		//BRG
					if (colorOrder == 6) { cOrder[0] = B; cOrder[1] = G; cOrder[2] = R;}		//BGR
					Serial.printf("Color Order is:%u\n", colorOrder);

//					added july 19 2019
					if (e131.begin(E131_MULTICAST, myUniverse, 1))  //re-configure the E1.31 listener
						Serial.printf("Listening to Universe:%u\n", myUniverse);

					mirrored = json["m"];
					int doReset = json["rst"].as<int>();
					json.remove("rst");
					String confData;
					json.printTo(confData);
					file.saveToSPIFFS(lightConfigFile.c_str(), confData.c_str());
					json["core"] = 7;
					json["cmd"] = 4;//the task, 4=showConfig;
					json["msg"] = "Light Config successful.";
					s.textAll(json);
					if (doReset)
						s.doReboot();//let us reboot the ESP to effect the config
				} else {
					Serial.println("WARN  No config data sent.");
				}
			} else {
				//show config
				json["core"] = 7;
				json["cmd"] = 4;//the task, 4=showConfig;
				json["p"] = pixelCount;
				json["s"] = stringCount;
				json["u"] = myUniverse;
				json["m"] = mirrored;
				JsonArray& a = json.createNestedArray("data");
				for(int i = 0; i < stringCount; i++) {
					JsonObject& o = a.createNestedObject();
					o["pin"] = lights[i].pin;;
					if (lights[i].fwdDirection)
						o["fwd"] = 1;
					else
						o["fwd"] = 0;
//					char lineData[50];
//					sprintf(lineData, "{'u':%i,'s':%i,'p':%i,'fwd':%i}",lights[i].universe,
//							stringCount,
//							lights[i].pixCount,
//							lights[i].fwdDirection);
//					a.add(lineData);
				}
				s.textAll(json);
#ifdef DEBUG_PIXELS
				Serial.println("show config");
				json.prettyPrintTo(Serial);
#endif
			}
		}
		if (cmd == 3) {//fire commands
			seq = FIRE;
#ifdef DEBUG_PIXELS
			Serial.println("fire commands");
			json.prettyPrintTo(Serial);
#endif
			if (json.containsKey("data")) {
				int fireType = json["data"].as<int>();
				json["core"] = 7;
				json["cmd"] = 1;//the task, 1=showFire;
				s.textAll(json);
				setupFire(fireType);
				json["msg"] = "Fire command successful.";
				s.textAll(json);
			}
		}
		if (cmd == 4) {//color picker
//			Serial.println("color picker");
			seq = PICKER;
//			isNormal = false;
			if (json.containsKey("data")) {
				JsonArray& a = json["data"];
#ifdef DEBUG_PIXELS
				a.printTo(Serial);
				Serial.println(a.size());
#endif
				uint8_t r = a[0].as<int>();
				uint8_t g = a[1].as<int>();
				uint8_t b = a[2].as<int>();
//				Serial.printf("color is (%i, %i, %i)",r,g,b);
				for( int i = 0; i < NUM_PIXELS; i++) {
					for(int j=0; j<LED_STRINGS;j++) {
						theLeds[j][i].r = r;
						theLeds[j][i].g = g;
						theLeds[j][i].b = b;
					}
				}
				FastLED.show();
			}
		}
		if (cmd == 5) {//twinkle
			seq = TWINKLE;
		}
		if (cmd == 6) {//sparkle
			seq = SPARKLE;
		}
		if (cmd == 7) {//cycle
			seq = CYCLE;
		}
		if (cmd == 9) {
			json["core"] = 7;
			json["cmd"] = 2;
			json["rcode"] = 1;
			json["msg"] = "Get sequence successful.";
			JsonArray& a = json.createNestedArray("seq");
			for (int i = 0; i < 2; i++) {
				JsonObject& o = a.createNestedObject();
				o["color"] = sequenceArray[i].color;
				o["ptrn"] = sequenceArray[i].pattern;
				o["dur"] = sequenceArray[i].duration;
			}
//			client->text("{\"core\":7,\"rcode\":1,\"msg\":\"Get sequence successful.\"}");
			size_t len = json.measureLength();
			AsyncWebSocketMessageBuffer * buffer = ws.makeBuffer(len);
			if (buffer) {
				json.printTo((char *)buffer->get(), len + 1);
				client->text(buffer);
			}
		}
		if (cmd == 10) { //on-off command from the control page
			Serial.println("command is 10");
			if (json.containsKey("name")) {
				Serial.printf("deviceName=%s, value=%i\n",json["name"].as<char *>(), json["val"].as<int>());
			}
		}
		if (cmd == 11) {//Set Sequences
			Serial.println("command is 11");
//			data has this format
//			{
//				"core": 7,
//				"cmd": 11,
//				"seq": [
//					{
//						"ptrn": 0,
//						"color": "FF212D"
//						"dur": 120
//					},
//					{
//						"ptrn": 4,
//						"color": "5",
//						"dur": 60
//					}
//				]
//			}
			seq = CYCLE;
			if (json.containsKey("seq")) {

				JsonArray& a = json["seq"];
#ifdef DEBUG_PIXELS
				a.printTo(Serial);
#endif
//				Serial.println(a.size());
				for (int i = 0; i < a.size(); i++) {
					int color = a[i]["color"];
					int pattern = a[i]["ptrn"];
					int duration = a[i]["dur"];
					Serial.printf("color:%i, pattern:%i, duration:%i\n", color, pattern, duration);
					sequenceArray[i].color = color;
					sequenceArray[i].pattern = pattern;
					sequenceArray[i].duration = duration;
				}
				client->text("{\"core\":7,\"rcode\":1,\"msg\":\"Set sequence successful.\"}");

			}
		}
		if (cmd == 12) { //2.2	Get Sequences
			Serial.println("command is 12");
		}
		if (cmd == 13) { //2.3	Play Sequences
			Serial.println("command is 13");
		}
		if (cmd == 14) { //2.4	Pause Sequences
			Serial.println("command is 14");
		}
		if (cmd == 15) { //2.5	Stop Sequences
			Serial.println("command is 15");
		}
		if (cmd == 16) { //2.6	Test Sequence
			Serial.println("command is 16");
		}
		if (cmd == 17) { //2.7	Resume Normal Operation
			Serial.println("command is 17");
		}
		if (cmd == 18) { //2.8	Set Static Sequence
			Serial.println("command is 18");
		}
		if (cmd == 19) { //2.9	Set Static Color
			Serial.println("command is 19");
//			Serial.println("color picker");
			seq = PICKER;
//			isNormal = false;
			if (json.containsKey("data")) {
				JsonArray& a = json["data"];
#ifdef DEBUG_PIXELS
				a.printTo(Serial);
				Serial.println(a.size());
#endif
				uint8_t r = a[0].as<int>();
				uint8_t g = a[1].as<int>();
				uint8_t b = a[2].as<int>();
//				Serial.printf("color is (%i, %i, %i)",r,g,b);
				for( int i = 0; i < NUM_PIXELS; i++) {
					for(int j=0; j<LED_STRINGS;j++) {
						theLeds[j][i].r = r;
						theLeds[j][i].g = g;
						theLeds[j][i].b = b;
					}
				}
				FastLED.show();
			}
		}
		if (cmd == 20) { //2.10	Get Current Status
			Serial.println("command is 20");
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
/*
 * Fills the palettes with default values
 */
void fillPalettes() {
	palettes[0].palette = RainbowColors_p;
	palettes[1].palette = ForestColors_p;
	palettes[2].palette = myRedGreenBluePalette_p;
	palettes[3].palette = OceanColors_p;
	palettes[4].palette = myOrangeWhiteBluePalette_p;
	palettes[5].palette = HeatColors_p;
	palettes[6].palette = SetupGreenRedWhiteStripedPalette();
	palettes[7].palette = SetupYellowBlueWhiteStripedPalette();
	palettes[8].palette = myChristmasPalette_p;
	palettes[9].palette = PartyColors_p;
	palettes[10].palette = myRedWhiteBluePalette_p;
	palettes[11].palette = CloudColors_p;
	palettes[0].name = "Rainbow";
	palettes[1].name = "Forest";
	palettes[2].name = "RedGreenBlue";
	palettes[3].name = "Ocean";
	palettes[4].name = "OrangeWhiteBlue";
	palettes[5].name = "Heat";
	palettes[6].name = "GreenRedWhiteStriped";
	palettes[7].name = "YellowBlueWhiteStriped";
	palettes[8].name = "Christmas";
	palettes[9].name = "Party";
	palettes[10].name = "RedWhiteBlue";
	palettes[11].name = "Cloud";
}
//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START SymphonyLight Setup***************");
	fillPalettes();
	s.setWsCallback(wsHandlerJason);
	char ver[10];
	sprintf(ver, "%u.%u", SYMPHONY_VERSION, LIGHT_VERSION);
	s.setup(myName, ver);
	initWeb();
	product = Product(s.nameWithMac, "Bedroom", "Lights");
	Gui gui1 = Gui("Grp1", RADIO_CTL, "Red", 0, 1, 1);
	product.addProperty("0001", false, gui1);
	Gui gui2 = Gui("Grp1", RADIO_CTL, "Blue", 0, 1, 0);
	product.addProperty("0002", false, gui2);
	Gui gui3 = Gui("Grp1", RADIO_CTL, "Green", 0, 1, 0);
	product.addProperty("0003", false, gui3);
	s.setProduct(product);
	FastLED.setBrightness(  BRIGHTNESS );

	String config = file.readFrSPIFFS(lightConfigFile.c_str());
Serial.printf("%s is %s\n",lightConfigFile.c_str(), config.c_str());
	DynamicJsonBuffer jsonBuffer;
	JsonObject& jsonObj = jsonBuffer.parseObject(config);
	if (jsonObj.success()) {
		JsonArray& json = jsonObj["data"];
#ifdef DEBUG_PIXELS
		Serial.printf("Config has %i elements\n", json.size());
		json.printTo(Serial);Serial.println();
#endif
		stringCount = json.size();
		//create an array of [stringCount][pixCount];
		theLeds = new CRGB*[stringCount];
		lights = new lightStruct[stringCount];
		pixelCount = jsonObj["p"];
		myUniverse = jsonObj["u"];
		mirrored = jsonObj["m"];
		for(int i = 0; i < stringCount; i++) {
			lights[i].pin = json[i]["pin"].as<int>();
			lights[i].fwdDirection = json[i]["fwd"].as<bool>();
#ifdef DEBUG_PIXELS
			Serial.printf("*********** string%i pin=%i, pixels=%i, universe=%i, fwd=%i\n",i, lights[i].pin, pixelCount, myUniverse, lights[i].fwdDirection);
#endif
			theLeds[i] = new CRGB[pixelCount];
			if (i==0)
				FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[i], pixelCount).setCorrection( TypicalLEDStrip );
			if (i==1)
				FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds[i], pixelCount).setCorrection( TypicalLEDStrip );
			if (i==2)
				FastLED.addLeds<LED_TYPE, LED_PIN3, COLOR_ORDER>(theLeds[i], pixelCount).setCorrection( TypicalLEDStrip );
			if (i==3)
				FastLED.addLeds<LED_TYPE, LED_PIN4, COLOR_ORDER>(theLeds[i], pixelCount).setCorrection( TypicalLEDStrip );
		}
	} else {
		theLeds = new CRGB*[stringCount];
		lights = new lightStruct[stringCount];
		lights[0].pin = LED_PIN1;
		lights[1].pin = LED_PIN2;
		for(int i = 0; i < stringCount; i++) {
			theLeds[i] = new CRGB[pixelCount];
			lights[i].fwdDirection = true;
#ifdef DEBUG_PIXELS
			Serial.printf("****DEFAULT******* string%i pin=%i, pixels=%i, universe=%i, fwd=%i\n",i, lights[i].pin, pixelCount, myUniverse, lights[i].fwdDirection);
#endif
			FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[i], pixelCount).setCorrection( TypicalLEDStrip );
		}
	}
	if (WiFi.status() == WL_CONNECTED ) {
		//we only enable e131 if we are connected to wifi as client
//		if (e131.begin(E131_MULTICAST, UNIVERSE_START, UNIVERSE_COUNT))   // Listen via Multicast for 1-n channels
		if (e131.begin(E131_MULTICAST, myUniverse, 1))   // July 19 2019 Listen via Multicast for 1 channel (myUniverse) only
			Serial.println(F("Listening for data..."));
		else
			Serial.println(F("*** e131.begin failed ***"));
	}
	Serial.printf("\n************END SymphonyLight Setup Version %u.%u***************\n", SYMPHONY_VERSION, LIGHT_VERSION);
}

// The loop function is called in an endless loop
//this is used to test the unexpected flashing
void loopTest()
{
	///DO NOT PUT A delay() out side of if (s.loop()){} AS IT CAUSES ERROR DURING OTA
	if (s.loop()) {

		{
//			Serial.println("is not normal");
			if (!e131.isEmpty()) {
				countE131++;
				e131_packet_t packet;
				e131.pull(&packet);     // Pull packet from ring buffer
				//code below is for a string that has its own channel
				if (myUniverse == htons(packet.universe)) {
					for (int i=0; i<pixelCount;i++) {
						for (int j=0; j<stringCount;j++){
								theLeds[j][i] = getPixColorFromE131Stream(packet, 30, j, i);
						}
					}
//					Serial.println();
//					Serial.printf("\nListening to Universe%u: thisUniverse %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u,%u,%u\n",
//									myUniverse,
//									htons(packet.universe),                 // The Universe for this packet
//									htons(packet.property_value_count) - 1, // Start code is ignored, we're interested in dimmer data
//									e131.stats.num_packets,                 // Packet counter
//									e131.stats.packet_errors,               // Packet error counter
//									packet.property_values[1],  packet.property_values[2], packet.property_values[3]);
					FastLED.show();
				}
			}
		}
	}
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
			switch (seq) {
			case FIRE:
				loopFire();
				break;
			case PICKER:
				//do nothing, the wshandler will receive rgb values from user input
				break;
			case CYCLE:
				doNormalRoutine();
				break;
			case SPARKLE:
				if (toggle) {
					Sparkle();
					FastLED.show();
					toggle = false;
				}
				if (millis() - start >= 100) {	//toggle every 10ms
					toggle = true;
					start = millis();
				}
				break;
			}
		} else {
//			Serial.println("is E1.31 stream");
			if (!e131.isEmpty()) {
				countE131++;
				e131_packet_t packet;
				e131.pull(&packet);     // Pull packet from ring buffer
#ifdef DEBUG_PIXELS
				CRGB pixColor = getPixColorFromE131Stream(packet, 30, 0, 1);	//we get the first pixel's color
				Serial.printf("Listening for Universe%u: rcvUniverse %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u,%u,%u --> CRGB(%u, %u, %u)\n",
				myUniverse,
				htons(packet.universe),                 // The Universe for this packet
				htons(packet.property_value_count) - 1, // Start code is ignored, we're interested in dimmer data
				e131.stats.num_packets,                 // Packet counter
				e131.stats.packet_errors,               // Packet error counter
				packet.property_values[1],  packet.property_values[2], packet.property_values[3],  // Dimmer data for Channel 1
				pixColor.r, pixColor.g, pixColor.b);
#endif
				if (htons(packet.property_value_count) - 1 == 6) {
					//this is a "single" rgb led string
					if (myUniverse == htons(packet.universe)) {
						for (int i=0; i<pixelCount;i++) {
							for (int j=0; j<stringCount;j++){
								CRGB theColor = getPixColorFromE131Stream(packet, 30, 0, j);
								theLeds[j][i] = theColor;
							}
						}
						FastLED.show();
					}
				} else {
					if (mirrored) {
						//code below is for the mirrored string
						if (myUniverse == htons(packet.universe)) {
							for (int i=0; i<pixelCount;i++) {
								CRGB theColor = getPixColorFromE131Stream(packet, 30, 0, i);
								for (int j=0; j<stringCount;j++){
									theLeds[j][i] = theColor;
								}
							}
							FastLED.show();
						}
					} else {
						//code below is for a string that has its own channel
						if (myUniverse == htons(packet.universe)) {
							for (int i=0; i<pixelCount;i++) {
								for (int j=0; j<stringCount;j++){
//									if (lights[j].fwdDirection)
										theLeds[j][i] = getPixColorFromE131Stream(packet, 30, j, i);
//									else
//										theLeds[j][pixelCount-1-i] = getPixColorFromE131Stream(packet, 30, j, i);
								}
							}
							FastLED.show();
						}
					}
				}
			}
		}
	}
}
