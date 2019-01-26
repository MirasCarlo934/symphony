#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2018-12-28 22:15:16

#include "Arduino.h"
#include "Arduino.h"
#include <ESPAsyncE131.h>
#define FASTLED_INTERRUPT_RETRY_COUNT 3
#include "SymphonyLight.h"

void handleInit(AsyncWebServerRequest *request) ;
void handleFire(AsyncWebServerRequest *request) ;
void handleToggle(AsyncWebServerRequest *request) ;
int wsHandlerJason(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) ;
void initWeb() ;
void setup() ;
void loop() ;

#include "SymphonyLight.ino"


#endif
