/*
* ESP8266_Test.ino - Simple sketch to listen for E1.31 data on an ESP8266
*                    and print some statistics.
*
* Project: E131 - E.131 (sACN) library for Arduino
* Copyright (c) 2015 Shelby Merrick
* http://www.forkineye.com
*
*  This program is provided free for you to use in any way that you wish,
*  subject to the laws and regulations where you are using it.  Due diligence
*  is strongly suggested before using this code.  Please give credit where due.
*
*  The Author makes no warranty of any kind, express or implied, with regard
*  to this program or the documentation contained in this document.  The
*  Author shall not be liable in any event for incidental or consequential
*  damages in connection with, or arising out of, the furnishing, performance
*  or use of these programs.
*
*/


#include <E131.h>
#include <FastLED.h>
#include "SymphonyCore.h"


#define LED_STRINGS    2
#define NUM_PIXELS    30
#define LED_PIN1     14
#define LED_PIN2     12
#define LED_TYPE    WS2812B
#define BRIGHTNESS  64
#define COLOR_ORDER GRB
#define CHANNEL_START 1 /* Channel to start listening at */
#define UPDATES_PER_SECOND 100

uint16_t myUniverse = 1;
CRGB theLeds[LED_STRINGS][NUM_PIXELS];
E131 e131;
boolean isMusical = false;

Symphony s = Symphony();
SymphProduct product = SymphProduct();
enum propertyIndex : uint8_t {
	Universe = 0,	//analog
	start = 1,	//digital
	stop = 2	//digital
};

CRGBPalette16 currentPalette1;
TBlendType    currentBlending;

extern const TProgmemPalette16 myRedWhiteBluePalette_p PROGMEM;
extern const TProgmemPalette16 myChristmasPalette_p PROGMEM;

/*
 * Callback for Websocket events
 */
int WsCallback(uint8_t * payload, size_t length) {
	WsData wsdata = WsData(payload, length);
	Serial.printf("WsCallback payload=%s ssid=%s value=%s\n", payload, wsdata.getSSID().c_str(), wsdata.getValue().c_str());
	product.setValue(wsdata.getSSID(), atoi(wsdata.getValue().c_str()));
	handleCommand(product.getProperty(wsdata.getSSID()).index, atoi(wsdata.getValue().c_str()));
	return 0;
}
/*
 * Callback for MQTT events
 */
attribStruct MyMqttCallback(attribStruct property, int scmd) {

	MqttUtil::product.setValue(property.ssid, scmd);
	MqttUtil::product.setDone(property.ssid);
	return (MqttUtil::product.getProperty(property.ssid));
}
void setup() {
    Serial.begin(115200);
    delay(10);
    Serial.println("\n\n************START Symphony Music Setup***************");
	product.productType = "0011";
	product.room = "U7YY";  //salas
	product.name = "tree";
	String u = SpiffsUtil::readFrSPIFFS("/universe");
	if (u.length()==0)
		Serial.printf("no universe found. Setting to 1.\n");
	else
		myUniverse = atoi(u.c_str());
	Serial.printf("universe fr spiffs:%s\n",u.c_str());

	String m = SpiffsUtil::readFrSPIFFS("/music");
	if (m.length()==0)
		Serial.printf("no music setting found. Setting to Stop.\n");
	else
		isMusical = atoi(m.c_str());
	Serial.printf("music setting fr spiffs:%s\n",m.c_str());

	FastLED.setBrightness(  BRIGHTNESS );
	currentPalette1 = RainbowColors_p;
	currentBlending = LINEARBLEND;
	product.addProperty(Universe, "0060", false, SymphProduct::createGui("E131",SLIDER_OUT,"Set Universe",0, 10, myUniverse));
	product.addProperty(start, "0059", false, SymphProduct::createGui("E131", RADIO_OUT,"Start",0, 1, isMusical));
	product.addProperty(stop, "0058", false, SymphProduct::createGui("E131", RADIO_OUT,"Stop",0, 1, !isMusical));
    s.setProduct(product);  //always set the product first before running the setup
	s.setWsCallback(&WsCallback);
	s.setMqttCallback(&MyMqttCallback);
	s.setup();
    /* Choose one to begin listening for E1.31 data */
//    e131.begin(ssid, passphrase);               /* via Unicast on the default port */
//	SpiffsUtil::saveSsid(server.arg("ssid").c_str());
	if (isMusical)
		e131.begin(E131_MULTICAST, myUniverse, 1);
	FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[0], NUM_PIXELS).setCorrection( TypicalLEDStrip );
	FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds[1], NUM_PIXELS).setCorrection( TypicalLEDStrip );
//    Serial.printf("e131.data = %u , %u\n", sizeof(e131.data), sizeof(*e131.data));
}

void loop() {
	s.loop();
	if (isMusical) {
		/* Parse a packet */
		uint16_t num_channels1 = e131.parsePacket();

		/* Process channel data if we have it */
		if (num_channels1) {
			Serial.printf("Listening to Universe%u: Universe %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u\n",
					myUniverse,
					e131.universe,              // The Universe for this packet
					num_channels1,               // Number of channels in this packet
					e131.stats.num_packets,     // Packet counter
					e131.stats.packet_errors,   // Packet error counter
					e131.data[0]);              // Dimmer data for Channel 1
			if ( e131.universe == myUniverse) {
				for (int i = 0; i < NUM_PIXELS; i++) {
					for (int k=0; k< LED_STRINGS;k++) {
						int l = k * NUM_PIXELS + i;
						int j = l * 3 + (CHANNEL_START - 1);
						theLeds[k][i].setRGB(e131.data[j], e131.data[j+1], e131.data[j+2]);
					}
				}
				FastLED.show();
			}
		}
	} else {
		//do a random light sequence
		ChangePalettePeriodically();
		static uint8_t startIndex = 0;
		startIndex = startIndex + 1; /* motion speed */
		FillLEDsFromPaletteColors( startIndex);
		FastLED.show();
		FastLED.delay(1000 / UPDATES_PER_SECOND);
	}
}

/*
 * Handles the transactions from ws
 */
int handleCommand(int index, int cmd) {
  Serial.printf("handleCommand index=%d cmd=%d\n",index,cmd);
  switch (index) {
    case start:
    	isMusical = true;
		SpiffsUtil::saveToSPIFFS("/music", "1");
		s.sendToWSClient();
		ESP.restart();
    	break;
    case stop:
    	isMusical = false;
		SpiffsUtil::saveToSPIFFS("/music", "0");
		s.sendToWSClient();
		ESP.restart();
    	break;
    case Universe:
    	myUniverse = cmd;
    	Serial.printf("myUniverse:%u",myUniverse);
    	String univ = SpiffsUtil::readFrSPIFFS("/universe");
		if (univ.length()==0)
			Serial.printf("no universe found.\n");
    	char u[2];
    	sprintf(u, "%u", cmd);
    	SpiffsUtil::saveToSPIFFS("/universe", u);
    	s.sendToWSClient();
    	ESP.restart();
    	break;
  }
  return 23;
}

void FillLEDsFromPaletteColors( uint8_t colorIndex)
{
    uint8_t brightness = 255;

    for( int i = 0; i < NUM_PIXELS; i++) {
    	for(int j=0; j<LED_STRINGS;j++) {
    		theLeds[j][i] = ColorFromPalette( currentPalette1, colorIndex, brightness, currentBlending);
    	}
        colorIndex += 3;
    }
}


// There are several different palettes of colors demonstrated here.
//
// FastLED provides several 'preset' palettes: RainbowColors_p, RainbowStripeColors_p,
// OceanColors_p, CloudColors_p, LavaColors_p, ForestColors_p, and PartyColors_p.
//
// Additionally, you can manually define your own color palettes, or you can write
// code that creates color palettes on the fly.  All are shown here.

void ChangePalettePeriodically()
{
    uint8_t secondHand = (millis() / 1000) % 60;
    static uint8_t lastSecond = 99;

    if( lastSecond != secondHand) {
        lastSecond = secondHand;
        if( secondHand ==  0)  { currentPalette1 = RainbowColors_p;         currentBlending = LINEARBLEND; Serial.println("RainbowColors_p LINEARBLEND");}
        if( secondHand == 10)  { currentPalette1 = RainbowStripeColors_p;   currentBlending = NOBLEND;  Serial.println("RainbowStripeColors_p NOBLEND");}
        if( secondHand == 15)  { currentPalette1 = RainbowStripeColors_p;   currentBlending = LINEARBLEND; Serial.println("RainbowStripeColors_p LINEARBLEND");}
        if( secondHand == 20)  { SetupRedBlueAndGreenPalette();             currentBlending = LINEARBLEND; Serial.println("SetupPurpleAndGreenPalette LINEARBLEND");}
        if( secondHand == 25)  { SetupTotallyRandomPalette();              currentBlending = LINEARBLEND; Serial.println("SetupTotallyRandomPalette NOBLEND");}
        if( secondHand == 30)  { SetupGreenAndRedStripedPalette();       currentBlending = NOBLEND; Serial.println("SetupBlackAndWhiteStripedPalette NOBLEND");}
        if( secondHand == 35)  { SetupGreenAndRedStripedPalette();       currentBlending = LINEARBLEND; Serial.println("SetupBlackAndWhiteStripedPalette LINEARBLEND");}
        if( secondHand == 40)  { currentPalette1 = myChristmasPalette_p;           currentBlending = LINEARBLEND; Serial.println("myChristmasPalette_p LINEARBLEND");}
        if( secondHand == 45)  { currentPalette1 = PartyColors_p;           currentBlending = LINEARBLEND; Serial.println("PartyColors_p LINEARBLEND");}
        if( secondHand == 50)  { currentPalette1 = myRedWhiteBluePalette_p; currentBlending = NOBLEND;  Serial.println("myRedWhiteBluePalette_p NOBLEND");}
        if( secondHand == 55)  { currentPalette1 = myRedWhiteBluePalette_p; currentBlending = LINEARBLEND; Serial.println("myRedWhiteBluePalette_p LINEARBLEND");}
    }
}

// This function fills the palette with totally random colors.
void SetupTotallyRandomPalette()
{
    for( int i = 0; i < 16; i++) {
        currentPalette1[i] = CHSV( random8(), 255, random8());
    }
}

// This function sets up a palette of black and white stripes,
// using code.  Since the palette is effectively an array of
// sixteen CRGB colors, the various fill_* functions can be used
// to set them up.
void SetupGreenAndRedStripedPalette()
{
    // 'black out' all 16 palette entries...
    fill_solid( currentPalette1, 16, CRGB::Black);
    // and set every fourth one to white.
    currentPalette1[0] = CRGB::White;
    currentPalette1[4] = CRGB::White;
    currentPalette1[8] = CRGB::White;
    currentPalette1[12] = CRGB::White;

}

// This function sets up a palette of purple and green stripes.
void SetupRedBlueAndGreenPalette()
{
    CRGB purple = CHSV( HUE_PURPLE, 255, 255);
    CRGB green  = CHSV( HUE_GREEN, 255, 255);
    CRGB black  = CRGB::Black;

    currentPalette1 = CRGBPalette16(
                                   green,  green,  black,  black,
                                   purple, purple, black,  black,
                                   green,  green,  black,  black,
                                   purple, purple, black,  black );
}


// This example shows how to set up a static color palette
// which is stored in PROGMEM (flash), which is almost always more
// plentiful than RAM.  A static PROGMEM palette like this
// takes up 64 bytes of flash.
const TProgmemPalette16 myRedWhiteBluePalette_p PROGMEM =
{
    CRGB::Red,
    CRGB::Gray, // 'white' is too bright compared to red and blue
    CRGB::Blue,
    CRGB::Black,

    CRGB::Red,
    CRGB::Gray,
    CRGB::Blue,
    CRGB::Black,

    CRGB::Red,
    CRGB::Red,
    CRGB::Gray,
    CRGB::Gray,
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Black,
    CRGB::Black
};

const TProgmemPalette16 myChristmasPalette_p PROGMEM =
{
    CRGB::Red,
    CRGB::Gray, // 'white' is too bright compared to red and blue
    CRGB::Blue,
    CRGB::Green,

    CRGB::Red,
    CRGB::Yellow,
    CRGB::Blue,
    CRGB::Green,

    CRGB::Red,
    CRGB::Red,
    CRGB::Gray,
    CRGB::Yellow,
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Green,
    CRGB::Green
};

