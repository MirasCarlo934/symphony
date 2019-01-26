#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2018-10-04 16:58:04

#include "Arduino.h"
#include <E131.h>
#include <FastLED.h>
#include "SymphonyCore.h"

int WsCallback(uint8_t * payload, size_t length) ;
attribStruct MyMqttCallback(attribStruct property, int scmd) ;
void setup() ;
void loop() ;
int handleCommand(int index, int cmd) ;
void FillLEDsFromPaletteColors( uint8_t colorIndex) ;
void ChangePalettePeriodically() ;
void SetupTotallyRandomPalette() ;
void SetupBlackAndWhiteStripedPalette() ;
void SetupPurpleAndGreenPalette() ;

#include "SymphonyE131.ino"


#endif
