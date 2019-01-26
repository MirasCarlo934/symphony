/**
 * This is a smart light decor.  Randomly shows light sequences.
 * But if it receives E1.31 data, it will synchronize to that if it receives a universe it recognizes.
 */
#include "Arduino.h"
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include <FastLED.h>
#include "Symphony.h"

//#define DEBUG_PIXELS
#define MY_VERSION 1.25
#define LED_STRINGS    2	//this corresponds to the number of strings in vixen
#define NUM_PIXELS    30	//this corresponds to the number of pixels in vixen
#define LED_PIN1     14
#define LED_PIN2     12
#define LED_TYPE    WS2812B
#define BRIGHTNESS  64
#define COLOR_ORDER GRB
#define CHANNEL_START 1 /* Channel to start listening at */
#define UNIVERSE_START 1
#define UNIVERSE_COUNT 7
#define UPDATES_PER_SECOND 100


uint16_t myUniverse = 1;
CRGB theLeds[NUM_PIXELS];
CRGBPalette16 currentPalette1;
TBlendType    currentBlending;
uint16_t countE131 = 0;  //count to be used to to determine if we are receiving any E131 packets
long timerMilli = 0;
long start = millis();
long twinkleStart = millis();

bool isNormal = true;
uint8_t effect = 1;

Symphony s = Symphony();

void handleColor(AsyncWebServerRequest *request) {
	int red, green, blue, index = 0;
	if(request->hasParam("r")) {
		AsyncWebParameter* r = request->getParam("r");
		red = atoi(r->value().c_str());
		AsyncWebParameter* g = request->getParam("g");
		green = atoi(g->value().c_str());
		AsyncWebParameter* b = request->getParam("b");
		blue = atoi(b->value().c_str());
		AsyncWebParameter* i = request->getParam("i");
		index = atoi(i->value().c_str());
		theLeds[index].setRGB(red, green, blue);
		FastLED.show();
	}
	request->send(200, "text/html", "Done.");
}

//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	s.setup("Test");
	delay(10);
	Serial.println("\n\n************START TestWS2812B Setup***************");
	FastLED.setBrightness(  BRIGHTNESS );
	FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds, NUM_PIXELS).setCorrection( TypicalLEDStrip );
	FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds, NUM_PIXELS).setCorrection( TypicalLEDStrip );
	s.on("/set", HTTP_GET, handleColor);
	Serial.printf("\n************END TestWS2812B Setup Version%.2f***************\n", MY_VERSION);
}

void testColors()
{
	CRGB colors[4] = {CRGB::Red, CRGB::Blue, CRGB::Green, CRGB::Black};
	int k = 0;
    for( int i = 0; i < NUM_PIXELS; i++) {
    	CRGB theColor = colors[k];
    	k++;
    	if (k >= 4)
    		k=0;
		theLeds[i].setRGB(theColor.red, theColor.green, theColor.blue);
    }
}

// The loop function is called in an endless loop
void loop()
{
	if (s.loop()) {
//		testColors();
//		FastLED.show();
//		FastLED.delay(10);
	}
}
