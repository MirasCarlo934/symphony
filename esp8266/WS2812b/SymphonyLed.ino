#include "Arduino.h"
#include "SymphonyCore.h"
#include <FastLED.h>

#define LED_PIN     12
#define NUM_LEDS    61
#define BRIGHTNESS  64
#define LED_TYPE    WS2811
#define COLOR_ORDER GRB

CRGB theLeds[NUM_LEDS];

#define UPDATES_PER_SECOND 100

Symphony s = Symphony();
SymphProduct product = SymphProduct();
int buffTimerMins = 1800000; //default timer is 30mins
boolean timerStarted = false;
int duration = 1800000;  //default timer is 30mins
int loopDelay = 30;  //the delay in millis
int switchPin = 14;
boolean isRandom = true;
boolean isGrouped = false;
int groups = 1;
/*
1	Switch1
2	Timer
3	StartTimer
*/
enum propertyIndex : uint8_t {
	Warmwhite = 0,	//digital
	Red = 1,
	Random = 2,
	Sequence = 3,
	Off = 4,
	Group = 5,
	Groups = 6,
	Timer = 8,		//analog
	StartTimer = 9,	//digital
};

CRGBPalette16 currentPalette;
TBlendType    currentBlending;

extern CRGBPalette16 myRedWhiteBluePalette;
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
int handleCommand(int index, int cmd) {
  Serial.printf("handleCommand index=%d cmd=%d\n",index,cmd);
  switch (index) {
    case Warmwhite:
      Serial.println("WARMWHITE");
      isRandom = false;
      SetupWarmWhitePalette();
      currentBlending = NOBLEND;
      break;
    case Red:
      Serial.println("RED");
      isRandom = false;
      SetupCoolWhiteStripedPalette();
      currentBlending = LINEARBLEND;
      break;
    case Random:
	  Serial.println("RANDOM");
	  isRandom = true;
	  break;
    case Sequence:
	  Serial.println("Sequence");
	  isRandom = false;
	  SetupSingleWhite();
	  break;
    case Off:
	  Serial.println("Off");
	  SetupOffPalette();
	  isRandom = false;
	  break;
    case Group:
	  Serial.println("Group");
	  isGrouped = !isGrouped;
	  break;
    case Groups:
	  Serial.println("Groups");
	  groups = cmd;
	  break;
    case StartTimer:
      Serial.println("STARTTIMER");
  	  startTimer();
	  break;
    case Timer:
      Serial.println("TIMER");
      setTimer(cmd);
	  break;
  }
  return 23;
}
/*
 * Callback for MQTT events
 */
attribStruct MyMqttCallback(attribStruct property, int scmd) {

//	MqttUtil::product.setValue(property.ssid, scmd);
//	MqttUtil::product.setDone(property.ssid);
//	switch (MqttUtil::product.getProperty(property.ssid).index) {
//		case StartTimer:
//	  	  startTimer();
//		  break;
//	    case Timer:
//	      setTimer(scmd);
//		  break;
//	}
//
	MqttUtil::product.setValue(property.ssid, scmd);
	MqttUtil::product.setDone(property.ssid);
	handleCommand(MqttUtil::product.getProperty(property.ssid).index, scmd);

	return (MqttUtil::product.getProperty(property.ssid));
}
void setTimer(int timeMins) {
	duration = timeMins * 60000;
	buffTimerMins = timeMins * 60000;
	Serial.printf("Timer will be set to %dmins.\n", timeMins);
}

void startTimer() {
	timerStarted = true;
	Serial.printf("Timer started.\n");
}
void handleOn(){
	digitalWrite(switchPin, 1);
	Symphony::setProperty("0006", "1");
	s.sendResponse("Successfully turned on.");
}
void handleOff(){
	digitalWrite(switchPin, 0);
	Symphony::setProperty("0006", "0");
	s.sendResponse("Successfully turned off.");
}
void handleToggle(){
	int state = !digitalRead(switchPin);
//	digitalWrite(12, !digitalRead(12));
	char buffer [3];
	itoa (state,buffer,10);
	MqttUtil::sendCommand("0006", state);
	Symphony::setProperty("0006", buffer);
	s.sendResponse("Success");
}

//The setup function is called once at startup of the sketch
void setup()
{
	Serial.begin(115200);
	Serial.println("************START SymphonyLed Setup***************");
	product.productType = "0011";
	product.room = "U7YY";  //salas
	product.name = "FastLed";
	int pIndex;
	product.addProperty(Warmwhite, "0049", false, SymphProduct::createGui(RADIO_OUT,"WarmWhite",0, 1, 0));
	product.addProperty(Red, "0050", false, SymphProduct::createGui(RADIO_OUT,"Red",0, 1, 0));
	product.addProperty(Random, "0051", false, SymphProduct::createGui(RADIO_OUT,"Random",0, 1, 1));
	product.addProperty(Sequence, "0052", false, SymphProduct::createGui(RADIO_OUT,"Sequence",0, 1, 0));
	product.addProperty(Off, "0053", false, SymphProduct::createGui(BUTTON_OUT,"Off",0, 1, 0));
	product.addProperty(Group, "0054", false, SymphProduct::createGui(BUTTON_OUT,"Group",0, 1, 0));
	product.addProperty(Groups, "0055", false, SymphProduct::createGui(SLIDER_OUT,"Groups",1, 8, 1));
	product.addProperty(StartTimer, "0056", false, SymphProduct::createGui(BUTTON_OUT,"StartTimer",0, 1, 0));
	product.addProperty(Timer, "0057", false, SymphProduct::createGui(SLIDER_OUT,"Timer(Mins)",0, 120, 0));
//	digitalWrite(switchPin, 1);	//our socket is Normally Open, we have to manually set it to on
//	pinMode(LED_PIN, OUTPUT);
//	digitalWrite(LED_PIN, 1);
	delay( 3000 ); // power-up safety delay
	FastLED.addLeds<LED_TYPE, LED_PIN, COLOR_ORDER>(theLeds, NUM_LEDS).setCorrection( TypicalLEDStrip );
	FastLED.setBrightness(  BRIGHTNESS );

	product.print();
	s.setProduct(product);  //always set the product first before running the setup
	s.setWsCallback(&WsCallback);
	s.setMqttCallback(&MyMqttCallback);
	s.on("/on", handleOn);
	s.on("/off", handleOff);
	s.on("/toggle", handleToggle);
	s.setup();
	Serial.println("************END SymphonyLed Setup***************");
}

void FillLEDsFromPaletteColors( uint8_t colorIndex)
{
    uint8_t brightness = 255;

    for( int i = 0; i < NUM_LEDS; i++) {
        theLeds[i] = ColorFromPalette( currentPalette, colorIndex, brightness, currentBlending);
        colorIndex += 3;
    }
}
void FillLEDsFromPaletteColorsReverse( uint8_t colorIndex)
{
    uint8_t brightness = 255;

    for( int i = 0; i < NUM_LEDS; i++) {
        theLeds[NUM_LEDS - 1 - i] = ColorFromPalette( currentPalette, colorIndex, brightness, currentBlending);
        colorIndex += 3;
    }
}

void FillGroupedLEDsFromPaletteColors( uint8_t colorIndex)
{
    uint8_t brightness = 255;
	int ledsPerGroup = NUM_LEDS/groups;
	for (int i=0; i<ledsPerGroup; i++) {
		for( int j = 0; j < groups; j++) {
			theLeds[i+j*ledsPerGroup] = ColorFromPalette( currentPalette, colorIndex, brightness, currentBlending);
		}
		colorIndex += 3;
	}
}
void FillGroupedLEDsFromPaletteColorsReverse( uint8_t colorIndex)
{
    uint8_t brightness = 255;
	int ledsPerGroup = NUM_LEDS/groups;
	for (int i=0; i<ledsPerGroup; i++) {
		for( int j = 0; j < groups; j++) {
			theLeds[ledsPerGroup -1- i+j*ledsPerGroup] = ColorFromPalette( currentPalette, colorIndex, brightness, currentBlending);
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
        if( secondHand ==  0)  {
        	currentPalette = RainbowColors_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("RainbowColors_p LINEARBLEND");
        }
        if( secondHand == 10)  {
        	currentPalette = RainbowStripeColors_p;
        	currentBlending = NOBLEND;
//        	Serial.println("RainbowStripeColors_p NOBLEND");
        }
        if( secondHand == 15)  {
        	currentPalette = RainbowStripeColors_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("RainbowStripeColors_p LINEARBLEND");
        }
        if( secondHand == 20)  {
        	SetupRedBlueAndGreenPalette();
        	currentBlending = LINEARBLEND;
//        	Serial.println("SetupPurpleAndGreenPalette LINEARBLEND");
        }
        if( secondHand == 25)  {
        	SetupTotallyRandomPalette();
        	currentBlending = LINEARBLEND;
//        	Serial.println("SetupTotallyRandomPalette NOBLEND");
        }
        if( secondHand == 30)  {
        	SetupGreenAndRedStripedPalette();
        	currentBlending = NOBLEND;
//        	Serial.println("SetupBlackAndWhiteStripedPalette NOBLEND");
        }
        if( secondHand == 35)  {
//        	SetupBlackAndWhiteStripedPalette();
        	currentPalette = LavaColors_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("SetupBlackAndWhiteStripedPalette LINEARBLEND");
        }
        if( secondHand == 40)  {
        	currentPalette = myChristmasPalette_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("myChristmasPalette_p LINEARBLEND");
        }
        if( secondHand == 45)  {
        	currentPalette = PartyColors_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("PartyColors_p LINEARBLEND");
        }
        if( secondHand == 50)  {
        	currentPalette = myRedWhiteBluePalette_p;
        	currentBlending = NOBLEND;
//        	Serial.println("myRedWhiteBluePalette_p NOBLEND");
        }
        if( secondHand == 55)  {
        	currentPalette = myRedWhiteBluePalette_p;
        	currentBlending = LINEARBLEND;
//        	Serial.println("myRedWhiteBluePalette_p LINEARBLEND");
        }
    }
}

// This function fills the palette with totally random colors.
void SetupTotallyRandomPalette()
{
    for( int i = 0; i < 16; i++) {
        currentPalette[i] = CHSV( random8(), 255, random8());
    }
}

// This function sets up a palette of black and white stripes,
// using code.  Since the palette is effectively an array of
// sixteen CRGB colors, the various fill_* functions can be used
// to set them up.
void SetupGreenAndRedStripedPalette()
{
    // 'black out' all 16 palette entries...
    fill_solid( currentPalette, 16, CRGB::Black);
    // and set every fourth one to white.
    currentPalette[0] = CRGB::White;
    currentPalette[4] = CRGB::White;
    currentPalette[8] = CRGB::White;
    currentPalette[12] = CRGB::White;

}
void SetupSingleWhite()
{
    // 'black out' all 16 palette entries...
    fill_solid( currentPalette, 16, CRGB::Black);
    // and set every fourth one to white.
    currentPalette[0] = CRGB::White;
}
// This function sets up a palette of purple and green stripes.
void SetupRedBlueAndGreenPalette()
{
    CRGB purple = CHSV( HUE_PURPLE, 255, 255);
    CRGB green  = CHSV( HUE_GREEN, 255, 255);
    CRGB black  = CRGB::Black;

    currentPalette = CRGBPalette16(
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

// This function sets up a palette of warmwhite stripes.
void SetupWarmWhitePalette()
{
	// 'black out' all 16 palette entries...
	fill_solid( currentPalette, 16, CRGB::Black);
	// and set every fourth one to white.
	currentPalette[0] = Tungsten40W;
	currentPalette[4] = Tungsten40W;
	currentPalette[8] = Tungsten40W;
	currentPalette[12] = Tungsten40W;
}

void SetupCoolWhiteStripedPalette()
{
    // 'black out' all 16 palette entries...
    fill_solid( currentPalette, 16, CRGB::Black);
    // and set every fourth one to white.
    currentPalette[0] = CoolWhiteFluorescent;
    currentPalette[4] = CoolWhiteFluorescent;
    currentPalette[8] = CoolWhiteFluorescent;
    currentPalette[12] = CoolWhiteFluorescent;

}

void SetupOffPalette()
{
    // 'black out' all 16 palette entries...
    fill_solid( currentPalette, 16, CRGB::Black);
}

void loop()
{
	s.loop();
	delay(loopDelay);
	ledloop();
	if (timerStarted) {
		duration = duration - loopDelay;
//		Serial.printf("Duration is %d\n", duration);
		if ( duration < 0 ) {
			digitalWrite(switchPin, !digitalRead(switchPin));
			Serial.println("Resetting the timer.");
			Symphony::setProperty("0008", "0");
			char buffer [3];
			itoa (digitalRead(switchPin),buffer,10);
			Symphony::setProperty("0006", buffer);
			MqttUtil::sendCommand("0008", 0);
			timerStarted = false;
			duration = buffTimerMins;
		}
	}
}

void ledloop()
{
	if (isRandom)
		ChangePalettePeriodically();

	static uint8_t startIndex = 0;
	startIndex = startIndex + 1; /* motion speed */
	if (!isGrouped)
		FillLEDsFromPaletteColors( startIndex);
	else
		FillGroupedLEDsFromPaletteColors( startIndex);

    FastLED.show();
    FastLED.delay(1000 / UPDATES_PER_SECOND);
}
