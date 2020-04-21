#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2020-04-19 23:57:01

#include "Arduino.h"
#include "Arduino.h"
#include <ESPAsyncE131.h>
#include "Symphony.h"
#include "version.h"

void handleInit(AsyncWebServerRequest *request) ;
void handleToggle(AsyncWebServerRequest *request) ;
void handleGetConfig(AsyncWebServerRequest *request) ;
void sendTimerData(AsyncWebSocketClient *client, JsonObject& json) ;
int wsHandler(AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json) ;
int mqttHandler(JsonObject& json) ;
void setup() ;
void loop() ;

#include "SymphonySocket.ino"


#endif
