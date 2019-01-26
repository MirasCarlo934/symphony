/*
 * SymphonyLightNew.h
 *
 *  Created on: Dec 2, 2018
 *
 *	Object that does the light effects.
 *	Effects are:
 *		1. Flow - continuous flow of colors using palette
 *		2. Wipe - fills the led string with color similar to centipede
 *		3. Sparkle - randomly lights the led string with a single color
 *		4. Solid - fills all leds with a single color
 *		5. Fill - fills first led with single color, then second, then third, until all the leds
 *		6. Fire - fire effect
 */

#ifndef SYMPHONYLIGHTNEW_H_
#define SYMPHONYLIGHTNEW_H_

#include <Arduino.h>
#include <ArduinoJson.h>
#include <FastLED.h>
#include "Symphony.h"

#define LED_STRINGS    2	//this corresponds to the number of strings in vixen
#define NUM_PIXELS    50	//this corresponds to the number of pixels in vixen
#define LED_PIN1     14
#define LED_PIN2     12
#define LED_TYPE    WS2812B
#define BRIGHTNESS  200
#define FRAMES_PER_SECOND 60
#define COLOR_ORDER GRB
#define CHANNEL_START 1 /* Channel to start listening at */
#define UNIVERSE_START 1
#define UNIVERSE_COUNT 7
#define UPDATES_PER_SECOND 100

uint16_t myUniverse = 1;
CRGB **theLeds = NULL;		//the led matrix

struct LedMatrix {
	int strings;		//the number of pixel strings
	int pixels;			//the number of pixels per string
	CRGB **theLeds;		//the led matrix
};

LedMatrix leds;

enum effects : uint8_t { FLOW, WIPE, SPARKLE, SOLID, FILL, FIRE};
Filemanager	file = 	Filemanager();
String lightConfigFile = "/light.json";
Symphony s = Symphony();
bool isNormal = true;

/**
 * 	returns the rgb taken from the E1.31 stream for the specified led Index given a StringIndex, PixelCount and Led Pixel Index
 * 	params:
 * 		packet			=	the E1.31 packet
 * 		pixCount 		=	number of pixels in the string
 * 		stringIndex		=	the index of Pixel String
 * 		ledIndex		=	the index of the pixel
 *
 *	stringStartChannel = 3*stringIndex*pixCount + 1  //the start channel of the string
 * 	r	stringStartChannel + ledIndex*3 + 0
 *	g	stringStartChannel + ledIndex*3 + 1
 *	b	stringStartChannel + ledIndex*3 + 2
 */
CRGB getPixColorFromE131Stream(e131_packet_t packet, uint8_t pixCount, uint8_t stringIndex, uint8_t ledIndex) {
	uint8_t stringStartChannel = 3*stringIndex*pixCount + 1;
	uint8_t r = packet.property_values[stringStartChannel + ledIndex*3 + 0];
	uint8_t g = packet.property_values[stringStartChannel + ledIndex*3 + 2];
	uint8_t b = packet.property_values[stringStartChannel + ledIndex*3 + 1];
	CRGB rgb = CRGB(r, g, b);
	return rgb;
}

LedMatrix createLedMatrix(int stringCount, int pixCount) {	//creates a led of stringCount strings with pixCount pixels
	//create an array of [stringCount][pixCount];
	LedMatrix ledM;
	ledM.strings = stringCount;
	ledM.pixels = pixCount;
	ledM.theLeds = new CRGB*[stringCount];
	for(int i = 0; i < stringCount; i++)
		ledM.theLeds[i] = new CRGB[pixCount];
	return ledM;
}

class LightEffect {
  public:
	uint8_t effect = FLOW;
    LedMatrix leds;
    long prevMillis;		//the previous millis
    long refreshRate = 100;

	LightEffect();
    LightEffect(LedMatrix l) {	//creates a led of stringCount strings with pixCount pixels
    	leds = l;
        prevMillis = millis();
    }
	virtual bool run() {	//function that executes the effect based on the duration
		long now = millis();
		bool isShow = false;
		if (now - prevMillis >= refreshRate) {
			Serial.printf( "Base, run! effect=%i counter=%i, pixel=%i\n", effect, counter, leds.pixels);
			prevMillis = now;
			isShow = true;
//			leds.theLeds[0][counter] = CRGB::Red;
//			FastLED.show();
			counter++;
			if (counter >= leds.pixels)
				counter = 0;
		}
		return isShow;
	}
    void setRefreshRate(long refreshMs) {	//refresh rate in millis, the frequency that this effect will call the Fastled.show
        refreshRate = refreshMs;
    }
    void setDuration(long durationMs) {	//duration in millis, the duration where the effect will be displayed
        duration = durationMs;
    }
    bool isShow() {
    	long now = millis();
		bool isShow = false;
		if (now - prevMillis >= refreshRate) {
			Serial.print( "Base, run!\n");
			prevMillis = now;
			isShow = true;
		}
		return isShow;
    }
    void print() {
    	Serial.print("I am instance of ");
    	switch (effect) {
			case FLOW:
				Serial.println("FLOW");
				break;
			case WIPE:
				Serial.println("WIPE");
				break;
			default:
				break;
		}
    }
  private:
	bool visible = true;		//indicates if the pattern is visible
	long duration = 15000;		//the duration for the effect (in ms)
	int counter = 0;
};
/**
 * The Object that creates the flowing effect
 */
class FlowEffect: public LightEffect {
	public:
	FlowEffect(LedMatrix l):LightEffect(l) {
		Serial.print( "FlowEffect, constructor!\n");
        effect = FLOW;
    }
    bool run() {
    	long now = millis();
		bool isShow = false;
		if (now - prevMillis >= refreshRate) {
			Serial.printf( "Flow, run! counter=%i, pixels=%i\n", counter, leds.pixels);
			prevMillis = now;
			isShow = true;
			counter++;
			if (counter >= leds.pixels)
				counter = 0;
		}
		return isShow;
//        if (LightEffect::isShow()) {
//        	Serial.printf( "FlowEffect, run counter=%i!\n", counter);
//        	leds.theLeds[0][counter] = CRGB::Red;
//        	FastLED.show();
//        	counter++;
//        	if (counter >= leds.pixels)
//				counter = 0;
//        }
    }
	private:
    int counter = 0;
};
/**
 * The Object that creates the wipe effect
 */
class WipeEffect: public LightEffect {
	public:
	WipeEffect(LedMatrix l):LightEffect(l) {
        effect = WIPE;
        Serial.print( "WipeEffect, constructor!\n");
    }
    bool run() {
    	long now = millis();
		bool isShow = false;
		if (now - prevMillis >= refreshRate) {
			Serial.printf( "Wipe, run! counter=%i pixels=%i\n", counter, leds.pixels);
			prevMillis = now;
			isShow = true;
			counter++;
			if (counter >= leds.pixels)
				counter = 0;
		}
		return isShow;
//        if (LightEffect::isShow()) {
//        	Serial.printf( "WipeEffect, run counter=%i!\n", counter);
//        	leds.theLeds[0][counter] = CRGB::Blue;
//        	FastLED.show();
//			counter++;
//			if (counter >= leds.pixels)
//				counter = 0;
//        }
    }
	private:
	int counter = 0;
};

/*
int main()
{
    std::chrono::steady_clock::time_point start= std::chrono::steady_clock::now();

    std::cout << "Hello, world!\n";
    FlowEffect flow = FlowEffect(2,5);
    WipeEffect wipe = WipeEffect(3,3);
    wipe.setRefreshRate(30);
    LightEffect* light[2];
    light[0] = &flow;
    light[1] = &wipe;
    for (int i=0; i<2; i++)
        light[i]->run();
    std::chrono::steady_clock::time_point end= std::chrono::steady_clock::now();

    while (std::chrono::duration_cast<std::chrono::microseconds>(end - start).count() <= 1000) {
        //std::cout << "Elapsed time in microseconds : " <<std::chrono::duration_cast<std::chrono::microseconds>(end - start).count() << " mics" << std::endl;
        for (int i=0; i<2; i++)
            light[i]->run();
        end= std::chrono::steady_clock::now();
    }
}
*/
#endif /* SYMPHONYLIGHTNEW_H_ */
