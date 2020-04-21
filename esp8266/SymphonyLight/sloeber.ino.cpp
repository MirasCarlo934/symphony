#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2020-04-19 20:33:05

#include "Arduino.h"
#include "Arduino.h"
#include <ESPAsyncE131.h>
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include "SymphonyLight.h"
#include "Product.h"

void handleInit(AsyncWebServerRequest *request) ;
void handleFire(AsyncWebServerRequest *request) ;
void handleToggle(AsyncWebServerRequest *request) ;
void handleGetConfig(AsyncWebServerRequest *request) ;
int wsHandlerJason(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) ;
void initWeb() ;
void fillPalettes() ;
void setup() ;
void loopTest() ;
void loop() ;

#include "SymphonyLight.ino"


#endif
