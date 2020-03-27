/**
 * This is the core for the Symphony Home
 *
 *
 */

#ifndef SYMPHONY_H
#define SYMPHONY_H

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <DNSServer.h>
#include <ArduinoJson.h>

#include "FileManager.h"
#include "html.h"
#include "version.h"
#include "Product.h"
#include "MqttHandler.h"

#define DEBUG_ONLY
//#define SHOW_FLASH
#define FW_VERSION 1252
#define HTTP_PORT 80      /* Default web server port */
#define WS_PORT 8080      /* Web socket server port */
#define CONNECT_TIMEOUT 15000   /* 15 seconds */
#define REBOOT_DELAY    1500     /* Delay for rebooting once reboot flag is set */

#define  CORE_INIT 1
#define  CORE_COMMIT_DEVICE_SETTINGS 2
#define  CORE_DELETE 3
#define  CORE_GETDEVICEINFO 4
#define  CORE_PING 5
#define  CORE_VALUES 20
#define  CORE_CONTROL 7

class Symphony {
	public:

		//constructor
		Symphony();

	    //public attributes
	    static String rootProperties;//the properties of the form {typ:'Rad',lbl:'RED',val:'0007', grp:'g2'}
	    static String hostName;//the hostName of this device
	    static String mac;//the mac address
	    static String nameWithMac;//the name of this device that includes the mac address
	    static Product product;
	    static String version;
	    static bool hasMqttHandler;

	    //public methods
	    void setup(String theHostName, String ver);
	    bool loop();
	    //lets the instantiator of this Symphony object assign a callback handler
	    void on(const char* uri, WebRequestMethodComposite method, ArRequestHandlerFunction handler);
	    void serveStatic(const char* uri, fs::FS& fs, const char* path);
	    void setWsCallback(int (* WsCallback) (AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json));
	    void setMqttCallback(int (* MqttCallback) (JsonObject& json));
	    void textAll(JsonObject& message);  //for sending to WsClients
	    static void setRootProperties(String s);
	    void setProduct(Product p);
	    void doReboot();
	    void sendToWsServer(String replyStr);
	    void transmit(const char* payload);
	    static String getMRN();

	private:
	    String ap_ssid, ap_passphrase = "12345678";
	    IPAddress apIP = IPAddress (192, 168, 7, 1);
	    String ssid = "ssid", pwd = "pwd", mqttIp = "localhost";
	    static long MRN;	//the Message Reference Number, an incremental variable
	    bool isProductSet = false, isRegistered = false;
	    int wifiMaxConnCount=50, mqttPort=1883;  //max counter when connecting to wifi AP, corresponds to 10secs
	    long restartTimer = 0;  //the restart timer in millis.  this will restart every maxrestartTimer if wifi is not connected.
	    const long maxRestartTimer = 120000; //the max millis before restart.  2 mins
//	    String hostName = "Symphony";
	    void connectToWifi();
	    void createMyName(String theHostName);
	    void setupAP();
	    void enableMqttHandler();
	    bool registerProduct();	//should be called after mqttHandler has been set and product has been set
	    void readConfigFile();	//reads the config file and loads to the variables
};


#endif /* SYMPHONY_H */
