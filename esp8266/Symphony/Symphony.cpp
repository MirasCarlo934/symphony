/*
 * SymphonyCore.cpp
 *
 * This is the core framework for the Symphony Products.
 * This will connect to an AP, the SSID and PassPhrase can be configured at <this>.ip/main
 * By default, AP Mode is enabled if this cannot connect to an AP.  This can be changed in <this>.ip/ota_setup
 * 		options: 1-default (AP enable if not connected to AP)
 * 				 2-AP always enabled
 * 				 3-AP always disabled
 *
 * Usage Notes:
 * 1. ALWAYS set the product first before calling setup
 *		s.setProduct(product);
 *		s.setup();
 * 2. ALWAYS set the wsCallback.  This is for the handling of requests from clients. This is via WebSockets.
 *		s.setWsCallback(WsCallback);
 *
 *
 *TODO:
 *	Oct 06, 2018:
 *	1. modify wsEvent to handle events with 4 characters
 *	2. modify wsEvent to handle INIT events, instantiators should no longer handle this even if they have set a callback
 *
 */


#include "Symphony.h"
#include "DeviceDiscovery.h"

//#define DISCOVERABLE

String Symphony::rootProperties = "";
String Symphony::hostName = "hostName";
String Symphony::mac = "";
String Symphony::nameWithMac = "myName";
Product Symphony::product;
String Symphony::version = "0.0";
bool Symphony::hasMqttHandler = false;

AsyncWebServer		webServer(HTTP_PORT); // Web Server
AsyncWebServer		wsServer(WS_PORT); // WebSocket Server
AsyncWebSocket      ws("/ws");      // Web Socket Plugin
WiFiEventHandler    wifiConnectHandler;     // WiFi connect handler
WiFiEventHandler    wifiDisconnectHandler;  // WiFi disconnect handler
MqttHandler 		theMqttHandler;	//Mqtt Handler

String homeHtml;

// DNS server
const byte DNS_PORT = 53;
DNSServer dnsServer;
String responseHTML = ""
                      "<!DOCTYPE html><html><head><title>CaptivePortal</title></head><body>"
                      "<h1>Hello World!</h1><p>This is a captive portal example. All requests will "
                      "be redirected here.</p></body></html>";

bool		reboot 	= false; // Reboot flag
bool 		isUpdateFw = false; // used to indicate if firmware update is ongoing
Filemanager	fManager = 		Filemanager();
int8_t		fwResult = 0;
long identifyTries[] = {5000, 10000, 20000};
int discoveryTries = 0;

//WebSocketsClient webSocketClient;  July 13 2019 do we really need this device to be a ws client?

/*
 * This is the callback handler that will be called when a Websocket event arrives.
 * This enables the instantiator to handle websocket events.
 * We are exposing the AsyncWebSocket ws to enable the instantiator to broadcast to all connected clients via the ws.textAll
 * AsyncWebSocketClient *client is exposed to enable response to the calling client.
 */
int (* WsCallback) (AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json);

/*
 *	wsEvent handles the transactions sent by client websockets.
 *	events can either be handled by the core, or the implementor.
 *	events recognized should be in json format and of the syntax:
 *	{"core":1, "data":""}, {"cmd":1, "data":""} or {"do":1, "data":""}
 *	where
 *	  "core" is handled by core,
 *	  "cmd" handled by implementor
 *	  "do" will be handle by both core and implementor
 *
 *	if "core", below are the recognized values
 *		0:
 *		1: client server connections command send by client,
 *			data: "INIT" - usually done during initial connect of client
 *			data: "PING" - done during the firmware update of client, client will receive websocket.onclose event if it tries to send during ESP reboot
 *						 - not needed as the client can detect the websocket.onclose event even without sending data
 *		2: commit AP, passkey and Device name
 *		3: delete file command from the fileupload client
 *
 */
void wsEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void * arg, uint8_t *data, size_t len) {
	if (!isUpdateFw) {
#ifdef DEBUG_ONLY
		Serial.printf("websocket called len=%i\n",len);
#endif
		switch (type) {
			case WS_EVT_DATA: {
				AwsFrameInfo *info = static_cast<AwsFrameInfo*>(arg);
				if (info->opcode == WS_TEXT) {
					DynamicJsonBuffer jsonBuffer;
					JsonObject& json = jsonBuffer.parseObject(data);
					if (!json.success()) {
						Serial.println("core parseObject() failed");
					} else {
#ifdef DEBUG_ONLY
						json.prettyPrintTo(Serial);Serial.println();
#endif
						if (json.containsKey("do")) {
							//this is for both the core and implementor
						} else if (json.containsKey("core")) {
							//this is for the core
							int core = json["core"].as<int>();
							switch (core) {

								case CORE_INIT:
									//this is the INIT command from the WS client.
									if (json.containsKey("data")) {
										String d = json["data"].as<String>();
										if (d.equals("INIT")) {
											DynamicJsonBuffer jsonBuffer;
											JsonObject& reply = jsonBuffer.createObject();
											reply["cmd"] = 1;
											reply["name"] = Symphony::hostName;
											reply["msg"] = "Ready for commands.";
											reply["mac"] = Symphony::mac;
											reply["box"] = "status";	//the element to show the message
											reply["cid"] = client->id();	//the client id
											reply["ver"] = Symphony::version;	//the version
											String strReply;
											reply.printTo(strReply);
											client->text(strReply);
										}
										if (d.equals("PING"))  //we are supposed to use this to ping this server, but websocket client.onclose is already triggered
											client->text("PING Successful");
									}
									break;
								case CORE_COMMIT://this is the commit AP, Passkey and Device Name, then reboot is done
									//this is from the admin WS client
									if (json.containsKey("data")) {
										String cfg = json["data"].as<String>();
#ifdef DEBUG_ONLY
										Serial.printf("\nwill save config %s\n", cfg.c_str());
#endif
										fManager.saveConfig(cfg.c_str());
										reboot = true;
									}
									break;
								case CORE_DELETE://this is the delete file command from the WS admin client
									//delete path fr SPIFFS
									if (json.containsKey("data")) {
										String path = json["data"].as<String>();
#ifdef DEBUG_ONLY
										Serial.printf("\nwill delete %s\n", path.c_str());
#endif
										fManager.delFile(path);
										client->text(path + " deleted");
									}
									break;
								case CORE_VALUES: {//VALUES command from the WS client, sends the current values of the product
									//this is used by the client app to display the current state of the product
										client->text(Symphony::product.stringifyValues());
									break;
								}
								case CORE_CONTROL://transactions from client to control the device
								{
									int cmd = json["cmd"].as<int>();
									if (cmd == 10) {//command from control
										//evaluate if corePin==true, execute here.  Else pass to wscallback
										attribStruct attrib = Symphony::product.getProperty(json["ssid"].as<char *>());
#ifdef DEBUG_ONLY
										Serial.printf("got attribute %s, current value=%i, pin=%i, corePin=%s\n", attrib.ssid.c_str(), attrib.gui.value, attrib.pin, attrib.corePin?"true":"false");
#endif
										if (attrib.corePin) {
											Symphony::product.setValue(json["ssid"].as<char *>(), json["val"].as<int>());
										} else {
											//this is for the implementor
											if (WsCallback != nullptr) {
												WsCallback(ws, client, json);
											} else {
												Serial.println("[wsEvent]No Websocket callback set!!!");
											}
										}
										String strReply;
										json.printTo(strReply);
										ws.textAll(strReply);
									} else {//other commands like setup, etc specific to the control of device
										//this is for the implementor
										if (WsCallback != nullptr) {
											WsCallback(ws, client, json);
										} else {
											Serial.println("[wsEvent]No Websocket callback set!!!");
										}
									}
								}
									break;
							}
						} else {
							Serial.println("core value-pair not found.");
						}
					}
				} else {
					Serial.println(F("-- binary message --"));
				}
				break;
			}
			case WS_EVT_CONNECT: {
				Serial.print(F("WS Connect - "));
				Serial.println(client->id());
// sep 06 2019, removed sending of data during connect, this should be handled via AJAX call
////				client->text("{\"box\":\"status\",\"msg\":\"Connected\"}");
//				DynamicJsonBuffer connReplyBuffer;
//				JsonObject& connReply = connReplyBuffer.createObject();
//				connReply["name"] = Symphony::hostName;
//				connReply["msg"] = "Connected";
//				connReply["mac"] = Symphony::mac;
//				connReply["box"] = "status";	//the element to show the message
//				connReply["cid"] = client->id();	//the client id
//				connReply.prettyPrintTo(Serial);
//				String strConnReply;
//				connReply.printTo(strConnReply);
//				client->text(strConnReply);
				break;
			}
			case WS_EVT_DISCONNECT: {
				Serial.print(F("WS Disconnect - "));
				Serial.println(client->id());
				break;
			}
			case WS_EVT_PONG: {
				Serial.println(F("WS PONG"));
				break;
			}
			case WS_EVT_ERROR: {
				Serial.println(F("WS ERROR"));
				break;
			}
		}
	} else {
		Serial.print(F("Cannot do websocket request since we are updating firmware.\n"));
	}
}

/**
* Handler tha displays Captive portal during softAP mode
*/
class CaptivePortalRequestHandler : public AsyncWebHandler {
public:
	CaptivePortalRequestHandler() {}
	virtual ~CaptivePortalRequestHandler() {}
	int ctr = 0;
	bool canHandle(AsyncWebServerRequest *request){
		//request->addInterestingHeader("ANY");
		return true;
	}

	void handleRequest(AsyncWebServerRequest *request) {
		AsyncClient *client = request->client();
		ctr++;
		Serial.printf("CaptivePortalRequestHandler Address:%i Port:%i, counter:%i\n", client->getRemoteAddress(), client->getRemotePort(), ctr);
		AsyncResponseStream *response = request->beginResponseStream("text/html");
		response->print("<meta http-equiv='Refresh' content='0; url=http://192.168.7.1/admin' />");  //this works in iPhone but not in ASUS android.
																									//But in iPhone, if you repeatedly do captive portal login, this does not work anymore.
																									//Need to forget and relogin again
		request->send(response);
	}
};

/**
 * This is for the apple devices
 * Sep 12 2019, but it does not seem to work
 */
void handleAppleCaptivePortal(AsyncWebServerRequest *request) {
    String Page = F("<HTML><HEAD><TITLE>Success</TITLE></HEAD><BODY>Success</BODY></HTML>");
    AsyncWebServerResponse *response = request->beginResponse(200, "text/plain");
    response->addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response->addHeader("Pragma", "no-cache");
    response->addHeader("Expires", "-1");
    request->send(response);
}
void onWifiConnect(const WiFiEventStationModeGotIP &event) {
    Serial.print(F("*** Connected with IP: "));
    Serial.print(WiFi.localIP());
    Serial.println(" ***");

    // Setup mDNS / DNS-SD
    char chipId[7] = { 0 };
    snprintf(chipId, sizeof(chipId), "%06x", ESP.getChipId());
    MDNS.setInstanceName("staticHostname");
    if (MDNS.begin("static.Hostname")) {
        MDNS.addService("http", "tcp", HTTP_PORT);
    } else {
        Serial.println(F("*** Error setting up mDNS responder ***"));
    }
    if (Symphony::hasMqttHandler)
    	theMqttHandler.connect();


#ifdef DISCOVERABLE
	 startDiscovery(Symphony::hostName, Symphony::mac);
	 discoveryTries = 0;
#endif

}
void onWiFiDisconnect(const WiFiEventStationModeDisconnected &event) {
    Serial.println(F("*** WiFi Disconnected ***"));

    // Pause MQTT reconnect while WiFi is reconnecting
//    mqttTicker.detach();
//    wifiTicker.once(2, connectWifi);
}

/***
 * Control Section
 * 		This is where we set the html display for controlling the device
 */
void showControl(AsyncWebServerRequest *request) {
	request->send(200, "text/html", homeHtml);
	Serial.println("control page displayed");
}
/*
 *  Returns the properties in the form:
 *  {typ:'Rad',lbl:'RED',val:'0007', grp:'g2'}
 *  {typ:'Rad',lbl:'GREEN',val:'0007', grp:'g2'},
 *  {typ:'Btn',lbl:'STOP',val:'0009', grp:'g2'},
 *  {typ:'Rng',lbl:'Hue',val:'0011',min:'0',max:'360', grp:'g2'}
 */
void showProperties(AsyncWebServerRequest *request) {
	request->send(200, "text/html", Symphony::rootProperties);
	Serial.println("**************************** showProperties");
}
/**
 * File upload Section
 */
void showFileUpload(AsyncWebServerRequest *request) {
	Serial.println("**************************** showFileUpload");
	request->send(200, "text/html", UPLOAD_HTML);//shows file upload html from the PROGMEM defined in html.h, we are doing this because initial loading of firmware does not have the SPIFFS files
}
/*
 * handles the upload of File
 */
void handleFileUpload(AsyncWebServerRequest *request, String filename, size_t index, uint8_t *data, size_t len, bool final) { // upload a new file to the SPIFFS
	Serial.printf("handleFileUpload filename=%s\n", filename.c_str());
	int8_t result = fManager.uploadFile(filename, index, data, len, final);
	Serial.printf("handleFileUpload done result=%i\n", result);
}
void doneFileUpload(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "{\"resp\":\"203\",\"msg\":\"File Upload Done.\"}");
	Serial.println("**************************** doneFileUpload");
}
void handleGetFiles(AsyncWebServerRequest *request) {
	String str = fManager.getFiles();
	request->send(200, "text/html", str);
}
/**
 * sends response to the get device information request
 */
void handleDevInfo(AsyncWebServerRequest *request) {
	DynamicJsonBuffer jsonBuffer;
	JsonObject& jsonObj = jsonBuffer.parseObject(fManager.readConfig());
	if (jsonObj.success()) {
#ifdef DEBUG_ONLY
		Serial.println("********************** AJAXGet device Info.");
		jsonObj.prettyPrintTo(Serial);
#endif
		String devInfo;
		jsonObj.printTo(devInfo);
		request->send(200, "text/html", devInfo);
	}
}
/**
 * Firmware update Section
 */
void showFWUpdate(AsyncWebServerRequest *request) {
	request->send(SPIFFS, "/admin.html", "text/html"); //we are showing admin.html in SPIFFS
//	request->send(200, "text/html", UPLOAD_HTML1);
	Serial.println("**************************** showFWUpdate");
}

void doneFWUpdate(AsyncWebServerRequest *request) {
	request->send(200, "text/html", "Firmware Update Done.");
	Serial.printf("**************************** doneFWUpdate %i\n", fwResult);
	reboot = true;
}
void handleFirmWareUpload(AsyncWebServerRequest *request, String filename, size_t index, uint8_t *data, size_t len, bool final) {
	isUpdateFw = true;
	delay(50);	//this is to enable the update to do SPIFF write before getting another data to write
	fwResult = fManager.updateFirmware(filename, index, data, len, final);
//	Serial.printf("**************************** handleFirmWareUpload %i\n", fwResult);
}
/**
 * Handles the commit config command
 */
void handleDeviceConfig(AsyncWebServerRequest *request) {
#ifdef DEBUG_ONLY
	Serial.printf("\nhandleDeviceConfig start\n");
#endif
	AsyncWebParameter* pName = request->getParam("pName");
	AsyncWebParameter* pSSID = request->getParam("pSSID");
	AsyncWebParameter* pPass = request->getParam("pPass");
	String configStr = "data:{name:$name, ssid:$ssid, pwd:$pwd}";
	configStr.replace("$name", pName->value().c_str());
	configStr.replace("$ssid", pSSID->value().c_str());
	configStr.replace("$pwd", pPass->value().c_str());
#ifdef DEBUG_ONLY
	Serial.printf("\handleDeviceConfig will save config %s\n", configStr.c_str());
#endif
	fManager.saveConfig(configStr.c_str());
	reboot = true;
}

/****
 * Initialize the webserver
 */
void initWebServer() {

	webServer.on("/control", showControl);		//shows the properties of the device for control functions
	webServer.on("/config", HTTP_GET, handleDeviceConfig);		//handles the commit config
	webServer.serveStatic("/admin", SPIFFS, "/admin.html");
	webServer.serveStatic("/img.jpg", SPIFFS, "/img.jpg");
	webServer.serveStatic("/symphony.css", SPIFFS, "/symphony.css");
//	webServer.serveStatic("/control.css", SPIFFS, "/control.css");
	webServer.serveStatic("/symphony.js", SPIFFS, "/symphony.js");
	webServer.on("/firmware", HTTP_GET, showFWUpdate);  //show the firmware update page
	webServer.on("/updateFirmware", HTTP_POST, doneFWUpdate, handleFirmWareUpload); //handle the firmware update request
	webServer.on("/files", HTTP_GET, showFileUpload);  //show the file upload page
	webServer.on("/uploadFile", HTTP_POST, doneFileUpload, handleFileUpload); //handle the file update request
	webServer.on("/getFiles", HTTP_GET, handleGetFiles);  //show the Files in SPIFFS
	webServer.on("/devInfo", HTTP_GET, handleDevInfo);  //show the Files in SPIFFS
	webServer.on("/properties.html", showProperties);
	webServer.serveStatic("/files.html", SPIFFS, "/files.html");
	webServer.serveStatic("/test.html", SPIFFS, "/test.html");
//	webServer.on("/hotspot-detect.html", handleAppleCaptivePortal);//for apple devices
	webServer.onNotFound([](AsyncWebServerRequest *request) {
		request->send(404, "text/plain", "Page not found");
	});
}
/*
 * Constructor
 */
Symphony::Symphony(){

}

/*
 * Initiates the Symphony module
 * theHostName is passed by the child
 * ver is passed by the child and should be composed of SYMPHONY_VERSION.CHILD_VERSION
 *
 */
void Symphony::setup(String theHostName, String ver) {
//	Serial.begin(115200);	//implementing objects should set the baud rate
//	wifi_set_sleep_type(NONE_SLEEP_T);
#ifdef SHOW_FLASH
	  uint32_t realSize = ESP.getFlashChipRealSize();
	  uint32_t ideSize = ESP.getFlashChipSize();
	  FlashMode_t ideMode = ESP.getFlashChipMode();
	  Serial.printf("\n[init]Sketch size: %u\n", ESP.getSketchSize());
	  Serial.printf("[init]Free Sketch size: %u\n", ESP.getFreeSketchSpace());
	  Serial.printf("[init]Flash real id:   %08X\n", ESP.getFlashChipId());
	  Serial.printf("[init]Flash real size: %u\n", realSize);
	  Serial.printf("[init]Flash ide  size: %u\n", ideSize);
	  Serial.printf("[init]Flash ide speed: %u\n", ESP.getFlashChipSpeed());
	  Serial.printf("[init]Flash ide mode:  %s\n", (ideMode == FM_QIO ? "QIO" : ideMode == FM_QOUT ? "QOUT" : ideMode == FM_DIO ? "DIO" : ideMode == FM_DOUT ? "DOUT" : "UNKNOWN"));
	  if(ideSize != realSize) {
		  Serial.printf("[init]Flash Chip configuration wrong!\n");
	  } else {
		  Serial.printf("[init]Flash Chip configuration ok.\n");
	  }
		Serial.printf("\n Core version %s\n\n", ver);
		Serial.println();
		Serial.println("****************** Start core setup ******************");
#endif
	Symphony::version = ver;
	// Setup WiFi Handler
	wifiConnectHandler = WiFi.onStationModeGotIP(onWifiConnect);
	delay(100);
	initWebServer();	//initialize the html pages
	connectToWifi();		//we are connecting to the wifi AP
	createMyName(theHostName);		//create this device's name
	if (WiFi.status() != WL_CONNECTED) {
		//setup the AP for this device since we cannot connect to wifi as station
		setupAP();
	}
	homeHtml = CONTROL_HTML1;
	homeHtml.replace("$AAA$", hostName);
	Serial.printf("Hostname is %s.local version is %s\n", hostName.c_str(), Symphony::version.c_str());
	// Handle OTA update from asynchronous callbacks
	Update.runAsync(true);
	// Setup WebSockets
	ws.onEvent(wsEvent);
	wsServer.addHandler(&ws);
	webServer.begin();

	wifiDisconnectHandler = WiFi.onStationModeDisconnected(onWiFiDisconnect);
	webServer.begin();
	wsServer.begin();

#ifdef WSCLIENT
	startWebSocketClients();
#endif

	Serial.print(F("- Web Server started on port "));
	Serial.println(HTTP_PORT);
	Serial.printf("\n************[Symphony] Setup Version %i,  boot:%i***************\n", SYMPHONY_VERSION, reboot);
}

/*
 * The loop method
 * 		returns true if firmware update is not ongoing
 * 		false otherwise
 */
bool Symphony::loop() {
	//DO NOT PUT A delay() AS IT CAUSES ERROR DURING OTA
	// Reboot handler
	if (reboot) {
		Serial.println("Rebooting...");
		delay(REBOOT_DELAY);
		ESP.restart();
	} else {
		//we process other items if it is not reboot mode
		dnsServer.processNextRequest();
//		webSocketClient.loop();July 13 2019 do we really need this device to be a ws client?
		registerProduct();//register this product to BM
	}
	if (!isUpdateFw) {
#ifdef DISCOVERABLE
		if (discoveryTries < 3) {
			sendIdentify(identifyTries[discoveryTries]);
			discoveryTries++;
		} else
			sendIdentify();	//this device sends discovery identify mode every 2mins, use sendIdentify(ms) if you want to override interval
#endif
		return true;
	} else {
		return false;
	}
}

/*
 * this enables the instantiator to set handler methods for the webserver
 */
void Symphony::on(const char* uri, WebRequestMethodComposite method, ArRequestHandlerFunction handler) {
	webServer.on(uri, method, handler);
}

void Symphony::serveStatic(const char* uri, fs::FS& fs, const char* path) {
	webServer.serveStatic(uri, fs, path);
}
/*
 *
 * This callback is called by the webSocketEvent function.  We can do manipulation of pins here.
 *
 */
void Symphony::setWsCallback(int (* Callback) (AsyncWebSocket ws, AsyncWebSocketClient *client, JsonObject& json)) {
	WsCallback = Callback;
}
/*
 *
 * This is the handler for all MQTT transactions.
 *
 */
void Symphony::setMqttHandler(const char *id, const char *url, int port) {
	theMqttHandler.setId(id);
	theMqttHandler.setUrl(url);
	theMqttHandler.setPort(port);
	hasMqttHandler = true;
}

/**
 * Private methods below
 */
void Symphony::setupAP() {
	Serial.println(F("Failed to connect as wifi client, going to softAP."));
	ap_ssid = "AP_"+hostName;
	WiFi.mode(WIFI_AP);
	/* Soft AP network parameters */
	IPAddress netMsk(255, 255, 255, 0);
    WiFi.softAPConfig(apIP, apIP, netMsk);
#ifdef DEBUG_ONLY
    Serial.printf("AP:%s, pk:%s, ip:%s\n",ap_ssid.c_str(),ap_passphrase.c_str(),apIP.toString().c_str());
#endif
    WiFi.softAP(ap_ssid.c_str(), ap_passphrase.c_str());

    /* Setup the DNS server redirecting all the domains to the apIP */
	dnsServer.setErrorReplyCode(DNSReplyCode::NoError);
	dnsServer.start(DNS_PORT, "*", apIP);
	webServer.addHandler(new CaptivePortalRequestHandler()).setFilter(ON_AP_FILTER);//only when requested from AP
//	webServer.serveStatic("/generate_204", SPIFFS, "/admin.html");
//	webServer.serveStatic("/fwlink", SPIFFS, "/admin.html");
//
//	webServer.serveStatic("/", SPIFFS, "/admin.html");
//	webServer.serveStatic("/wifi", SPIFFS, "/admin.html");
//	webServer.serveStatic("/0wifi", SPIFFS, "/admin.html");
//	webServer.serveStatic("/wifisave", SPIFFS, "/admin.html");
//	webServer.serveStatic("/i", SPIFFS, "/admin.html");
//	webServer.serveStatic("/r", SPIFFS, "/admin.html");
}
/**
 * Connect to the AP using the passkey
 * If unable to connect, setup its own AP
 */
void Symphony::connectToWifi() {
	// Switch to station mode and disconnect just in case
	WiFi.mode(WIFI_STA);
	WiFi.disconnect();
	//sets the wifi login
	DynamicJsonBuffer jsonBuffer;
	JsonObject& json = jsonBuffer.parseObject(fManager.readConfig());
	if (!json.success()) {
		Serial.println("connectToWifi parseObject() failed");
	} else {
		if (json.containsKey("ssid")) {
			//ssid key is found, this means pwd and name are also there
			ssid = json["ssid"].as<String>();
			pwd = json["pwd"].as<String>();
			nameWithMac = json["name"].as<String>();
		}
#ifdef DEBUG_ONLY
		json.prettyPrintTo(Serial);
		Serial.printf("\nssid:%s pwd:%s\n", ssid.c_str(), pwd.c_str());
#endif
	}

	WiFi.begin(ssid.c_str(), pwd.c_str());
	int i = 0;
	//tries to connect to the AP
	while (WiFi.status() != WL_CONNECTED && i <= wifiMaxConnCount) {
		i++;
		delay(200);
	    Serial.print(".");
	}
}
/*
 * this creates the unique name of this device based on its mac address
 * hostname will be myName, which is from config file read during connectToWifi
 * if there is no config file, it will be theHostName
 */
void Symphony::createMyName(String theHostName) {
	if (nameWithMac.length() == 0)
		nameWithMac = theHostName;
	hostName = nameWithMac;
	hostName.toLowerCase();
	nameWithMac += "_";
	// Generate device name based on MAC address
	uint8_t _mac[6];
	WiFi.macAddress(_mac);
	//	we generate the name based on the MAC values
	for (int i = 0; i < 6; ++i) {
		nameWithMac += String(_mac[i], 16);
		mac += String(_mac[i], 16);
	}
}
/**
 * sets the product details of this device
 */
void Symphony::setProduct(Product p) {
	product = p;
	setRootProperties(product.stringify());
	isProductSet = true;
}
/**
 * sends broadcast message to all connected websocket clients
 */
void Symphony::textAll(JsonObject& message){
	size_t len = message.measureLength();
	AsyncWebSocketMessageBuffer * buffer = ws.makeBuffer(len);
	if (buffer) {
		message.printTo((char *)buffer->get(), len + 1);
		ws.textAll(buffer);
	}
}
/**
 * Sets the properties that will be displayed as root HTML as interpreted by js.loadDoc();
 */
void Symphony::doReboot() {
	reboot = true;
}
/**
 * Sets the properties that will be displayed as root HTML as interpreted by js.loadDoc();
 */
void Symphony::setRootProperties(String s) {
	Symphony::rootProperties = s;
}
/**
 * /should be called after mqttHandler has been set and product has been set
 */
bool Symphony::registerProduct() {
	if (!isRegistered) {
		if (isProductSet) {
			if ( hasMqttHandler && theMqttHandler.isConnected()) {
				//register to the BM if not yet registered and if product is set and if mqtt is connected
				isRegistered = true;
				theMqttHandler.setProduct(product);
				Serial.println("************** registerProduct start 1");
				/*	Registration format is:
					"{RID:5ccf7f15a492,CID:0000,RTY:register,name:Ngalan,roomID:J444,product:0000}";
				*/
				DynamicJsonBuffer jsonBuffer;
				JsonObject& regJson = jsonBuffer.createObject();
				regJson["RID"] = Symphony::mac;
				regJson["CID"] = "0000";
				regJson["RTY"] = "register";
				regJson["name"] = nameWithMac;
				regJson["roomID"] = "J444";
				regJson["product"] = "0000";
				String strReg;
				regJson.printTo(strReg);
				theMqttHandler.publish(strReg.c_str(), 0);
				Serial.printf("************** registerProduct end payload len=%i\n", strReg.length());
			}
		}
	}
}
void Symphony::sendToWsServer(String replyStr){
//	webSocketClient.sendTXT(replyStr);July 13 2019 do we really need this device to be a ws client?
}
/***********************************************************************
*					Utility classes
************************************************************************/
/**
 * This is the class for the holder of data received by Websocket
 * Data is of the format <ssid>=<value>
 *    ex 0006=1
 */
WsData::WsData(String data){
	deviceName = data.substring(0, data.indexOf(':'));
	value = data.substring(data.indexOf('=') + 1);
	ssid = data.substring(data.indexOf(':') + 1, data.indexOf('='));
}

String WsData::getDeviceName() {
	return deviceName;
}
String WsData::getSSID() {
	return ssid;
}
String WsData::getValue() {
	return value;
}

