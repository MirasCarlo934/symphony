#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2018-09-18 22:14:05

#include "Arduino.h"
#include "Arduino.h"
#include "SymphonyCore.h"
#include <FastLED.h>

int WsCallback(uint8_t * payload, size_t length) ;
int handleCommand(int index, int cmd) ;
attribStruct MyMqttCallback(attribStruct property, int scmd) ;
void setTimer(int timeMins) ;
void startTimer() ;
void handleOn();
void handleOff();
void handleToggle();
void setup() ;
void FillLEDsFromPaletteColors( uint8_t colorIndex) ;
void FillLEDsFromPaletteColorsReverse( uint8_t colorIndex) ;
void FillGroupedLEDsFromPaletteColors( uint8_t colorIndex) ;
void FillGroupedLEDsFromPaletteColorsReverse( uint8_t colorIndex) ;
void ChangePalettePeriodically() ;
void SetupTotallyRandomPalette() ;
void SetupBlackAndWhiteStripedPalette() ;
void SetupSingleWhite() ;
void SetupPurpleAndGreenPalette() ;
void SetupWarmWhitePalette() ;
void SetupCoolWhiteStripedPalette() ;
void SetupOffPalette() ;
void loop() ;
void ledloop() ;


#include "SymphonyLed.ino"

#endif
