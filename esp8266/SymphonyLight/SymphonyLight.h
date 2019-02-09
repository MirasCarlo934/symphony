/*
 * SymphonyLight.cpp
 *
 *  Created on: Oct 24, 2018
 *      Author: cels
 */
#include <FastLED.h>
#include "Symphony.h"

#ifndef SYMPHONYLIGHT_H_
#define SYMPHONYLIGHT_H_

#define LED_STRINGS    2	//this corresponds to the number of strings in vixen
#define NUM_PIXELS    30	//this corresponds to the number of pixels in vixen
#define LED_PIN1     14
#define LED_PIN2     12
#define LED_PIN3     13
#define LED_PIN4     5
//#define LED_TYPE    WS2812B
#define LED_TYPE    WS2812
//#define BRIGHTNESS  64
#define BRIGHTNESS  200
#define FRAMES_PER_SECOND 60
#define COLOR_ORDER GRB
#define CHANNEL_START 1 /* Channel to start listening at */
#define UNIVERSE_START 1
#define UNIVERSE_COUNT 7
#define UPDATES_PER_SECOND 100
// There are two main parameters you can play with to control the look and
// feel of your fire: COOLING (used in step 1 above), and SPARKING (used
// in step 3 above).
//
// COOLING: How much does the air cool as it rises?
// Less cooling = taller flames.  More cooling = shorter flames.
// Default 55, suggested range 20-100
#define COOLING  55

// SPARKING: What chance (out of 255) is there that a new spark will be lit?
// Higher chance = more roaring fire.  Lower chance = more flickery fire.
// Default 120, suggested range 50-200.
#define SPARKING 120

uint16_t myUniverse = 1;
//CRGB theLeds[LED_STRINGS][NUM_PIXELS];
CRGB **theLeds = NULL;		//the led matrix

struct lightStruct {
	uint8_t pin;
	bool fwdDirection;
};
lightStruct *lights = NULL;

struct cycleStruct {
	uint8_t pattern = 0;		//the sequence pattern ["Flow", "Solid", "Fade", "Twinkle"];
	bool visible = true;		//indicates if the pattern is visible
	CRGBPalette16 palette;		//the palette for this sequence pattern
	String name;				//the name of this pattern
};
struct sequenceStruct {
	uint8_t pattern = 0;		//the sequence pattern ["Flow", "Solid", "Fade", "Twinkle"];
	uint8_t color = 0;			//indicates color for the pattern or the color palette
	uint8_t duration = 0;		//the duration for the pattern
};

CRGBPalette16 currentPalette1;
const int maxPattern = 12;
cycleStruct palettes[maxPattern];
sequenceStruct sequenceArray[maxPattern];

TBlendType    currentBlending = LINEARBLEND;
uint8_t effect = 0;
uint8_t paletteIndex = 0;
uint8_t brightness = 0;
int indexStart = 0;
bool gReverseDirection = false;
bool toggle = true;
bool isShow = true;
long start = millis();

Filemanager	file = 	Filemanager();
String lightConfigFile = "/light.json";
Symphony s = Symphony();
bool isNormal = true;
enum sequences : uint8_t { CYCLE, FIRE, PICKER, TWINKLE, SPARKLE};
sequences seq = CYCLE;
int stringCount = LED_STRINGS;
int pixelCount = NUM_PIXELS;
int mirrored = false;

// This function sets up a palette of red blue and green stripes.
const TProgmemPalette16 myRedGreenBluePalette_p PROGMEM =
{
	CRGB::Red,
	CRGB::Green,
	CRGB::Blue,
	CRGB::Red,
	CRGB::Green,
	CRGB::Blue,
	CRGB::Red,
	CRGB::Green,
	CRGB::Blue,
	CRGB::Red,
	CRGB::Green,
	CRGB::Blue,
	CRGB::Red,
	CRGB::Green,
	CRGB::Blue,
	CRGB::Red
};
const TProgmemPalette16 myOrangeWhiteBluePalette_p PROGMEM =
{
    CRGB::Orange,
    CRGB::Gray, // 'white' is too bright compared to red and blue
    CRGB::Blue,
    CRGB::Orange,
    CRGB::Gray,
    CRGB::Blue,
	CRGB::Orange,
	CRGB::White,
	CRGB::Blue,
	CRGB::Orange,
	CRGB::White,
	CRGB::Blue,
	CRGB::Orange,
	CRGB::White,
	CRGB::Blue,
    CRGB::Orange
};

const TProgmemPalette16 myRedWhiteBluePalette_p PROGMEM =
{
    CRGB::Red,
    CRGB::White, // 'white' is too bright compared to red and blue
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Red,
    CRGB::White,
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Red,
    CRGB::Red,
    CRGB::White,
    CRGB::White,
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Red,
    CRGB::Red
};
const TProgmemPalette16 myChristmasPalette_p PROGMEM =
{
    CRGB::Red,
    CRGB::White,
    CRGB::Blue,
    CRGB::Green,
    CRGB::Red,
    CRGB::Yellow,
    CRGB::Blue,
    CRGB::Green,
    CRGB::Red,
    CRGB::Red,
    CRGB::White,
    CRGB::Yellow,
    CRGB::Blue,
    CRGB::Blue,
    CRGB::Green,
    CRGB::Green
};

// Fire2012 with programmable Color Palette
//
// This code is the same fire simulation as the original "Fire2012",
// but each heat cell's temperature is translated to color through a FastLED
// programmable color palette, instead of through the "HeatColor(...)" function.
//
// Four different static color palettes are provided here, plus one dynamic one.
//
// The three static ones are:
//   1. the FastLED built-in HeatColors_p -- this is the default, and it looks
//      pretty much exactly like the original Fire2012.
//
//  To use any of the other palettes below, just "uncomment" the corresponding code.
//
//   2. a gradient from black to red to yellow to white, which is
//      visually similar to the HeatColors_p, and helps to illustrate
//      what the 'heat colors' palette is actually doing,
//   3. a similar gradient, but in blue colors rather than red ones,
//      i.e. from black to blue to aqua to white, which results in
//      an "icy blue" fire effect,
//   4. a simplified three-step gradient, from black to red to white, just to show
//      that these gradients need not have four components; two or
//      three are possible, too, even if they don't look quite as nice for fire.
//
// The dynamic palette shows how you can change the basic 'hue' of the
// color palette every time through the loop, producing "rainbow fire".

CRGBPalette16 gPal;

void setupFire(uint8_t i) {
//  delay(3000); // sanity delay
//  FastLED.addLeds<LED_TYPE, LED_PIN1, COLOR_ORDER>(theLeds[0], NUM_PIXELS).setCorrection( TypicalLEDStrip );
//  FastLED.addLeds<LED_TYPE, LED_PIN2, COLOR_ORDER>(theLeds[1], NUM_PIXELS).setCorrection( TypicalLEDStrip );
//  FastLED.setBrightness( BRIGHTNESS );

  if (i == 0) {
	  // This first palette is the basic 'black body radiation' colors,
	  // which run from black to red to bright yellow to white.
	  gPal = HeatColors_p;
  }

  // These are other ways to set up the color palette for the 'fire'.
  // First, a gradient from black to red to yellow to white -- similar to HeatColors_p
  //   gPal = CRGBPalette16( CRGB::Black, CRGB::Red, CRGB::Yellow, CRGB::White);

  if (i == 1) {
  // Second, this palette is like the heat colors, but blue/aqua instead of red/yellow
     gPal = CRGBPalette16( CRGB::Black, CRGB::Blue, CRGB::Aqua,  CRGB::White);
  }

  // Third, here's a simpler, three-step gradient, from black to red to white
  //   gPal = CRGBPalette16( CRGB::Black, CRGB::Red, CRGB::White);

  if (i==2) {
	  gPal = ForestColors_p;
  }
}


// Fire2012 by Mark Kriegsman, July 2012
// as part of "Five Elements" shown here: http://youtu.be/knWiGsmgycY
////
// This basic one-dimensional 'fire' simulation works roughly as follows:
// There's a underlying array of 'heat' cells, that model the temperature
// at each point along the line.  Every cycle through the simulation,
// four steps are performed:
//  1) All cells cool down a little bit, losing heat to the air
//  2) The heat from each cell drifts 'up' and diffuses a little
//  3) Sometimes randomly new 'sparks' of heat are added at the bottom
//  4) The heat from each cell is rendered as a color into the leds array
//     The heat-to-color mapping uses a black-body radiation approximation.
//
// Temperature is in arbitrary units from 0 (cold black) to 255 (white hot).
//
// This simulation scales it self a bit depending on NUM_LEDS; it should look
// "OK" on anywhere from 20 to 100 LEDs without too much tweaking.
//
// I recommend running this simulation at anywhere from 30-100 frames per second,
// meaning an interframe delay of about 10-35 milliseconds.
//
// Looks best on a high-density LED setup (60+ pixels/meter).
//
//

void Fire2012WithPalette(lightStruct light, int ledStringIndex)
{
// Array of temperature readings at each simulation cell
  static byte heat[NUM_PIXELS];

  // Step 1.  Cool down every cell a little
    for( int i = 0; i < pixelCount; i++) {
      heat[i] = qsub8( heat[i],  random8(0, ((COOLING * 10) / pixelCount) + 2));
    }

    // Step 2.  Heat from each cell drifts 'up' and diffuses a little
    for( int k= pixelCount - 1; k >= 2; k--) {
      heat[k] = (heat[k - 1] + heat[k - 2] + heat[k - 2] ) / 3;
    }

    // Step 3.  Randomly ignite new 'sparks' of heat near the bottom
    if( random8() < SPARKING ) {
      int y = random8(7);
      heat[y] = qadd8( heat[y], random8(160,255) );
    }

    // Step 4.  Map from heat cells to LED colors
    for( int j = 0; j < pixelCount; j++) {
      // Scale the heat value from 0-255 down to 0-240
      // for best results with color palettes.
      byte colorindex = scale8( heat[j], 240);
      CRGB color = ColorFromPalette( gPal, colorindex);
      int pixelnumber;
      if( gReverseDirection ) {
        pixelnumber = (pixelCount-1) - j;
      } else {
        pixelnumber = j;
      }
      for (int i=0; i<stringCount;i++)
    	  theLeds[ledStringIndex][pixelnumber] = color;
    }
}
void loopFire()
{
  // Add entropy to random number generator; we use a lot of it.
//  random16_add_entropy( random());

  // Fourth, the most sophisticated: this one sets up a new palette every
  // time through the loop, based on a hue that changes every time.
  // The palette is a gradient from black, to a dark color based on the hue,
  // to a light color based on the hue, to white.
  //
  //   static uint8_t hue = 0;
  //   hue++;
  //   CRGB darkcolor  = CHSV(hue,255,192); // pure hue, three-quarters brightness
  //   CRGB lightcolor = CHSV(hue,128,255); // half 'whitened', full brightness
  //   gPal = CRGBPalette16( CRGB::Black, darkcolor, lightcolor, CRGB::White);

//		  Fire2012WithPalette(); // run simulation frame, using palette colors
		for (int i=0; i<stringCount; i++) {
			Fire2012WithPalette(lights[i], i); // run simulation frame, using palette colors
		}
		FastLED.show(); // display this frame
		FastLED.delay(1000 / FRAMES_PER_SECOND);
}

/***********************************************
 * Light show section
 ************************************************/

void Sparkle() {
	static bool sparkle = true;
	sparkle = !sparkle;
	int pix1 = random(NUM_PIXELS);
	int pix2 = random(NUM_PIXELS);
	int pix3 = random(NUM_PIXELS);
	for(int j=0; j<stringCount;j++) {
		theLeds[j][pix1] = CRGB::White;
		theLeds[j][pix2] = CRGB::Red;
		theLeds[j][pix3] = CRGB::Green;
	}
	FastLED.show();
	delay(50);
	for(int j=0; j<stringCount;j++) {
		theLeds[j][pix1] = CRGB::Black;
		theLeds[j][pix2] = CRGB::Black;
		theLeds[j][pix3] = CRGB::Black;
	}
}
/**
 * Creates the pixel colors from the pallete.
 * We put a black pixel on every even numbered pixel to simulate twinkle
  */
void TwinkleLEDsFromPaletteColors( uint8_t colorIndex, uint8_t brightness)
{
	static bool twinkle = true;
	twinkle = !twinkle;
    long i = random(NUM_PIXELS);
    int cIndex = random(16);
    CRGB c = ColorFromPalette( palettes[paletteIndex].palette, cIndex, brightness, currentBlending);
	for(int j=0; j<LED_STRINGS;j++) {
		theLeds[j][i] = c;
	}
}
/**
 * Creates the pixel colors from the pallete.
 * increment determines how smooth the color is.
 * 3 gives a smooth flowing color
 * 16 gives the actual color sequence from the palette
 */
void FillLEDsFromPaletteColors( uint8_t colorIndex, uint8_t brightness, uint8_t increment)
{

	for (int i=0; i<pixelCount;i++) {
		for (int j=0; j<stringCount;j++){
			theLeds[j][i] = ColorFromPalette( palettes[paletteIndex].palette, colorIndex, brightness, currentBlending);
		}
		colorIndex += increment;
	}
}
/*
 * the overloaded FillLEDsFromPaletteColors, used for showing a fixed brightness
 */
void FillLEDsFromPaletteColors( uint8_t colorIndex, uint8_t i)
{
	FillLEDsFromPaletteColors(colorIndex, 255, i);
}
/**
 * Creates the pixel colors from the palette.
 */
void FillLEDsAsSolid()
{
    int colorIndex=0;
    uint8_t brightness = 255;
	for (int i=0; i<pixelCount;i++) {
		for (int j=0; j<stringCount;j++){
			theLeds[j][i] = ColorFromPalette( currentPalette1, colorIndex, brightness, LINEARBLEND);
		}
		colorIndex += 3;
	}
}
CRGBPalette16 SetupPalette(CRGB::HTMLColorCode color)
{
	CRGBPalette16 retPalette;
    fill_solid( retPalette, 16, color);
    return retPalette;
}
void SetupGreenAndRedStripedPalette()
{
	CRGBPalette16 retPalette;
    fill_solid( retPalette, 16, CRGB::Green);
    // and set every fourth one to Red.
    retPalette[0] = CRGB::Red;
    retPalette[4] = CRGB::Red;
    retPalette[8] = CRGB::Red;
    retPalette[12] = CRGB::Red;

}
CRGBPalette16 SetupGreenRedWhiteStripedPalette()
{
	CRGB red = CHSV( HUE_RED, 255, 255);
	CRGB green  = CHSV( HUE_GREEN, 255, 255);
	CRGB white  = CHSV( 255, 255, 255);

	return CRGBPalette16(
	   green,  green,  white,  white,
	   red, red, white,  white,
	   green,  green,  white,  white,
	   red, red, white,  white );
}
CRGBPalette16 SetupYellowBlueWhiteStripedPalette()
{
	CRGB yellow = CHSV( HUE_YELLOW, 255, 255);
	CRGB blue  = CHSV( HUE_BLUE, 255, 255);
	CRGB white  = CHSV( 255, 255, 255);

	return CRGBPalette16(
	   yellow,  yellow,  white,  white,
	   yellow, yellow, white,  white,
	   yellow,  yellow,  white,  white,
	   yellow, yellow, white,  white );
}
void ChangePalettePeriodically()
{
    uint8_t secondHand = (millis() / 1000) % 60;
    static uint8_t lastSecond = 99;

    if( lastSecond != secondHand) {
        lastSecond = secondHand;
        if (secondHand % 5 == 0) {
        	paletteIndex++;
        	DynamicJsonBuffer jsonBuffer;
			JsonObject& reply = jsonBuffer.createObject();
			reply["core"] = CORE_TOCHILD;//the command
			reply["cmd"] = 0;//the task, 0=showPattern;
			JsonObject& data = reply.createNestedObject("data");
			data["index"] = paletteIndex;
			data["pattern"] = palettes[paletteIndex].pattern;
			data["show"] = palettes[paletteIndex].visible ? "t" : "f";
			s.textAll(reply);//we are broadcasting to the connected clients
        }
        if (paletteIndex >= maxPattern)
        	paletteIndex = 0;
    }
}

void fadeAll() {
	for (int i=0; i<pixelCount;i++) {
		for (int j=0; j<stringCount;j++){
			theLeds[j][i].nscale8(250);
		}
	}
	/*for(int i = 0; i < NUM_PIXELS; i++) {
		for (int j = 0; j < LED_STRINGS; j++) {
			theLeds[j][i].nscale8(250);
//			theLeds[j][i] = CRGB::Black;
		}
	}*/
}


void testColors(int startIndex)
{
	CRGB colors[4] = {CRGB::Red, CRGB::Blue, CRGB::Green, CRGB::Black};
	int k = startIndex;
    for( int i = 0; i < NUM_PIXELS; i++) {
    	CRGB theColor = colors[k];
    	k++;
    	if (k >= 4)
    		k=0;
    	for(int j=0; j<LED_STRINGS;j++) {
			theLeds[j][i] = theColor;
    	}
    }
}
/**
 * the sequence for normal stand alone effects
 */
void doNormalRoutine() {
	static uint8_t startIndex = 0;
	uint8_t pattern = palettes[paletteIndex].pattern;
	ChangePalettePeriodically();
	switch (pattern) {
		case 0: {//Flow pattern
			if (isShow) {
				startIndex = startIndex + 1; /* motion speed */
				FillLEDsFromPaletteColors( startIndex, 3);
				FastLED.show();
				isShow = false;
			}
			if (millis() - start >= 10) {	//toggle every 10ms
				isShow = true;
				start = millis();
			}
			break;
		}
		case 1: {//Solid pattern
			if (toggle) {
				FillLEDsFromPaletteColors( startIndex, 16);
				FastLED.show();
				toggle = false;
			}
			if (millis() - start >= 1000) {	//toggle every 1s
				toggle = true;
				start = millis();
			}
			break;
		}
		case 2: {//Fadein/fadeout pattern
			if (toggle) {
				FillLEDsFromPaletteColors( startIndex, brightness, 16);
				if (brightness >= 255)
					brightness = 255;
				else
					brightness+=3;
			} else {
				fadeAll();
			}
			FastLED.show();
			if (millis() - start >= 1000) {	//toggle every 1s
				toggle = !toggle;
				start = millis();
				startIndex = startIndex + 1; /* motion speed */
				brightness = 0;
			}
			break;
		}
		case 3: {//Twinkle pattern
			if (toggle) {
				FastLED.show();
				toggle = false;
			}
			if (millis() - start >= 10) {	//toggle every 10ms
				toggle = true;
				start = millis();
			}
			break;
	    }
	 }
}

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

/**
 * For filling the leds directly using palettes
 */
void FillLEDsDirectFromPaletteColors()
{

	for (int i=0; i<pixelCount;i++) {
		for (int j=0; j<stringCount;j++){
			theLeds[j][i] = palettes[paletteIndex].palette[j];
		}
	}
}
#endif /* SYMPHONYLIGHT_H_ */


