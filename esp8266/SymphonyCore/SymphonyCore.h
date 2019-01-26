/**
 * This is the core for the Symphony Home
 *
 *
 */

#ifndef SYMPHONYCORE_H_
#define SYMPHONYCORE_H_

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <DNSServer.h>
//#include <ArduinoOTA.h>
#include <PubSubClient.h>
#include <WebSocketsServer.h>
//#include "RemoteDebug.h" // Remote debug over telnet - not recommended for production, only for development
#include "mainhtml.h" //source for the html pages of the captive portal and the default html
#include "MqttUtil.h"
//#include "CloudMqttUtil.h"
#include "SymphonyProduct.h"
#include "SpiffsUtil.h"
#include <ESP8266HTTPClient.h>
#include <ESP8266httpUpdate.h>

#define FW_VERSION 1252

#define DEBUG_ true
//#define PROD

class Symphony {
	public:
		//constructor
		Symphony();

	    //public attributes
	    boolean isWifiConnected = false;
	    boolean isMqttSuccessful = false;
	    boolean isPBSuccessful = false;
	    uint8_t static apMode;		//1-default (AP enable if not connected as station), 2-AP always enabled, 3-AP always disabled
	    boolean isApEnabled = false;//will only be applicable if apMode is 1 or 2
	    boolean isOtaOK = false;

	    //public methods
	    void setup();
	    void loop();
	    void print();
	    void setProduct(SymphProduct p);
	    void setWsCallback(int (* WsCallback) (uint8_t * payload, size_t length));
	    void setMqttCallback(attribStruct (* myMqttCB)(attribStruct property, int cmd));
//	    void setMqttCallback(attribStruct (* MyMqttCallback) (attribStruct property, int scmd));
//	    void static doCommand(String ssid, String scmd);  //static method for handling the command for the SSID
//	    void static doCommand(const char *ssid, const char *scmd);  //static method for handling the command for the SSID. DEPRECATED Apr 15 2017
	    void static setProperty(const char *ssid, const char *scmd);  //static method for setting the property for the SSID
	    void showStatus();
	    void on(const char* uri, ESP8266WebServer::THandlerFunction handler);
	    void static sendResponse(const char *response);
	    void sendToWSClient();

	private:
	    String myName, ap_ssid, ap_passphrase = "12345678";
	    IPAddress apIP = IPAddress (192, 168, 7, 1);
	    String ssid, pwd;
	    int wifiMaxConnCount=50;  //max counter when connecting to wifi AP, corresponds to 10secs
	    long restartTimer = 0;  //the restart timer in millis.  this will restart every maxrestartTimer if wifi is not connected.
	    const long maxRestartTimer = 120000; //the max millis before restart.  2 mins
	    long bmLoginAttemptTimer = 0, bmLoginAttemptTimerPrev = 0;  //the BM login attempt timer.
	    const long maxBmLoginDelay = 60000; //the delay before the next BM login attempt.  1min
		PubSubClient client, cloudclient;
	    WiFiClient wifiClient;
	    SymphProduct product;
	    String hostName = "Symphony";

	    void connectToWifi();
	    void setupAP();
	    void createMyName();
//	    void startOTA();
};

class WsData
{
  public:
	WsData(uint8_t * payload, size_t length);
	String getDeviceName();
	String getSSID();
	String getValue();
  private:
	String deviceName;
	String ssid;
	String value;
};
#endif /* SYMPHONYCORE_H_ */
