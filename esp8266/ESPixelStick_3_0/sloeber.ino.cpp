#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2018-09-20 20:35:12

#include "Arduino.h"
extern const char ssid[];
extern const char passphrase[];
#include <ESP8266WiFi.h>
#include <Ticker.h>
#include <AsyncMqttClient.h>
#include <ESP8266mDNS.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncUDP.h>
#include <ESPAsyncWebServer.h>
#include <ESPAsyncE131.h>
#include <ArduinoJson.h>
#include <Hash.h>
#include <SPI.h>
#include "ESPixelStick.h"
#include "EFUpdate.h"
#include "wshandler.h"
extern "C" {
#include <user_interface.h>
}

RF_PRE_INIT() ;
void setup() ;
void initWifi() ;
void connectWifi() ;
void onWifiConnect(const WiFiEventStationModeGotIP &event) ;
void onWiFiDisconnect(const WiFiEventStationModeDisconnected &event) ;
void multiSub() ;
void connectToMqtt() ;
void onMqttConnect(bool sessionPresent) ;
void onMqttDisconnect(AsyncMqttClientDisconnectReason reason) ;
void onMqttMessage(char* topic, char* p_payload,         AsyncMqttClientMessageProperties properties, size_t len, size_t index, size_t total) ;
void publishRGBState() ;
void publishRGBBrightness() ;
void publishRGBColor() ;
void initWeb() ;
void validateConfig() ;
void updateConfig() ;
void dsNetworkConfig(JsonObject &json) ;
void dsDeviceConfig(JsonObject &json) ;
void loadConfig() ;
void serializeConfig(String &jsonString, bool pretty, bool creds) ;
void saveConfig() ;
void setStatic(uint8_t r, uint8_t g, uint8_t b) ;
void loop() ;


#include "ESPixelStick.ino"

#endif
