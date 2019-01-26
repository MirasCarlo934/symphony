#include "Arduino.h"
#include <E131.h>
#include <FastLED.h>
#include "Symphony.h"

//#define DEBUG_PIXELS
#define MY_VERSION 1.21
#define LED_STRINGS    2	//this corresponds to the number of strings in vixen
#define NUM_PIXELS    30	//this corresponds to the number of pixels in vixen
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

CRGBPalette16 currentPalette1;
TBlendType    currentBlending;

Symphony s = Symphony();

/***********************************************
 * Light show section
 ************************************************/
/**
 * Creates the pixel colors from the pallete.
 */
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
void ChangePalettePeriodically()
{
    uint8_t secondHand = (millis() / 1000) % 60;
    static uint8_t lastSecond = 99;

    if( lastSecond != secondHand) {
    	Serial.printf("secondHand %u\n", secondHand);
        lastSecond = secondHand;
        if( secondHand ==  0)  { currentPalette1 = RainbowColors_p;         currentBlending = LINEARBLEND; Serial.println("RainbowColors_p LINEARBLEND");}
        if( secondHand == 10)  { currentPalette1 = LavaColors_p;   currentBlending = NOBLEND;  Serial.println("RainbowStripeColors_p NOBLEND");}
        if( secondHand == 15)  { currentPalette1 = RainbowStripeColors_p;   currentBlending = LINEARBLEND; Serial.println("RainbowStripeColors_p LINEARBLEND");}
        if( secondHand == 20)  { SetupRedBlueAndGreenPalette();             currentBlending = LINEARBLEND; Serial.println("SetupPurpleAndGreenPalette LINEARBLEND");}
        if( secondHand == 25)  { SetupTotallyRandomPalette();              currentBlending = LINEARBLEND; Serial.println("SetupTotallyRandomPalette NOBLEND");}
        if( secondHand == 30)  { SetupGreenAndRedStripedPalette();       currentBlending = NOBLEND; Serial.println("SetupBlackAndWhiteStripedPalette NOBLEND");}
        if( secondHand == 35)  { SetupGreenAndRedStripedPalette();       currentBlending = LINEARBLEND; Serial.println("SetupBlackAndWhiteStripedPalette LINEARBLEND");}
        if( secondHand == 40)  { currentPalette1 = myChristmasPalette_p;           currentBlending = LINEARBLEND; Serial.println("myChristmasPalette_p LINEARBLEND");}
        if( secondHand == 45)  { currentPalette1 = PartyColors_p;           currentBlending = LINEARBLEND; Serial.println("PartyColors_p LINEARBLEND");}
        if( secondHand == 50)  { currentPalette1 = myRedWhiteBluePalette_p; currentBlending = NOBLEND;  Serial.println("myRedWhiteBluePalette_p NOBLEND");}
        if( secondHand == 55)  { currentPalette1 = CloudColors_p; currentBlending = LINEARBLEND; Serial.println("myRedWhiteBluePalette_p LINEARBLEND");}
    }
}
void handleInit(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Init of SimpleSymphony E1.31 is called.");
	Serial.println("handleInit");
}

//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	delay(10);
	Serial.println("\n\n************START Symphony Music Setup***************");
	s.setup();
	s.on("/init", HTTP_GET, handleInit);
	FastLED.setBrightness(  BRIGHTNESS );
	e131.begin(E131_MULTICAST, myUniverse, 1);
	FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[0], NUM_PIXELS).setCorrection( TypicalLEDStrip );
	FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds[1], NUM_PIXELS).setCorrection( TypicalLEDStrip );
	Serial.printf("\n\n************PIXELS=%d STRINGS=%d***************", NUM_PIXELS, LED_STRINGS);
	Serial.printf("\n************END Symphony Music Setup Version%.3f***************", MY_VERSION);
}

// The loop function is called in an endless loop
void loop()
{
	s.loop();
	/* Parse a packet */
	uint16_t num_channels1 = e131.parsePacket();
	/* Process channel data if we have it */
	if (num_channels1) {
#ifdef DEBUG_PIXELS
		Serial.printf("Listening to Universe%u: thisUniverse %u / %u Channels | Packet#: %u / Errors: %u / CH1: %u,%u,%u\n",
				myUniverse,
				e131.universe,              // The Universe for this packet
				num_channels1,               // Number of channels in this packet
				e131.stats.num_packets,     // Packet counter
				e131.stats.packet_errors,   // Packet error counter
				e131.data[0], e131.data[2], e131.data[1]);              // Dimmer data for Channel 1
#endif
		if ( e131.universe == myUniverse) {
			for (int i = 0; i < NUM_PIXELS; i++) {
				for (int k=0; k< LED_STRINGS;k++) {
					int l = k * NUM_PIXELS + i;
					int j = l * 3 + (CHANNEL_START - 1);
					theLeds[k][i].setRGB(e131.data[j], e131.data[j+2], e131.data[j+1]);
				}
			}
			FastLED.show();
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
